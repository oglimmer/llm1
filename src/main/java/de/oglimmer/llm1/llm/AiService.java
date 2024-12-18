package de.oglimmer.llm1.llm;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

@Service
public class AiService {

    private final Assistant assistant;

    public AiService() {
        String openaiKey = System.getenv("OPENAI_KEY");
        OpenAiStreamingChatModel model = OpenAiStreamingChatModel.builder()
                .apiKey(openaiKey)
                .modelName(GPT_4_O_MINI)
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

    public void doAi(String question, WebSocketSession session) {

        TokenStream tokenStream = assistant.chat(question);

        tokenStream.onNext((String token) -> {
                    try {
                        session.sendMessage(new TextMessage(token));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .onRetrieved(System.out::println)
                .onToolExecuted(System.out::println)
                .onComplete(System.out::println)
                .onError(Throwable::printStackTrace)
                .start();
    }

}

