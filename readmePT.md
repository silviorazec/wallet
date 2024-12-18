
# Worker Wallet Project

## Visão Geral

Este projeto consiste em uma aplicação distribuída desenvolvida com **Spring Boot**, **Kafka** e **Keycloak**. Seu objetivo principal é gerenciar transações financeiras de maneira segura, com suporte para notificações e armazenamento em banco de dados relacional.

A arquitetura é orientada por eventos, onde as transações são processadas de forma assíncrona usando o **Apache Kafka** e serialização Avro para garantir a consistência e a escalabilidade.

---

## Estrutura do Projeto

### Módulos
- **API Wallet**: Interface REST que expõe endpoints para gerenciar transações e comunicação com o Keycloak.
- **Worker Wallet**: Serviço responsável por consumir mensagens de transações do Kafka e processá-las.
- **Keycloak**: Serviço de autenticação e autorização baseado em OAuth2 e OpenID Connect.

### Dependências Principais
- **Spring Kafka**: Para produção e consumo de eventos no Kafka.
- **Avro**: Para serialização de mensagens no formato binário.
- **Keycloak**: Para autenticação e gerenciamento de permissões.
- **PostgreSQL**: Banco de dados para armazenamento de informações.

---

## Como Configurar o Projeto

### Pré-requisitos
- Docker e Docker Compose instalados
- Maven instalado para compilar o projeto localmente
- JDK 17+

### Construção e Execução

1. **Configuração do Ambiente**:
   Certifique-se de que o arquivo `.docker/.env` está preenchido com as seguintes variáveis:
   ```bash
   API_IMAGE_NAME=api-wallet
   API_IMAGE_VERSION=latest
   WORKER_IMAGE_NAME=worker-wallet
   WORKER_IMAGE_VERSION=latest`` 

2.  **Subindo o projeto**: Execute o seguinte comando na raiz do projeto:
    
     ./build-and-run.sh 
 
----------

## Serviços Disponíveis

### API Wallet

-   Porta padrão: **8084**
-   Endpoints principais:
	 
	 -  POST /v1/wallets/withdraws : cria uma carteira.
	 -  `GET /v1//wallets/balance`: Retorna o saldo da carteira
	 - `GET /v1//wallets/balance?dateHistory`: Retorna o saldo da carteira em um dia específico
    -   `POST /v1/transactions/transfers`: Cria uma transferência entre carteiras.
    -   `POST /v1/transactions/deposits`: Adiciona saldo à carteira.
    -   `POST /v1/transactions/withdraws`: Realiza um saque da carteira.
    -   `GET /v1/transactions/`: Retorna todas as transações associadas ao usuário autenticado.
   

### Worker Wallet

-   Porta padrão: **8085**
-   Consome mensagens do Kafka no tópico de transações para processar eventos de maneira assíncrona.

### Keycloak

-   Porta padrão: **8080**
-   Gerencia autenticação e autorização dos usuários e serviços.

----------

## Estrutura de Mensagens Avro

### Transação

`{
  "namespace": "com.recargapay.code.assessment.topics",
  "type": "record",
  "name": "Transaction",
  "fields": [
    { "name": "amount", "type": { "type": "bytes", "logicalType": "decimal", "precision": 20, "scale": 2 } },
    { "name": "relatedWalletId", "type": ["null", { "type": "string", "logicalType": "uuid" }], "default": null },
    { "name": "createdAt", "type": { "type": "long", "logicalType": "timestamp-millis" } },
    { "name": "walletId", "type": ["null", { "type": "string", "logicalType": "uuid" }], "default": null },
    { "name": "typeTransaction", "type": { "type": "string" } }
  ]
}` 

### Notificação


`{
  "type": "record",
  "name": "Notification",
  "namespace": "com.recargapay.code.assessment.topics",
  "fields": [
    { "name": "message", "type": "string" },
    { "name": "to", "type": "string" }
  ]
}` 

----------

## Banco de Dados

### Configuração

-   **PostgreSQL**
    -   Porta padrão: **5432**
    -   Credenciais padrão:
        -   **Usuário**: postgres
        -   **Senha**: Postgres2022!

### Inicialização

Scripts de criação e população:

-   `ddl.sql`: Criação de tabelas.
-   `dml.sql`: Dados iniciais para testes.

----------

## Keycloak

### Informações

-   **Admin Console**: `http://localhost:8080/admin`
-   **Usuário Administrador**: admin
-   **Senha**: admin

### Usuários de Teste

-   `pedro.costa`: Dono de uma carteira.
-   `maria.melo`: Dono de uma carteira.

----------

## Logs e Monitoramento

### Grafana

-   URL: `http://localhost:3000`
-   Monitoramento de métricas de aplicação.

### Loki

-   Integração com Grafana para logs.