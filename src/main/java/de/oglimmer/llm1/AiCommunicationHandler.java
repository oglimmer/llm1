package de.oglimmer.llm1;

import de.oglimmer.llm1.llm.AiService;
import de.oglimmer.llm1.llm.OllamaAiService;
import de.oglimmer.llm1.llm.OpenAiService;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;

@Getter
@Component
@Slf4j
public class AiCommunicationHandler extends TextWebSocketHandler {

    private AiService aiService;
    private final Map<String, WebSocketSession> sessions = new HashMap<>();

    public AiCommunicationHandler(@NonNull OllamaAiService aiService) {
        this.aiService = aiService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        log.debug(message.getPayload());
        aiService.doAi(message.getPayload(), session);
    }

}