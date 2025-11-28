# MS Pix Wallet

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.8-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Available-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Flyway](https://img.shields.io/badge/Flyway-Migration-CC0200?style=for-the-badge&logo=flyway&logoColor=white)

Este projeto é um microsserviço de carteira digital com suporte a transferências Pix, desenvolvido com foco em qualidade de código, arquitetura limpa e boas práticas de engenharia de software.

---

## 🏛️ Arquitetura

O projeto segue os princípios da **Clean Architecture** (Arquitetura Limpa), garantindo a independência de frameworks, testabilidade e separação de responsabilidades.

A estrutura de pacotes reflete as camadas da arquitetura:

- **domain**: Contém as entidades, objetos de valor, exceções de domínio e interfaces de repositório. É o núcleo da aplicação e não depende de nenhuma outra camada.
- **application**: Contém os casos de uso (Use Cases) que orquestram a lógica de negócios, implementando as regras da aplicação.
- **infrastructure**: Contém as implementações concretas de repositórios, configurações de banco de dados, clientes externos e outros detalhes de infraestrutura.
- **presentation**: Contém os controladores REST (Controllers) e DTOs de entrada/saída, responsáveis por expor a API para o mundo externo.

## 🚀 Tecnologias Utilizadas

- **Java 21**: Linguagem de programação moderna e robusta.
- **Spring Boot 3.5.8**: Framework para criação de aplicações Java produtivas.
- **PostgreSQL**: Banco de dados relacional robusto e confiável.
- **Flyway**: Ferramenta de migração de banco de dados para controle de versão do esquema.
- **Docker & Docker Compose**: Para containerização e orquestração do ambiente de desenvolvimento.
- **JUnit 5 & Mockito**: Para testes unitários e de integração abrangentes.
- **SpringDoc OpenAPI (Swagger)**: Para documentação viva e interativa da API.

## 🛠️ Como Executar

### Pré-requisitos

- **Docker** e **Docker Compose** instalados.
- **Java 21** (Opcional, caso queira rodar fora do Docker).
- **Maven** (Opcional, o projeto inclui o `mvnw`).

### Rodando com Docker Compose (Recomendado)

O projeto já está configurado com um `docker-compose.yml` que sobe tanto a aplicação quanto o banco de dados PostgreSQL.

1.  Clone o repositório.
2.  Na raiz do projeto, execute:

```bash
docker-compose up -d --build
```

Isso irá:

- Compilar a aplicação.
- Criar a imagem Docker.
- Iniciar o container do PostgreSQL.
- Iniciar o container da aplicação `ms-pix-wallet` na porta `8080`.

### Rodando Localmente (Desenvolvimento)

Caso prefira rodar a aplicação localmente (ex: na IDE) e apenas o banco no Docker:

1.  Suba o banco de dados:
    ```bash
    docker-compose up -d postgres
    ```
2.  Execute a aplicação via Maven Wrapper:
    ```bash
    ./mvnw spring-boot:run
    ```

## 📚 Documentação da API (Swagger)

A API é documentada utilizando o padrão OpenAPI 3. Após iniciar a aplicação, você pode acessar a interface interativa do Swagger UI no seguinte endereço:

👉 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

Lá você poderá explorar todos os endpoints disponíveis, ver os esquemas de requisição e resposta, e testar as chamadas diretamente pelo navegador.

## ✅ Testes

O projeto possui uma suíte abrangente de testes unitários e de integração. Para executá-los:

```bash
./mvnw test
```

---

Desenvolvido com 💙 por Valdeir Nascimento.
