-- Baseline schema.
--
-- This is src/main/java/br/com/papaprecoapi/outros/banco.sql, which was applied
-- by hand until now, turned into the first migration. It is deliberately a
-- translation rather than a redesign: the shape of the schema is what the
-- entities and the native queries in the repositories already assume, and a
-- baseline that quietly changes semantics is a baseline nobody can trust.
--
-- Four things did change, because the original could not run start to finish:
--
--   1. pg_trgm was created twice, the second time without IF NOT EXISTS, which
--      aborts the script on any database that already had it.
--   2. An index was declared on produto (nome, latitude, longitude). produto
--      has no latitude or longitude — those columns are on localizacao — so
--      that statement always failed. Dropped rather than guessed at; the
--      geospatial indexing item in Phase 4 is where it gets decided properly,
--      with EXPLAIN ANALYZE rather than by assumption.
--   3. fuzzystrmatch was created and never used. The ranking queries use
--      similarity(), which is pg_trgm. Dropped.
--   4. The INSERT statements were interleaved with the DDL. Demo rows are not
--      schema, and a deployed database must not get them; they moved to
--      db/seed, which is a separate Flyway location that has to be opted into.
--
-- Both extensions and the plpgsql function need rights an unprivileged user
-- does not have. The postgres container's default user is a superuser so this
-- runs as-is; on RDS later, pg_trgm is in the permitted extension list for
-- rds_superuser, which is what the master user gets.

CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- ---------------------------------------------------------------------------
-- Tables
-- ---------------------------------------------------------------------------

-- GENERATED ALWAYS AS IDENTITY throughout, matching GenerationType.IDENTITY on
-- the entities: Hibernate lets the database allocate the id and reads it back,
-- and an explicit id in an INSERT is rejected rather than silently colliding
-- with the sequence.

CREATE TABLE localizacao (
    id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    descricao VARCHAR(1024) NOT NULL,
    -- Also the index behind LocalizacaoRepository.findByLatitudeAndLongitude,
    -- which is how a coordinate pair is deduplicated on insert.
    UNIQUE (latitude, longitude)
);

CREATE TABLE usuario (
    id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    nome VARCHAR(128) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    -- Nullable on purpose: an account created through Google Sign-In has no
    -- local password.
    senha VARCHAR(255),
    localizacao_id INTEGER,
    verificado BOOL DEFAULT FALSE,
    fcm_token VARCHAR(255),
    -- ON DELETE CASCADE from localizacao is carried over from banco.sql. It
    -- reads backwards — deleting a coordinate deletes the users who live near
    -- it — but nothing in the API deletes a localizacao, so it has never
    -- fired. Left as it is rather than changed inside a baseline; correcting
    -- it belongs in its own migration.
    FOREIGN KEY (localizacao_id) REFERENCES localizacao(id) ON DELETE CASCADE
);

