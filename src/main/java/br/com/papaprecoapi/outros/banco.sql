CREATE EXTENSION IF NOT EXISTS pg_trgm;


/**************************************************************************************************************/

CREATE TABLE localizacao (
    id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    descricao VARCHAR(1024) NOT NULL,
    UNIQUE (latitude, longitude)
);

INSERT INTO localizacao (latitude, longitude, descricao)
VALUES 
    (-25.46968, -49.235317, 'Rua Banana'),
    (-25.469685, -49.235317, 'Rua Banana'),
    (-25.46958, -49.235217, 'Rua Pacote de Açúcar'),
    (-25.46978, -49.235417, 'Rua Pão de Forma'),
    (-25.46948, -49.235117, 'Rua Leite'),
    (-25.46988, -49.235517, 'Rua Café'),
    (-25.46938, -49.235017, 'Rua Arroz'),
    (-25.46998, -49.235617, 'Rua Feijão'),
    (-25.46928, -49.234917, 'Rua Macarrão'),
    (-25.47008, -49.235717, 'Rua Óleo de Soja'),
    (-25.46918, -49.234817, 'Rua Sal');

/**************************************************************************************************************/

CREATE TABLE usuario (
    id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    nome VARCHAR(128) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    senha VARCHAR(255),
    localizacao_id INTEGER,
    verificado BOOL DEFAULT FALSE,
    fcm_token VARCHAR(255),
    FOREIGN KEY (localizacao_id) REFERENCES localizacao(id) ON DELETE CASCADE
);
INSERT INTO usuario (nome, email, senha, localizacao_id, verificado) VALUES 
('João Silva', 'joao.silva@example.com', '$2a$10$7uV3eBlXeQwoRCz.vR2UjuU1EIEuABk4zIzl2FTSHKKVRrh3a23v2', 1, true),
('Maria Oliveira', 'maria.oliveira@example.com', '$2a$10$8qMntoRTpHExdpXyER5cyOlBGwrGUbJJznbF1Kak49pq6QgCJGYP2', 3, true),
('Carlos Souza', 'carlos.souza@example.com', '$2a$10$5D25DY4ZtGaxTC2IAVaQYu5HCOUxo.YoI9q/SafI/w5QXyovD8Z8O', 5, true),
('Ana Pereira', 'ana.pereira@example.com', '$2a$10$Qft8ICsxyKHJlzPV4tW.7uCRn3yR.9A1kdRdtOkA/O5FdRnYV4I6m', 7, true),
('Lucas Lima', 'lucas.lima@example.com', '$2a$10$1f1iwcnlO8yRcUe4PLsK1u2MzwEdI6AA9vl5qRj2sqkfj2/Qc1WE6', 9, true);

/**************************************************************************************************************/

