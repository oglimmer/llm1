package de.oglimmer.llm1.llm;

import org.springframework.web.socket.WebSocketSession;

public interface AiService {
    void doAi(String question, WebSocketSession session);
}
