-- Demo dataset. Not schema, and not loaded unless asked for.
--
-- This file is in classpath:db/seed, which is not one of Flyway's locations by
-- default — application.yaml lists only classpath:db/migration. Local
-- development opts in through FLYWAY_LOCATIONS in .env; a deployed environment
-- leaves it out and never sees any of this. That is the whole reason for the
-- second directory: the alternative, a migration that inserts demo rows, would
-- run everywhere or nowhere.
--
-- Numbered V900 so it always sorts after the schema migrations, with room for
-- V2..V899 of real ones in between.
--
-- **Every account below has the password `demo1234`**, published on purpose so
-- a reviewer can sign in. That is only safe because these rows exist solely on
-- machines where someone deliberately pointed FLYWAY_LOCATIONS at this file.
--
-- Nothing here specifies an id: the columns are GENERATED ALWAYS AS IDENTITY,
-- so the rows are wired together by joining on email and on the shop name
-- rather than by assuming which ids the sequence handed out.

-- ---------------------------------------------------------------------------
-- Shops
-- ---------------------------------------------------------------------------
-- Curitiba, spread across roughly 4 km. Two of them sit on the coordinates the
-- Flutter app falls back to when geolocation is unavailable (map_util.dart and
-- map_lib.dart), so the map has something on it on first run, and all of them
-- are inside the 10 km radius the alert query uses.
--
-- descricao is unique across these rows because the inserts below join on it.

INSERT INTO localizacao (latitude, longitude, descricao) VALUES
    (-25.438255, -49.277965, 'Mercado Central'),
    (-25.441105, -49.276300, 'Supermercado Boa Compra'),
    (-25.443235, -49.280965, 'Hipermercado Vila Velha'),
    (-25.446735, -49.283065, 'Mercado Bairro Novo'),
    (-25.449735, -49.284865, 'Supermercado São Francisco'),
    (-25.452235, -49.286365, 'Mercado Parque Barigui'),
    (-25.455235, -49.288165, 'Supermercado Água Verde'),
    (-25.458162, -49.290000, 'Mercado Batel'),
    (-25.461235, -49.291765, 'Hipermercado Portão'),
    (-25.464235, -49.293565, 'Supermercado Rebouças'),
    (-25.467235, -49.295365, 'Mercado Juvevê'),
    (-25.470235, -49.297165, 'Hipermercado Cabral');

-- ---------------------------------------------------------------------------
-- Users
-- ---------------------------------------------------------------------------
-- The hash is BCrypt, cost 10, of `demo1234` — the same encoder SecurityConfig
-- registers. verificado is true so none of them is stuck behind the e-mail
-- verification code, which would need working SMTP credentials.

INSERT INTO usuario (nome, email, senha, localizacao_id, verificado, fcm_token)
SELECT u.nome, u.email, '$2a$10$T0EI2h8pTRtURmmkNwOwTeBB2oPHglsE6dROdxIPqQO5VrS1g1I3y',
       l.id, TRUE, u.fcm_token
FROM (VALUES
    ('João Silva',     'joao.silva@example.com',     'Mercado Central',            NULL),
    ('Maria Oliveira', 'maria.oliveira@example.com', 'Hipermercado Vila Velha',    NULL),
    ('Carlos Souza',   'carlos.souza@example.com',   'Supermercado São Francisco', NULL),
    -- A placeholder token, so the notification query in AlertaUsuarioRepository
    -- returns rows and can be inspected — it filters on fcm_token IS NOT NULL.
    -- Firebase rejects it if a send is actually attempted, which the API logs
    -- and swallows. Replace it with a token from a real device to test delivery.
    ('Ana Pereira',    'ana.pereira@example.com',    'Supermercado Água Verde',    'demo-fcm-token-not-a-real-device'),
    ('Lucas Lima',     'lucas.lima@example.com',     'Hipermercado Portão',        NULL)
) AS u(nome, email, loja, fcm_token)
JOIN localizacao l ON l.descricao = u.loja;

-- ---------------------------------------------------------------------------
-- Prices
-- ---------------------------------------------------------------------------
-- The point of the app is the same product costing different amounts in
-- different shops, so the staples below are repeated across shops at prices
-- that differ enough to rank. One row per product per shop — the votes further
-- down join on that pair and would multiply if it were not unique.

