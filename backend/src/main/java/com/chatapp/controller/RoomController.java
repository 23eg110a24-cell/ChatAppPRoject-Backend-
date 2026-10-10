package com.chatapp.controller;

import com.chatapp.domain.User;
import com.chatapp.dto.CreateRoomRequest;
import com.chatapp.dto.MessageDto;
import com.chatapp.dto.RoomDto;
import com.chatapp.dto.SendMessageRequest;
import com.chatapp.service.MessageService;
import com.chatapp.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;
    private final MessageService messageService;

    public RoomController(RoomService roomService, MessageService messageService) {
        this.roomService = roomService;
        this.messageService = messageService;
    }

    @GetMapping
    public List<RoomDto> mine(@AuthenticationPrincipal User user) {
        return roomService.listForUser(user.getId());
    }

    @GetMapping("/channels")
    public List<RoomDto> channels(@RequestParam(required = false) String q,
                                  @AuthenticationPrincipal User user) {
        return roomService.browseChannels(q, user.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomDto createChannel(@Valid @RequestBody CreateRoomRequest request,
                                 @AuthenticationPrincipal User user) {
        return roomService.createChannel(user.getId(), request);
    }

    @PostMapping("/direct/{partnerId}")
    public RoomDto openDirect(@PathVariable Long partnerId, @AuthenticationPrincipal User user) {
        return roomService.openDirect(user.getId(), partnerId);
    }

    @PostMapping("/{roomId}/join")
    public RoomDto join(@PathVariable Long roomId, @AuthenticationPrincipal User user) {
        return roomService.joinChannel(user.getId(), roomId);
    }

    @GetMapping("/{roomId}/messages")
    public List<MessageDto> history(@PathVariable Long roomId,
                                    @RequestParam(required = false) Long before,
                                    @RequestParam(defaultValue = "50") int limit,
                                    @AuthenticationPrincipal User user) {
        return messageService.history(roomId, user.getId(), before, limit);
    }

    @PostMapping("/{roomId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageDto send(@PathVariable Long roomId,
                           @Valid @RequestBody SendMessageRequest request,
                           @AuthenticationPrincipal User user) {
        return messageService.send(roomId, user.getId(), request.content());
    }
}