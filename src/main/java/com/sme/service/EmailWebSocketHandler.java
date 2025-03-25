package com.sme.service;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EmailWebSocketHandler extends TextWebSocketHandler {
    private final List<WebSocketSession> sessions = new ArrayList<>();
    private final List<String> emailHistory = new ArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("WebSocket session opened: " + session.getId() + " from " + session.getRemoteAddress());
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("WebSocket session closed: " + session.getId() + " with status: " + status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.out.println("WebSocket transport error for session: " + session.getId() + " - " + exception.getMessage());
        session.close(CloseStatus.SERVER_ERROR);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("Received message: " + message.getPayload() + " from session: " + session.getId());
        session.sendMessage(new TextMessage("Pong: " + message.getPayload()));
    }

    public void sendEmailUpdate(String emailData) {
        emailHistory.add(emailData); // Store email in history
        System.out.println("Sending WebSocket update to " + sessions.size() + " sessions: " + emailData);
        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(emailData));
                    System.out.println("Sent to session: " + session.getId());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<String> getEmailHistory() {
        return new ArrayList<>(emailHistory); // Return a copy to avoid external modification
    }
}