INSERT INTO produto (nome, descricao, preco, localizacao_id, data_observacao, usuario_id)
SELECT p.nome, p.descricao, p.preco, l.id,
       CURRENT_TIMESTAMP - p.dias_atras * INTERVAL '1 day',
       u.id
FROM (VALUES
    ('Arroz 5kg',    'Arroz branco tipo 1, pacote de 5 kg',  21.90, 'Mercado Central',            'joao.silva@example.com',      1),
    ('Arroz 5kg',    'Arroz branco tipo 1, pacote de 5 kg',  19.49, 'Supermercado Boa Compra',    'maria.oliveira@example.com',  2),
    ('Arroz 5kg',    'Arroz branco tipo 1, pacote de 5 kg',  24.90, 'Hipermercado Vila Velha',    'carlos.souza@example.com',    0),
    ('Arroz 5kg',    'Arroz branco tipo 1, pacote de 5 kg',  20.75, 'Mercado Batel',              'ana.pereira@example.com',     4),
    ('Feijão 1kg',   'Feijão preto, pacote de 1 kg',          8.49, 'Mercado Central',            'joao.silva@example.com',      1),
    ('Feijão 1kg',   'Feijão preto, pacote de 1 kg',          7.29, 'Mercado Bairro Novo',        'lucas.lima@example.com',      3),
    ('Feijão 1kg',   'Feijão preto, pacote de 1 kg',          9.90, 'Hipermercado Cabral',        'maria.oliveira@example.com',  2),
    ('Leite 1L',     'Leite integral, caixa de 1 L',          4.29, 'Supermercado Boa Compra',    'maria.oliveira@example.com',  0),
    ('Leite 1L',     'Leite integral, caixa de 1 L',          3.79, 'Supermercado Água Verde',    'ana.pereira@example.com',     1),
    ('Leite 1L',     'Leite integral, caixa de 1 L',          5.10, 'Mercado Juvevê',             'carlos.souza@example.com',    5),
    ('Café 500g',    'Café torrado e moído, pacote de 500 g', 14.90, 'Mercado Central',           'joao.silva@example.com',      2),
    ('Café 500g',    'Café torrado e moído, pacote de 500 g',  9.99, 'Mercado Parque Barigui',    'lucas.lima@example.com',      1),
    ('Café 500g',    'Café torrado e moído, pacote de 500 g', 12.50, 'Supermercado Rebouças',     'ana.pereira@example.com',     6),
    ('Açúcar 1kg',   'Açúcar cristal, pacote de 1 kg',         4.99, 'Hipermercado Vila Velha',   'carlos.souza@example.com',    2),
    ('Açúcar 1kg',   'Açúcar cristal, pacote de 1 kg',         3.89, 'Mercado Bairro Novo',       'lucas.lima@example.com',      0),
    ('Óleo 900ml',   'Óleo de soja, garrafa de 900 ml',        7.49, 'Mercado Central',           'joao.silva@example.com',      3),
    ('Óleo 900ml',   'Óleo de soja, garrafa de 900 ml',        6.29, 'Supermercado São Francisco','carlos.souza@example.com',    1),
    ('Óleo 900ml',   'Óleo de soja, garrafa de 900 ml',        8.90, 'Hipermercado Portão',       'lucas.lima@example.com',      4),
    ('Macarrão 500g','Macarrão parafuso, pacote de 500 g',     4.19, 'Supermercado Boa Compra',   'maria.oliveira@example.com',  1),
    ('Macarrão 500g','Macarrão parafuso, pacote de 500 g',     3.49, 'Mercado Batel',             'ana.pereira@example.com',     2),
    ('Pão de Forma', 'Pão de forma integral, 500 g',           8.90, 'Supermercado Água Verde',   'ana.pereira@example.com',     0),
    ('Pão de Forma', 'Pão de forma integral, 500 g',           7.45, 'Mercado Juvevê',            'carlos.souza@example.com',    3),
    ('Manteiga 200g','Manteiga com sal, pote de 200 g',       11.90, 'Mercado Central',           'joao.silva@example.com',      5),
    ('Manteiga 200g','Manteiga com sal, pote de 200 g',        9.75, 'Hipermercado Cabral',       'lucas.lima@example.com',      1),
    ('Queijo 300g',  'Queijo mussarela fatiado, 300 g',       16.90, 'Hipermercado Vila Velha',   'maria.oliveira@example.com',  2),
    ('Queijo 300g',  'Queijo mussarela fatiado, 300 g',       14.20, 'Supermercado Rebouças',     'ana.pereira@example.com',     4),
    ('Banana 1kg',   'Banana prata, 1 kg',                     5.49, 'Mercado Parque Barigui',    'lucas.lima@example.com',      0),
    ('Banana 1kg',   'Banana prata, 1 kg',                     4.29, 'Mercado Bairro Novo',       'joao.silva@example.com',      1),
    ('Tomate 1kg',   'Tomate italiano, 1 kg',                  8.99, 'Supermercado São Francisco','carlos.souza@example.com',    2),
    ('Tomate 1kg',   'Tomate italiano, 1 kg',                  6.49, 'Mercado Batel',             'ana.pereira@example.com',     1),
    ('Batata 1kg',   'Batata inglesa, 1 kg',                   5.99, 'Hipermercado Portão',       'lucas.lima@example.com',      3),
    ('Batata 1kg',   'Batata inglesa, 1 kg',                   4.79, 'Supermercado Boa Compra',   'maria.oliveira@example.com',  0),
    ('Sabão em Pó 1kg', 'Sabão em pó, caixa de 1 kg',         13.90, 'Mercado Juvevê',            'carlos.souza@example.com',    6),
    ('Sabão em Pó 1kg', 'Sabão em pó, caixa de 1 kg',         11.49, 'Supermercado Água Verde',   'ana.pereira@example.com',     2),
    ('Papel Higiênico 12un', 'Papel higiênico folha dupla, 12 rolos', 24.90, 'Hipermercado Cabral', 'maria.oliveira@example.com', 1),
    ('Papel Higiênico 12un', 'Papel higiênico folha dupla, 12 rolos', 21.50, 'Mercado Central',     'joao.silva@example.com',     4)
) AS p(nome, descricao, preco, loja, email, dias_atras)
JOIN localizacao l ON l.descricao = p.loja
JOIN usuario u ON u.email = p.email;

