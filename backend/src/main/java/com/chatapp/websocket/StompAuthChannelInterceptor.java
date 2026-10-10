package com.chatapp.websocket;

import com.chatapp.repository.ChatRoomRepository;
import com.chatapp.repository.UserRepository;
import com.chatapp.security.BearerTokens;
import com.chatapp.security.JwtAuthenticationFilter;
import com.chatapp.security.JwtService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Authenticates the STOMP CONNECT frame with the same bearer token the REST API uses. The
 * principal's name is the user id as a string.
 */
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final Pattern ROOM_TOPIC = Pattern.compile("^/topic/rooms/(\\d+)(/typing)?$");

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ChatRoomRepository roomRepository;

    public StompAuthChannelInterceptor(JwtService jwtService,
                                       UserRepository userRepository,
                                       ChatRoomRepository roomRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        if (StompCommand.CONNECT.equals(command)) {
            authenticate(accessor);
        } else if (StompCommand.SEND.equals(command) || StompCommand.SUBSCRIBE.equals(command)) {
            if (accessor.getUser() == null) {
                throw new MessagingException("Unauthorized");
            }
            if (StompCommand.SUBSCRIBE.equals(command)) {
                checkSubscription(accessor);
            }
        }
        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {
        Long userId = BearerTokens.extract(accessor.getFirstNativeHeader("Authorization"))
                .flatMap(jwtService::extractUserId)
                .filter(userRepository::existsById)
                .orElseThrow(() -> new MessagingException("Unauthorized"));

        accessor.setUser(new UsernamePasswordAuthenticationToken(
                String.valueOf(userId),
                null,
                List.of(new SimpleGrantedAuthority(JwtAuthenticationFilter.USER_ROLE))));
    }

    private void checkSubscription(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null) {
            throw new MessagingException("Missing destination");
        }
        Matcher room = ROOM_TOPIC.matcher(destination);
        if (room.matches()) {
            Long roomId = Long.valueOf(room.group(1));
            Long userId = Long.valueOf(accessor.getUser().getName());
            if (!roomRepository.existsByIdAndMembersId(roomId, userId)) {
                throw new MessagingException("Not a member of this room");
            }
            return;
        }
        if (!"/topic/presence".equals(destination) && !"/user/queue/errors".equals(destination)) {
            throw new MessagingException("Unknown destination");
        }
    }
}