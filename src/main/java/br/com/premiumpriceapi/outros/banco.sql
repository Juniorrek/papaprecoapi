CREATE TABLE usuario (
    id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    nome VARCHAR(128) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL
);
INSERT INTO usuario (nome, email, senha) VALUES 
('João Silva', 'joao.silva@example.com', '$2a$10$7uV3eBlXeQwoRCz.vR2UjuU1EIEuABk4zIzl2FTSHKKVRrh3a23v2'),
('Maria Oliveira', 'maria.oliveira@example.com', '$2a$10$8qMntoRTpHExdpXyER5cyOlBGwrGUbJJznbF1Kak49pq6QgCJGYP2'),
('Carlos Souza', 'carlos.souza@example.com', '$2a$10$5D25DY4ZtGaxTC2IAVaQYu5HCOUxo.YoI9q/SafI/w5QXyovD8Z8O'),
('Ana Pereira', 'ana.pereira@example.com', '$2a$10$Qft8ICsxyKHJlzPV4tW.7uCRn3yR.9A1kdRdtOkA/O5FdRnYV4I6m'),
('Lucas Lima', 'lucas.lima@example.com', '$2a$10$1f1iwcnlO8yRcUe4PLsK1u2MzwEdI6AA9vl5qRj2sqkfj2/Qc1WE6');


CREATE EXTENSION pg_trgm;  -- Ativar a extensão de trigramas
CREATE EXTENSION fuzzystrmatch;

CREATE INDEX idx_produto_nome_trgm ON produto USING gin (nome gin_trgm_ops);
CREATE INDEX idx_produto_nome_latitude_longitude ON produto (nome, latitude, longitude);
CREATE INDEX idx_voto_produto ON voto_usuario_produto (id_produto, voto);
CREATE INDEX idx_voto ON voto_usuario_produto (voto);
CREATE INDEX idx_produto_nome ON produto (nome);

CREATE TABLE produto (
	id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
	nome VARCHAR(128) NOT NULL,
	descricao VARCHAR(512),
	preco NUMERIC(8,2) NOT NULL,
	latitude FLOAT NOT NULL,
	longitude FLOAT NOT NULL,
	data_insercao TIMESTAMP DEFAULT DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO produto (nome, preco, latitude, longitude) VALUES 
('Banana', 4.50, -25.469680, -49.235317),
('Banana', 5.00, -25.469680, -49.235317),
('Banana', 5.50, -25.469680, -49.235317),
('Banana', 4.80, -25.469685, -49.235317),
('Pacote de Açúcar', 3.00, -25.469580, -49.235217),
('Pão de Forma', 7.50, -25.469780, -49.235417),
('Leite', 4.00, -25.469480, -49.235117),
('Café', 10.00, -25.469880, -49.235517),
('Arroz', 20.00, -25.469380, -49.235017),
('Feijão', 8.00, -25.469980, -49.235617),
('Macarrão', 6.00, -25.469280, -49.234917),
('Óleo de Soja', 8.50, -25.470080, -49.235717),
('Sal', 2.50, -25.469180, -49.234817);

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

CREATE TABLE loja (
	id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
	nome VARCHAR(128) NOT NULL,
	endereco VARCHAR(256) NOT NULL
);

CREATE TABLE redefinir_senha_token (
    id INTEGER PRIMARY KEY  GENERATED ALWAYS AS IDENTITY,
    token VARCHAR(255) NOT NULL,
    usuario_id INTEGER NOT NULL,
    data_validade TIMESTAMP NOT NULL,
    CONSTRAINT fk_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);