-- ---------------------------------------------------------------------------
-- Votes
-- ---------------------------------------------------------------------------
-- TRUE confirms a price, FALSE disputes it. The disputed rows matter: the
-- produtos_sem_voto_negativo view drops any product with a single FALSE vote,
-- so without a few of these that view is indistinguishable from the table and
-- the ranking query has nothing to rank on.

INSERT INTO voto_usuario_produto (id_usuario, id_produto, voto)
SELECT u.id, pr.id, v.voto
FROM (VALUES
    ('maria.oliveira@example.com', 'Arroz 5kg',    'Mercado Central',            TRUE),
    ('carlos.souza@example.com',   'Arroz 5kg',    'Mercado Central',            TRUE),
    ('ana.pereira@example.com',    'Arroz 5kg',    'Mercado Central',            TRUE),
    ('joao.silva@example.com',     'Arroz 5kg',    'Supermercado Boa Compra',    TRUE),
    ('lucas.lima@example.com',     'Arroz 5kg',    'Supermercado Boa Compra',    TRUE),
    -- The dearest arroz, disputed twice: it drops out of the view entirely.
    ('joao.silva@example.com',     'Arroz 5kg',    'Hipermercado Vila Velha',    FALSE),
    ('ana.pereira@example.com',    'Arroz 5kg',    'Hipermercado Vila Velha',    FALSE),
    ('joao.silva@example.com',     'Café 500g',    'Mercado Parque Barigui',     TRUE),
    ('maria.oliveira@example.com', 'Café 500g',    'Mercado Parque Barigui',     TRUE),
    ('ana.pereira@example.com',    'Café 500g',    'Mercado Central',            FALSE),
    ('lucas.lima@example.com',     'Leite 1L',     'Supermercado Água Verde',    TRUE),
    ('carlos.souza@example.com',   'Leite 1L',     'Supermercado Água Verde',    TRUE),
    ('joao.silva@example.com',     'Leite 1L',     'Mercado Juvevê',             FALSE),
    ('maria.oliveira@example.com', 'Feijão 1kg',   'Mercado Bairro Novo',        TRUE),
    ('ana.pereira@example.com',    'Feijão 1kg',   'Mercado Bairro Novo',        TRUE),
    ('lucas.lima@example.com',     'Feijão 1kg',   'Hipermercado Cabral',        FALSE),
    ('carlos.souza@example.com',   'Óleo 900ml',   'Supermercado São Francisco', TRUE),
    ('lucas.lima@example.com',     'Tomate 1kg',   'Mercado Batel',              TRUE),
    ('joao.silva@example.com',     'Queijo 300g',  'Supermercado Rebouças',      TRUE),
    ('maria.oliveira@example.com', 'Banana 1kg',   'Mercado Bairro Novo',        TRUE)
) AS v(email, produto, loja, voto)
JOIN usuario u ON u.email = v.email
JOIN localizacao l ON l.descricao = v.loja
JOIN produto pr ON pr.nome = v.produto AND pr.localizacao_id = l.id;

