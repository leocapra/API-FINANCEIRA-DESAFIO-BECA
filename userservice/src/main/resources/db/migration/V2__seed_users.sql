INSERT INTO users (
    id,
    cpf,
    nome,
    email,
    senha,
    telefone,
    active,
    created_at,
    updated_at
) VALUES (
    1,
    '123.456.789-10',
    'Leonardo',
    'leonardo@teste.com',
    '$2a$12$6ZsbdL5g2dj9q.uI8wceUOz9tylxcFEYg9RKXbZqM1BcSxXqG0QZq',
    '+55 15 98132-3921',
    TRUE,
    NOW(),
    NOW()
)
ON CONFLICT (id) DO NOTHING;
