INSERT INTO users (id, cpf, roles, nome, email, senha, telefone, active, created_at, updated_at)
VALUES (
  '25c45a2a-adaa-4e75-bbf0-85495cb8a809',
  '123.456.789-10',
  'ROLE_ADMIN',
  'Leonardo',
  'leonardo@teste.com',
  '$2a$12$6ZsbdL5g2dj9q.uI8wceUOz9tylxcFEYg9RKXbZqM1BcSxXqG0QZq',
  '+55 15 98132-3921',
  TRUE,
  NOW(),
  NOW()
)
ON CONFLICT (email) DO NOTHING;
