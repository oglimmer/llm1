# LLM / OpenAI Demo Application

This web-app uses https://docs.langchain4j.dev/ to access the OpenAI chat API.

A very simple html UI access the backend via WebSockets.

As an added bonus, you can integrate a custom directory using https://docs.langchain4j.dev/tutorials/rag/#easy-rag

# How to run

```bash
docker compose up --build
# wait all is started
docker exec -it llm1-ollama-1 ollama pull llama3.2:3b
```
