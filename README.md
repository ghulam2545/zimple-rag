### Zimple

Zimple is a Retrieval-Augmented Generation (RAG) application built with Spring AI for chatting with uploaded Markdown (`.md`) documents. It uses Ollama Cloud for chat, local Ollama for embeddings, PostgreSQL with pgvector for vector search, and Redis for chat cache.

#### How to Run

1. **Create `.env` in the project root**

   ```env
   OLLAMA_API_KEY=your_ollama_api_key
   ```

   Get your key from [Ollama](https://ollama.com/settings/keys).

2. **Start dependencies**

   ```bash
   docker compose up -d
   ```

   Make sure local Ollama is running with `embeddinggemma:300m`.

3. **Run the backend**

   ```bash
   ./mvnw spring-boot:run
   ```

4. **Open Swagger UI**

   ```
   http://localhost:8080/backend/api/v1/swagger-ui/index.html
   ```

5. **Optional: Run the frontend**

   See [`frontend/`](./frontend).
