package com.chatapp.websocket;

import com.chatapp.dto.PresenceEvent;
import com.chatapp.repository.UserRepository;
import com.chatapp.service.PresenceService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.time.Instant;

@Component
public class PresenceEventListener {

    private final PresenceService presenceService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messaging;

    public PresenceEventListener(PresenceService presenceService,
                                 UserRepository userRepository,
                                 SimpMessagingTemplate messaging) {
        this.presenceService = presenceService;
        this.userRepository = userRepository;
        this.messaging = messaging;
    }

    @EventListener
    public void onConnected(SessionConnectedEvent event) {
        Principal principal = event.getUser();
        if (principal == null) {
            return;
        }
        Long userId = Long.valueOf(principal.getName());
        String sessionId = StompHeaderAccessor.wrap(event.getMessage()).getSessionId();
        boolean wasOnline = presenceService.isOnline(userId);
        presenceService.sessionConnected(userId, sessionId);
        if (!wasOnline) {
            messaging.convertAndSend("/topic/presence", new PresenceEvent(userId, true, null));
        }
    }

    @EventListener
    @Transactional
    public void onDisconnected(SessionDisconnectEvent event) {
        Principal principal = event.getUser();
        if (principal == null) {
            return;
        }
        Long userId = Long.valueOf(principal.getName());
        if (presenceService.sessionDisconnected(userId, event.getSessionId())) {
            Instant now = Instant.now();
            userRepository.findById(userId).ifPresent(user -> user.setLastSeenAt(now));
            messaging.convertAndSend("/topic/presence", new PresenceEvent(userId, false, now));
        }
    }
}