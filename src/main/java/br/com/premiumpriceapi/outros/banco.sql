CREATE TABLE usuario (
	id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
	cpf VARCHAR(11) NOT NULL,
	nome VARCHAR(128) NOT NULL
);

CREATE TABLE produto (
	id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
	nome VARCHAR(128) NOT NULL,
	descricao VARCHAR(512) NOT NULL,
	preco NUMERIC(8,2) NOT NULL
);
INSERT INTO produto (nome, descricao, preco) VALUES
('Banana', 'Fruta amarela e rica em potássio.', 2.50),
('Leite', 'Leite integral de 1 litro.', 4.30),
('Arroz', 'Arroz branco tipo 1 - pacote de 5kg.', 20.90),
('Feijão', 'Feijão carioca - pacote de 1kg.', 8.75),
('Pão', 'Pão francês - unidade.', 0.75),
('Café', 'Café torrado e moído - pacote de 500g.', 10.90),
('Açúcar', 'Açúcar refinado - pacote de 1kg.', 4.50),
('Óleo de Soja', 'Óleo de soja - garrafa de 900ml.', 6.80),
('Sal', 'Sal refinado - pacote de 1kg.', 2.20),
('Macarrão', 'Macarrão espaguete - pacote de 500g.', 3.60);

CREATE TABLE loja (
	id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
	nome VARCHAR(128) NOT NULL,
	endereco VARCHAR(256) NOT NULL
);