# 📘 README — Projeto de Microsserviços

## 🧩 Diagrama de Arquitetura

![Diagrama de Arquitetura](./img.png)

## 🚀 Como rodar o projeto

1. Abra o **Git Bash** na raiz do projeto  
   (diretório onde está o `docker-compose.yml`)

2. Suba os containers:
```bash
docker compose up -d
```

3. Verifique se todos os serviços subiram corretamente.

### 📌 Acessos aos serviços
- **MS1** → http://localhost:8080/swagger-ui/index.html  
- **MS2** → http://localhost:8081/swagger-ui/index.html  

---

## ⚠️ Observações importantes

- Para o projeto funcionar corretamente, o arquivo **`.env` foi intencionalmente versionado** neste repositório.
- Entendo totalmente a preocupação com segurança e **garanto que isso não aconteceria em projetos com clientes reais**.
- Recomendo fortemente configurar sua **MockAPI no `.env`**, evitando problemas caso a API pública esteja sobrecarregada.

---

## 👤 Conta Admin (gerada automaticamente)

O **Flyway** cria automaticamente um usuário administrador ao subir o projeto.

### Credenciais:
```json
{
  "login": "leonardo@teste.com",
  "senha": "Senha@123"
}
```

📌 Todas as senhas criptografadas presentes no Excel de importação correspondem à senha:
Senha@123

---

## 🔐 Permissões (Admin x Usuário comum)

Com o **token de ADMIN**, é possível:
- Listar usuários (com CPF visível)
- Atualizar outros usuários
- Deletar outros usuários
- Importar usuários em lote
- Detalhar usuários que não sejam você mesmo

Usuários comuns **não têm acesso** a essas ações.

---

## 🧪 Fluxo recomendado de testes

### Criar usuário comum
Endpoint:
POST /usuarios/criar

```json
{
  "cpf": "127.322.055-24",
  "nome": "Joaozinho das Flores",
  "email": "joaozinho@dasflores.com",
  "senha": "Senha@123",
  "telefone": "+55 15 99193-9234"
}
```

---

## 🔁 MS2 — Transações

Antes de testar o MS2, acesse o Kafka UI:
http://localhost:9090

---

## 🙏 Considerações finais

Agradeço a oportunidade de desenvolver este projeto.  
Foi extremamente enriquecedor em termos de aprendizado técnico.