-- ---------------------------------------------------------------------------
-- Price history
-- ---------------------------------------------------------------------------
-- Older observations of prices already listed above, so
-- buscarHistoricoProdutoRanking has more than one row per shop to return.
-- Inserted after the votes so the (nome, shop) pairs those joins rely on were
-- still unique when they ran.

INSERT INTO produto (nome, descricao, preco, localizacao_id, data_observacao, usuario_id)
SELECT p.nome, p.descricao, p.preco, l.id,
       CURRENT_TIMESTAMP - p.dias_atras * INTERVAL '1 day',
       u.id
FROM (VALUES
    ('Arroz 5kg',  'Arroz branco tipo 1, pacote de 5 kg',  23.90, 'Mercado Central',         'maria.oliveira@example.com', 12),
    ('Arroz 5kg',  'Arroz branco tipo 1, pacote de 5 kg',  22.50, 'Mercado Central',         'carlos.souza@example.com',    8),
    ('Arroz 5kg',  'Arroz branco tipo 1, pacote de 5 kg',  20.90, 'Supermercado Boa Compra', 'lucas.lima@example.com',     10),
    ('Café 500g',  'Café torrado e moído, pacote de 500 g', 11.90, 'Mercado Parque Barigui', 'joao.silva@example.com',      9),
    ('Café 500g',  'Café torrado e moído, pacote de 500 g', 13.49, 'Mercado Parque Barigui', 'ana.pereira@example.com',    15),
    ('Leite 1L',   'Leite integral, caixa de 1 L',           4.09, 'Supermercado Água Verde','maria.oliveira@example.com',  7),
    ('Leite 1L',   'Leite integral, caixa de 1 L',           4.49, 'Supermercado Água Verde','lucas.lima@example.com',     14)
) AS p(nome, descricao, preco, loja, email, dias_atras)
JOIN localizacao l ON l.descricao = p.loja
JOIN usuario u ON u.email = p.email;

-- ---------------------------------------------------------------------------
-- Price alerts
-- ---------------------------------------------------------------------------
-- Thresholds set above a price that actually exists nearby, so the
-- notification query has something to match. It joins on
-- similarity(alerta.produto, produto.nome) > 0.3, which is why the wording does
-- not have to be exact — 'Café' finds 'Café 500g'.

INSERT INTO alerta_usuario (produto, preco, usuario_id)
SELECT a.produto, a.preco, u.id
FROM (VALUES
    ('Café 500g',  12.00, 'ana.pereira@example.com'),
    ('Arroz 5kg',  20.00, 'joao.silva@example.com'),
    ('Leite 1L',    4.00, 'lucas.lima@example.com'),
    ('Feijão 1kg',  8.00, 'maria.oliveira@example.com'),
    ('Óleo 900ml',  7.00, 'carlos.souza@example.com')
) AS a(produto, preco, email)
JOIN usuario u ON u.email = a.email;