CREATE TABLE produto (
    id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    nome VARCHAR(128) NOT NULL,
    descricao VARCHAR(512),
    preco NUMERIC(8,2) NOT NULL,
    localizacao_id INTEGER NOT NULL,
    -- Set by the database, and mapped insertable=false updatable=false on the
    -- entity, so the row records when the API heard about the price.
    data_insercao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- Supplied by the client: when the price was actually seen in the shop.
    data_observacao TIMESTAMP NOT NULL,
    usuario_id INTEGER NOT NULL,
    FOREIGN KEY (localizacao_id) REFERENCES localizacao(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

CREATE TABLE voto_usuario_produto (
    id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    id_usuario INTEGER NOT NULL,
    id_produto INTEGER NOT NULL,
    -- TRUE confirms the price, FALSE disputes it.
    voto BOOLEAN NOT NULL,
    data_voto TIMESTAMP DEFAULT NOW(),
    FOREIGN KEY (id_produto) REFERENCES produto(id) ON DELETE CASCADE,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id) ON DELETE CASCADE,
    -- One vote per user per product; the API upserts against this.
    CONSTRAINT unique_user_product UNIQUE (id_produto, id_usuario)
);

CREATE TABLE codigo_verificacao (
    id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    codigo VARCHAR(255) NOT NULL,
    usuario_id INTEGER NOT NULL,
    -- VARCHAR rather than a PostgreSQL enum type, matching @Enumerated(STRING)
    -- on CodigoVerificacao.tipo. The columnDefinition on that field names a
    -- tipo_codigo_verificacao type that was only ever commented out; it has no
    -- effect while Hibernate generates no DDL.
    tipo VARCHAR(64) NOT NULL,
    data_validade TIMESTAMP,
    data_geracao TIMESTAMP DEFAULT NOW(),
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

CREATE TABLE alerta_usuario (
    id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    -- A product name the user is watching, matched fuzzily against produto.nome
    -- by the notification query below. Not a foreign key: the alert is created
    -- before any matching product necessarily exists.
    produto VARCHAR(128) NOT NULL,
    preco NUMERIC(8,2) NOT NULL,
    usuario_id INTEGER NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------------
-- Indexes
-- ---------------------------------------------------------------------------

-- GIN over trigrams: this is what makes similarity(LOWER(nome), :palavra) in
-- ProdutoRepository a lookup rather than a scan of every row.
CREATE INDEX idx_produto_nome_trgm ON produto USING gin (nome gin_trgm_ops);
CREATE INDEX idx_produto_nome ON produto (nome);
CREATE INDEX idx_voto_produto ON voto_usuario_produto (id_produto, voto);
CREATE INDEX idx_voto ON voto_usuario_produto (voto);

-- ---------------------------------------------------------------------------
-- Views
-- ---------------------------------------------------------------------------

-- Prices nobody has disputed, one row per (name, coordinate), most recently
-- observed first. Read by the alert query below.
CREATE OR REPLACE VIEW produtos_sem_voto_negativo AS
SELECT
    p.id AS produto_id,
    p.nome,
    p.preco,
    p.data_observacao,
    l.latitude AS produto_latitude,
    l.longitude AS produto_longitude,
    ROW_NUMBER() OVER (PARTITION BY LOWER(p.nome), l.latitude, l.longitude ORDER BY p.data_observacao DESC) AS row_num
FROM
    produto p
JOIN
    localizacao l ON p.localizacao_id = l.id
LEFT JOIN
    voto_usuario_produto vup ON vup.id_produto = p.id
GROUP BY
    p.id, l.latitude, l.longitude
HAVING
    SUM(CASE WHEN vup.voto = false THEN 1 ELSE 0 END) = 0
ORDER BY p.data_observacao DESC;

-- One alert per user, chosen at random, so the twice-daily notification job
-- sends at most one push per user per run rather than one per alert.
CREATE OR REPLACE VIEW alerta_aleatorio AS
SELECT
    u.id AS usuario_id,
    au.id AS alerta_id,
    au.produto,
    au.preco,
    ROW_NUMBER() OVER (PARTITION BY u.id ORDER BY random()) AS rn
FROM
    alerta_usuario au
JOIN
    usuario u ON u.id = au.usuario_id;

-- ---------------------------------------------------------------------------
-- Functions
-- ---------------------------------------------------------------------------

-- Great-circle distance in kilometres. Used by the alert query to keep a
-- notification within 10 km of the user's own coordinates. Phase 4 replaces
-- this with PostGIS or earthdistance, which can be indexed; a plpgsql function
-- cannot, so today every candidate row is computed.
CREATE OR REPLACE FUNCTION haversine(lat1 float8, lon1 float8, lat2 float8, lon2 float8)
RETURNS float8 AS $$
DECLARE
    r float8 := 6371; -- Earth's radius in km
    d_lat float8 := radians(lat2 - lat1);
    d_lon float8 := radians(lon2 - lon1);
    a float8 := sin(d_lat / 2)^2 + cos(radians(lat1)) * cos(radians(lat2)) * sin(d_lon / 2)^2;
    c float8 := 2 * atan2(sqrt(a), sqrt(1 - a));
BEGIN
    RETURN r * c;
END;
$$ LANGUAGE plpgsql;
