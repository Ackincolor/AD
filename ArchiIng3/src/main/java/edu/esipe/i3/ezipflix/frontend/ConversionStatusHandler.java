package edu.esipe.i3.ezipflix.frontend;

import edu.esipe.i3.ezipflix.frontend.data.entities.VideoConversions;
import edu.esipe.i3.ezipflix.frontend.data.repositories.VideoConversionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.UUID;

public class ConversionStatusHandler extends TextWebSocketHandler {

    @Autowired
    VideoConversionRepository videoConversionRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(VideoStatusHandler.class);
    public ConversionStatusHandler() {
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        LOGGER.info("Session opened = {}", session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        //VideoConversions vc = videoConversionRepository.findById(UUID.fromString(message.getPayload())).get();
        String msg = message.getPayload();
        System.out.println(msg);
        VideoConversions vc = videoConversionRepository.findById(UUID.fromString(msg)).get();
        LOGGER.info("Status = {}", msg);
        session.sendMessage(new TextMessage(Float.toString(vc.getDone())));
    }
}
