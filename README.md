# 📘 README — Projeto de Microsserviços

## 🧩 Diagrama de Arquitetura

![Diagrama de Arquitetura](./img.png)

O diagrama acima representa o fluxo completo do sistema, envolvendo:
- Autenticação e gestão de usuários (MS1)
- Registro e consulta de transações (MS2)
- Processamento assíncrono de transações (MS3)
- Comunicação via Kafka
- Integração com BrasilAPI (câmbio)
- Simulação de carteira digital via MockAPI (apenas BRL)

---

## 🚀 Como rodar o projeto

1. Abra o **Git Bash** na raiz do projeto  
   (diretório onde está o `docker-compose.yml`)

2. Suba os containers:
```bash
docker compose up -d
```

3. Verifique se todos os serviços subiram corretamente.

### 📌 Acessos aos serviços
- **MS1 (Usuários)** → http://localhost:8080/swagger-ui/index.html  
- **MS2 (Transações)** → http://localhost:8081/swagger-ui/index.html  

---

## ⚠️ Observações importantes

- Para o projeto funcionar corretamente, o arquivo **`.env` foi intencionalmente versionado** neste repositório.
- Entendo totalmente a preocupação de segurança e **garanto que isso não ocorreria em projetos reais com clientes**.
- Recomendo configurar sua **MockAPI no arquivo `.env`**, evitando falhas caso a API pública esteja indisponível ou sobrecarregada.

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

📌 Observações:
- A senha está criptografada no banco.
- Todas as senhas criptografadas presentes no Excel de importação correspondem à senha:
```
Senha@123
```

---

## 🔐 Permissões (Admin x Usuário comum)

Com o **token de ADMIN**, é possível:

- Listar usuários (exibe CPF)
- Atualizar outros usuários
- Deletar outros usuários
- Importar usuários em lote
- Detalhar usuários que não sejam você mesmo

Usuários comuns:
- Não visualizam CPF
- Não podem alterar, deletar ou listar outros usuários
- Não podem importar usuários

---

## 🧪 Fluxo recomendado de testes

### 1️⃣ Criar usuário comum
Endpoint:
```
POST /usuarios/criar
```

Payload de exemplo:
```json
{
  "cpf": "127.322.055-24",
  "nome": "Joaozinho das Flores",
  "email": "joaozinho@dasflores.com",
  "senha": "Senha@123",
  "telefone": "+55 15 99193-9234"
}
```

> 💡 Teste livremente as validações dos campos.

---

### 2️⃣ Papéis do sistema
- `ROLE_ADMIN` → apenas o usuário gerado pelo Flyway
- `ROLE_USER` → todos os usuários criados via endpoint

Recomendação:
- Faça login como **usuário comum** para observar as limitações
- Depois faça login como **admin** para ver a diferença de permissões

---

### 3️⃣ Importação de usuários
Endpoint:
```
POST /usuarios/importar
```

Na raiz do projeto existe o arquivo:
```
lote-usuario.zip
```

- O arquivo já segue o padrão esperado de importação
- Extraia e envie o XLSX pelo endpoint

📌 Observação importante:
- Upload de XLSX pode ser limitado no Swagger
- Foi disponibilizado também um **export do Insomnia**, que pode ser importado no Postman ou Insomnia

🧪 Testes esperados:
- Usuário comum → **403 Forbidden**
- Usuário admin → **OK**

---

### 4️⃣ Listar usuários (paginação obrigatória)

Corpo obrigatório:
```json
{
  "page": 0,
  "size": 5
}
```

📌 Resultado esperado:
- Token de usuário comum → **403 Forbidden**
- Token de admin → **OK**

---

## 🔁 MS2 — Transações

Antes de testar o MS2, acesse o **Kafka UI**:
```
http://localhost:9090
```

---

## ⚠️ Regras críticas

- O **userId nunca vem no corpo**
- Sempre é extraído do **JWT**
- O comportamento muda conforme o papel do usuário

---

## 💰 Tipos de transações

### ➕ Depósito
```json
{
  "amount": 1230,
  "currency": "BRL"
}
```

Atributo opcional:
```json
{
  "amount": 1230,
  "currency": "BRL",
  "record": true
}
```

📌 `record = true`:
- Registra no banco
- Status automático: **APROVADO**
- Não processa MockAPI
- Ainda passa pelo MS3 para câmbio

---

### ➖ Saque
```json
{
  "amount": 2,
  "currency": "BRL"
}
```

Ou:
```json
{
  "amount": 2,
  "currency": "BRL",
  "record": true
}
```

---

### 🔁 Transferência
```json
{
  "amount": 3,
  "currency": "BRL",
  "targetAccountId": "4304336a-3630-44b1-867d-447756af43ec",
  "transferType": "PIX"
}
```

Tipos:
- PIX
- TED
- DOC
- TEF

---

### 🛒 Compra
```json
{
  "amount": 99,
  "currency": "BRL",
  "buyType": "CREDITO",
  "category": "MERCADO",
  "description": "Compras do mês"
}
```

Tipos de compra:
- DEBITO
- CREDITO
- PIX
- CEDULA

---

## ❌ Cancelamento de transação

- Usuário comum → apenas próprias transações
- Admin → qualquer usuário

ID enviado via **path param**

---

## 📄 Listar transações

Usuário comum:
- Apenas transações vinculadas a ele

Admin:
- Pode listar transações de outros usuários

Filtro:
```json
{
  "userId": "7085d32f-0979-499b-95a9-6dcae0c40da5",
  "status": null,
  "type": null,
  "startCreatedAt": "2026-02-01T17:00:00",
  "endCreatedAt": "2026-02-01T18:00:00"
}
```

---

## 📤 Exportação de transações (PDF)

```json
{
  "userId": "7085d32f-0979-499b-95a9-6dcae0c40da5",
  "status": null,
  "type": null,
  "startCreatedAt": null,
  "endCreatedAt": null
}
```

---

## 🧠 Autoavaliação do projeto

- Cobertura de testes unitários abaixo do ideal
- Prioridade foi entregar funcionalidades essenciais dentro do prazo
- Bug conhecido:
  - `record = false` tratado como diferente de `null`
- Lógica de compra `CEDULA` não implementada
  - Ideal exigir `record = true`

---

## 🙏 Considerações finais

Agradeço a oportunidade de desenvolver este projeto.  
Foi extremamente enriquecedor em termos de arquitetura, mensageria e boas práticas.
