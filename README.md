# Projeto Itakua Marmoraria

Sistema de gerenciamento de orçamentos para marmorarias desenvolvido com Spring Boot, PostgreSQL e Frontend Web.

## Sobre o Projeto

O Projeto Itakua Marmoraria foi desenvolvido para auxiliar no gerenciamento de clientes, materiais e orçamentos de uma marmoraria.

O sistema permite:

* Cadastro de clientes
* Cadastro de materiais
* Cadastro de orçamentos
* Controle de itens do orçamento
* Geração de PDF para impressão de pedidos
* Integração entre frontend e backend através de API REST

---

## Tecnologias Utilizadas

### Backend

* Java 21
* Spring Boot
* Spring Data JPA
* PostgreSQL
* Maven
* OpenHTMLToPDF

### Frontend

* HTML5
* CSS3
* JavaScript

### Banco de Dados

* PostgreSQL

---

## Estrutura do Projeto

```text
PROJETO-ITAKUA-FULL
│
├── itakua-backend
│   ├── src
│   ├── pom.xml
│   ├── mvnw
│   └── mvnw.cmd
│
├── itakua-front-end
│   ├── html
│   ├── css
│   └── script
│
└── README.md
```

---

## Configuração do Banco de Dados

Crie um banco PostgreSQL:

```sql
CREATE DATABASE banco_itakua;
```

Crie o arquivo:

```text
src/main/resources/application.properties
```

Utilize a seguinte configuração:

```properties
spring.application.name=itakua-backend

spring.datasource.url=jdbc:postgresql://localhost:5432/banco_itakua
spring.datasource.username=SEU_USUARIO
spring.datasource.password=SUA_SENHA

spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.generate-ddl=true
spring.jpa.hibernate.ddl-auto=update
```

---

## Executando o Backend

Acesse a pasta:

```bash
cd itakua-backend
```

Execute:

```bash
mvn spring-boot:run
```

Ou utilize a execução diretamente pela IDE.

Servidor padrão:

```text
http://localhost:8080
```

---

## Executando o Frontend

Abra o arquivo:

```text
itakua-front-end/html/index.html
```

ou utilize uma extensão como Live Server no VS Code.

---

## Funcionalidades

### Clientes

* Cadastro
* Listagem
* Edição
* Exclusão

### Materiais

* Cadastro
* Listagem
* Atualização
* Exclusão

### Orçamentos

* Criação de orçamento
* Inclusão de itens
* Cálculo de valores
* Controle de status

### PDF

* Geração de pedido para impressão
* Layout personalizado utilizando HTML Template

---

## Autor

Desenvolvido por Kauã Fonseca Rocha.

Projeto acadêmico desenvolvido para estudos de Java, Spring Boot, APIs REST e desenvolvimento Full Stack.
