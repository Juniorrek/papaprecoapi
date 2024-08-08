CREATE TABLE usuario (
	id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
	cpf VARCHAR(11) NOT NULL,
	nome VARCHAR(128) NOT NULL
);

CREATE TABLE produto (
	id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
	nome VARCHAR(128) NOT NULL,
	descricao VARCHAR(512) NOT NULL,
	preco NUMERIC(8,2) NOT NULL,
	latitude FLOAT NOT NULL,
	longitude FLOAT NOT NULL
);
INSERT INTO produto (nome, descricao, preco, latitude, longitude) VALUES 
('Banana', 'Pacote com 6 bananas', 4.50, -25.469680, -49.235317),
('Pacote de Açúcar', 'Pacote de açúcar refinado de 1kg', 3.00, -25.469580, -49.235217),
('Pão de Forma', 'Pão de forma integral, 500g', 7.50, -25.469780, -49.235417),
('Leite', 'Leite integral, 1L', 4.00, -25.469480, -49.235117),
('Café', 'Pacote de café moído, 500g', 10.00, -25.469880, -49.235517),
('Arroz', 'Pacote de arroz branco, 5kg', 20.00, -25.469380, -49.235017),
('Feijão', 'Pacote de feijão carioca, 1kg', 8.00, -25.469980, -49.235617),
('Macarrão', 'Pacote de macarrão espaguete, 1kg', 6.00, -25.469280, -49.234917),
('Óleo de Soja', 'Garrafa de óleo de soja, 900ml', 8.50, -25.470080, -49.235717),
('Sal', 'Pacote de sal refinado, 1kg', 2.50, -25.469180, -49.234817);

CREATE TABLE loja (
	id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
	nome VARCHAR(128) NOT NULL,
	endereco VARCHAR(256) NOT NULL
);