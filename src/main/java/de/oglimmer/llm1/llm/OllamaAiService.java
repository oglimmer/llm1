package de.oglimmer.llm1.llm;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

@Profile("ollama")
@Service
@Slf4j
public class OllamaAiService implements AiService {

    private final Assistant assistant;

    public OllamaAiService() {
        String modelName = System.getenv("MODEL_NAME");
        OllamaStreamingChatModel model = OllamaStreamingChatModel.builder()
                .modelName(modelName)
                .baseUrl("http://ollama:11434")
                .timeout(Duration.ofMinutes(30))
                .build();

        String loc = System.getenv("SOURCE_CODE_ROOT");
        List<Document> documents = FileSystemDocumentLoader.loadDocuments(loc);

        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        EmbeddingStoreIngestor.ingest(documents, embeddingStore);


        assistant = AiServices.builder(Assistant.class)
                .streamingChatLanguageModel(model)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .contentRetriever(EmbeddingStoreContentRetriever.from(embeddingStore))
                .build();

    }

    @Override
    public void doAi(String question, WebSocketSession session) {

        TokenStream tokenStream = assistant.chat(question);

        tokenStream.onNext((String token) -> {
                    try {
                        session.sendMessage(new TextMessage(token));
                    } catch (IOException e) {
                        log.error("Error sending message", e);
                    }
                })
                .onRetrieved(contentList -> log.debug(contentList.toString()))
                .onToolExecuted(toolExecution -> log.debug(toolExecution.toString()))
                .onComplete(response -> log.debug(response.toString()))
                .onError(err -> log.error("onError", err))
                .start();
    }

}

