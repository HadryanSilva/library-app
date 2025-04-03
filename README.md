# Sistema de Gerenciamento de Biblioteca

## Visão Geral

Uma aplicação desktop Java para o gerenciamento de um acervo de livros, permitindo catalogação, busca, importação e integração com serviços externos. O sistema permite administrar um catálogo completo de livros com informações detalhadas, incluindo sugestões de livros relacionados.

## Tecnologias Utilizadas

- **Java 8**: Linguagem de programação principal
- **Swing**: Framework para a interface gráfica do usuário
- **Hibernate/JPA**: Framework de persistência para a camada de dados
- **PostgreSQL**: Sistema de banco de dados relacional
- **Docker**: Containerização do banco de dados
- **Docker Compose**: Orquestração de containers
- **Maven**: Gerenciamento de dependências e build do projeto
- **Jackson**: Processamento de JSON para integração com APIs
- **Design Patterns**:
    - MVC (Model-View-Controller)
    - Repository Pattern
    - Strategy Pattern (importação de diferentes formatos)
    - Builder Pattern (construção de objetos complexos)
    - Singleton (configuração do banco de dados)

## Funcionalidades Principais

- Cadastro completo de livros com todos os metadados
- Busca por ISBN via API OpenLibrary para preenchimento automático de dados
- Pesquisa avançada por diversos critérios
- Importação de dados a partir de arquivos CSV, XML e texto de largura fixa
- Sugestão de livros relacionados com base em metadados e conteúdo similar
- Validação de formulários para garantir entrada de dados correta

## Requisitos

- JDK 8 ou superior
- Docker e Docker Compose
- Conexão com a internet (para a funcionalidade de busca por ISBN)
- Maven 3.6 ou superior (opcional, se utilizar o wrapper do Maven)

## Instalação e Execução

### 1. Clone o Repositório

```bash
git clone https://github.com/seu-usuario/library-app.git
cd library-app
```

### 2. Inicialize o Banco de Dados com Docker

O projeto inclui um arquivo `compose.yml` que configura automaticamente o PostgreSQL:

```bash
docker-compose up -d
```

Isso iniciará um container PostgreSQL na porta 5432 com as seguintes credenciais:
- **Usuário**: postgres
- **Senha**: postgres
- **Banco de Dados**: postgres
- **Schema**: library_db (criado automaticamente pelo JPA)

### 3. Compile e Execute a Aplicação

Utilizando o Maven Wrapper incluído no projeto:

```bash
# Em sistemas Unix/Linux/macOS
./mvnw clean package
java -jar target/library-app-1.0-SNAPSHOT.jar

# Em sistemas Windows
mvnw.cmd clean package
java -jar target\library-app-1.0-SNAPSHOT.jar
```

Alternativamente, você pode abrir o projeto em uma IDE como IntelliJ IDEA ou Eclipse e executar a classe `br.com.hadryan.app.LibraryApplication`.

## Estrutura do Projeto

```
├── src/main/java/br/com/hadryan/app
│   ├── config              # Configurações da aplicação (JPA, DB)
│   ├── controller          # Controladores (padrão MVC)
│   ├── model
│   │   ├── entity          # Entidades JPA (Livro, Autor, Editora)
│   │   └── repository      # Repositórios para acesso a dados
│   ├── service             # Camada de serviços
│   │   ├── http            # Cliente HTTP para APIs externas
│   │   ├── importacao      # Estratégias de importação
│   │   └── json            # Processamento de JSON
│   ├── util                # Classes utilitárias
│   └── view                # Interface gráfica (Swing)
│       ├── components      # Componentes reutilizáveis
│       │   ├── base        # Classes base abstratas
│       │   ├── dialog      # Diálogos específicos
│       │   └── panel       # Painéis específicos
├── src/main/resources
│   ├── META-INF
│   │   ├── beans.xml       # Configuração CDI
│   │   └── persistence.xml # Configuração JPA/Hibernate
├── compose.yml             # Configuração Docker Compose
└── pom.xml                 # Configuração Maven
```

## Uso da Aplicação

### Tela Principal

A aplicação inicia na tela de lista de livros, com os seguintes painéis principais:

1. **Lista de Livros**: Mostra todos os livros cadastrados
2. **Pesquisa**: Permite buscar livros por diferentes critérios
3. **Importação**: Permite importar livros de arquivos externos

### Cadastro de Livros

1. Na tela de Lista de Livros, clique em "Adicionar Livro"
2. Preencha o ISBN e clique em "Buscar por ISBN" para preencher automaticamente os dados via API
3. Ou preencha os campos manualmente:
    - Título (obrigatório)
    - ISBN (obrigatório)
    - Autores (separados por vírgula)
    - Editora
    - Data de Publicação
    - Livros Similares
4. Clique em "Salvar" para cadastrar o livro

### Busca de Livros

1. Navegue até a tela de Pesquisa
2. Preencha um ou mais critérios de busca:
    - Título
    - ISBN
    - Autor
    - Editora
    - Data de Publicação
3. Clique em "Pesquisar" para ver os resultados
4. Dê duplo clique em um resultado para ver detalhes

### Importação de Arquivos

1. Navegue até a tela de Importação
2. Clique em "Selecionar..." para escolher um arquivo
3. O sistema suporta os seguintes formatos:
    - CSV (valores separados por vírgula)
    - XML (formato estruturado)
    - TXT (texto com largura fixa)
4. Clique em "Importar" para iniciar o processo
5. Aguarde a conclusão e veja o resumo da importação

## Configurações Avançadas

### Alterando a Configuração do Banco de Dados

Para usar um servidor PostgreSQL existente em vez do Docker, edite o arquivo `src/main/resources/META-INF/persistence.xml`:

```xml
<property name="javax.persistence.jdbc.url" value="jdbc:postgresql://seu-servidor:porta/seu-banco?currentSchema=library_db" />
<property name="javax.persistence.jdbc.user" value="seu-usuario" />
<property name="javax.persistence.jdbc.password" value="sua-senha" />
```

### Ajustando o Comportamento da API

Para modificar o timeout ou outras configurações da API OpenLibrary, edite a classe `OpenLibraryService.java`:

```java
this.httpClient.setTimeouts(10000, 10000); // Aumentar para 10 segundos
```

## Solução de Problemas

### Erro de Conexão ao Banco de Dados

1. Verifique se o container Docker está em execução:
   ```bash
   docker ps
   ```
2. Certifique-se de que a porta 5432 não está sendo usada por outro processo
3. Verifique as credenciais no arquivo `persistence.xml`

### Erros na Importação de Arquivos

1. Para arquivos CSV, verifique se o cabeçalho contém os campos esperados: titulo, isbn, autor/autores, editora, data_publicacao
2. Para arquivos XML, certifique-se de que a estrutura tenha elementos `<livro>` com subelementos para os dados
3. Para arquivos de texto com largura fixa, o formato esperado é:
    - Título (colunas 1-50)
    - Autores (colunas 51-100)
    - ISBN (colunas 101-120)
    - Editora (colunas 121-160)
    - Data (colunas 161-180)