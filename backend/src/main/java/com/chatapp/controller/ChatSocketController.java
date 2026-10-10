package com.chatapp.controller;

import com.chatapp.dto.MessageDto;
import com.chatapp.dto.TypingEvent;
import com.chatapp.dto.SendMessageRequest;
import com.chatapp.domain.User;
import com.chatapp.error.ApiException;
import com.chatapp.service.MessageService;
import com.chatapp.service.RoomService;
import com.chatapp.service.UserService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatSocketController {

    public record TypingSignal(boolean typing) {
    }

    private final MessageService messageService;
    private final RoomService roomService;
    private final UserService userService;
    private final SimpMessagingTemplate messaging;

    public ChatSocketController(MessageService messageService,
                                RoomService roomService,
                                UserService userService,
                                SimpMessagingTemplate messaging) {
        this.messageService = messageService;
        this.roomService = roomService;
        this.userService = userService;
        this.messaging = messaging;
    }

    @MessageMapping("/rooms/{roomId}/send")
    public void send(@DestinationVariable Long roomId,
                     @Payload SendMessageRequest request,
                     Principal principal) {
        MessageDto saved = messageService.send(roomId, userId(principal), request.content());
        messaging.convertAndSend("/topic/rooms/" + roomId, saved);
    }

    @MessageMapping("/rooms/{roomId}/typing")
    public void typing(@DestinationVariable Long roomId,
                       @Payload TypingSignal signal,
                       Principal principal) {
        Long userId = userId(principal);
        roomService.requireMember(roomId, userId);
        User user = userService.requireById(userId);
        messaging.convertAndSend("/topic/rooms/" + roomId + "/typing",
                new TypingEvent(roomId, userId, user.getDisplayName(), signal.typing()));
    }

    @MessageExceptionHandler(ApiException.class)
    @SendToUser("/queue/errors")
    public String handleApi(ApiException ex) {
        return ex.getMessage();
    }

    private static Long userId(Principal principal) {
        return Long.valueOf(principal.getName());
    }
}