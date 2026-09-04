## Zimple

A Retrieval-Augmented Generation (RAG) backend built with **Spring AI**, for answering questions over Markdown (`.md`) documents.

#### Tech Stack

- **Java 17** + **Spring Boot** (Spring AI `2.0.1`)
- **Ollama** — cloud LLM for chat + local for embeddings
- **PostgreSQL + pgvector** — vector store for document embeddings
- **Redis** — caching / chat memory support
- **springdoc-openapi** — Swagger UI for API docs
- `frontend/` — a simple UI for interacting with the RAG API

#### Prerequisites

- A cloud ollama API, [GET IT](https://ollama.com/settings/keys)
- A [Ollama](https://ollama.com) running locally with your chosen model pulled (for embedding)
- Update `src/main/resources/application.properties` to set your Ollama model name,  and any other environment-specific values before running.

#### Getting Started

1. **Start Postgres + Redis**
   ```bash
   docker compose up -d
   ```

2. **Run the backend**
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Explore the API**
   Once running, open the Swagger UI (check `springdoc` path, default `/swagger-ui.html`) to view and test the available endpoints.

4. **(Optional) Run the frontend**
   See `frontend/` for setup instructions.