CREATE TABLE produto (
	id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
	nome VARCHAR(128) NOT NULL,
	descricao VARCHAR(512),
	preco NUMERIC(8,2) NOT NULL,
    localizacao_id INTEGER NOT NULL,
	data_insercao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	data_observacao TIMESTAMP NOT NULL,
	usuario_id INTEGER NOT NULL,
    FOREIGN KEY (localizacao_id) REFERENCES localizacao(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

INSERT INTO produto (nome, preco, usuario_id, localizacao_id, data_observacao)
VALUES 
    ('Banana', 4.50, 1, 1, CURRENT_TIMESTAMP),
    ('Banana', 5.00, 2, 1, CURRENT_TIMESTAMP),
    ('Banana', 5.50, 3, 1, CURRENT_TIMESTAMP),
    ('Banana', 4.80, 4, 2, CURRENT_TIMESTAMP),
    ('Pacote de Açúcar', 3.00, 5, 3, CURRENT_TIMESTAMP),
    ('Pão de Forma', 7.50, 1, 4, CURRENT_TIMESTAMP),
    ('Leite', 4.00, 2, 5, CURRENT_TIMESTAMP),
    ('Café', 10.00, 3, 6, CURRENT_TIMESTAMP),
    ('Arroz', 20.00, 4, 7, CURRENT_TIMESTAMP),
    ('Feijão', 8.00, 5, 8, CURRENT_TIMESTAMP),
    ('Macarrão', 6.00, 1, 9, CURRENT_TIMESTAMP),
    ('Óleo de Soja', 8.50, 2, 10, CURRENT_TIMESTAMP),
    ('Sal', 2.50, 3, 11, CURRENT_TIMESTAMP);



/**************************************************************************************************************/

CREATE TABLE voto_usuario_produto (
	id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
	id_usuario INT NOT NULL,
	id_produto INT NOT NULL,
    voto BOOLEAN NOT NULL, -- TRUE para voto positivo, FALSE para voto negativo
    data_voto TIMESTAMP DEFAULT NOW(),
    FOREIGN KEY (id_produto) REFERENCES produto(id) ON DELETE CASCADE,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id) ON DELETE CASCADE,
    CONSTRAINT unique_user_product UNIQUE (id_produto, id_usuario)
);
INSERT INTO voto_usuario_produto (id_usuario, id_produto, voto) VALUES
(1, 1, TRUE),
(2, 1, FALSE),
(3, 1, TRUE),
(4, 1, TRUE),
(5, 1, FALSE),
(1, 2, FALSE),
(2, 2, TRUE),
(3, 2, FALSE),
(4, 2, TRUE),
(5, 2, TRUE);

/**************************************************************************************************************/

/*CREATE TABLE token_verificar_email (
    id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    token VARCHAR(255) NOT NULL,
    data_criacao TIMESTAMP NOT NULL,
    data_validade TIMESTAMP NOT NULL,
    usuario_id INTEGER NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);*/

/**************************************************************************************************************/

/*CREATE TABLE loja (
	id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
	nome VARCHAR(128) NOT NULL,
	endereco VARCHAR(256) NOT NULL
);*/


/**************************************************************************************************************/

/*CREATE TABLE redefinir_senha_token (
    id INTEGER PRIMARY KEY  GENERATED ALWAYS AS IDENTITY,
    token VARCHAR(255) NOT NULL,
    usuario_id INTEGER NOT NULL,
    data_validade TIMESTAMP NOT NULL,
    CONSTRAINT fk_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);*/

/**************************************************************************************************************/

/*CREATE TYPE tipo_codigo_verificacao AS ENUM ('VERIFICAR_EMAIL', 'REDEFINIR_SENHA');*/
CREATE TABLE codigo_verificacao (
    id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    codigo VARCHAR(255) NOT NULL,
    usuario_id INTEGER NOT NULL,
    tipo VARCHAR(64) NOT NULL,
    data_validade TIMESTAMP,
    data_geracao TIMESTAMP DEFAULT NOW(),
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

/**************************************************************************************************************/

CREATE TABLE alerta_usuario (
    id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    produto VARCHAR(128) NOT NULL,
    preco NUMERIC(8, 2) NOT NULL,
    usuario_id INTEGER NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);


/**************************************************************************************************************/


CREATE EXTENSION pg_trgm;  -- Ativar a extensão de trigramas
CREATE EXTENSION fuzzystrmatch;

CREATE INDEX idx_produto_nome_trgm ON produto USING gin (nome gin_trgm_ops);
CREATE INDEX idx_produto_nome_latitude_longitude ON produto (nome, latitude, longitude);
CREATE INDEX idx_voto_produto ON voto_usuario_produto (id_produto, voto);
CREATE INDEX idx_voto ON voto_usuario_produto (voto);
CREATE INDEX idx_produto_nome ON produto (nome);

/**************************************************************************************************************/


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
   
/**************************************************************************************************************/

CREATE OR REPLACE FUNCTION haversine(lat1 float8, lon1 float8, lat2 float8, lon2 float8)
RETURNS float8 AS $$
DECLARE
    r float8 := 6371; -- Raio da Terra em km
    d_lat float8 := radians(lat2 - lat1);
    d_lon float8 := radians(lon2 - lon1);
    a float8 := sin(d_lat / 2)^2 + cos(radians(lat1)) * cos(radians(lat2)) * sin(d_lon / 2)^2;
    c float8 := 2 * atan2(sqrt(a), sqrt(1 - a));
BEGIN
    RETURN r * c;
END;
$$ LANGUAGE plpgsql;
