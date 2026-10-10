package com.chatapp.service;

import com.chatapp.domain.ChatRoom;
import com.chatapp.domain.Message;
import com.chatapp.domain.User;
import com.chatapp.dto.MessageDto;
import com.chatapp.error.ApiException;
import com.chatapp.repository.MessageRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class MessageService {

    private static final int MAX_PAGE_SIZE = 100;

    private final MessageRepository messageRepository;
    private final RoomService roomService;
    private final UserService userService;

    public MessageService(MessageRepository messageRepository,
                          RoomService roomService,
                          UserService userService) {
        this.messageRepository = messageRepository;
        this.roomService = roomService;
        this.userService = userService;
    }

    /** Oldest-first page of history. Pass {@code beforeId} to load older messages. */
    @Transactional(readOnly = true)
    public List<MessageDto> history(Long roomId, Long userId, Long beforeId, int limit) {
        roomService.requireMember(roomId, userId);
        PageRequest page = PageRequest.of(0, Math.min(Math.max(limit, 1), MAX_PAGE_SIZE));

        List<Message> found = beforeId == null
                ? messageRepository.findLatest(roomId, page)
                : messageRepository.findLatestBefore(roomId, beforeId, page);

        List<MessageDto> result = new ArrayList<>(found.stream().map(m -> MessageDto.from(m, roomId)).toList());
        Collections.reverse(result);
        return result;
    }

    public MessageDto send(Long roomId, Long senderId, String content) {
        ChatRoom room = roomService.requireMember(roomId, senderId);
        User sender = userService.requireById(senderId);

        String text = content == null ? "" : content.strip();
        if (text.isEmpty()) {
            throw ApiException.badRequest("Message content is required");
        }

        Message saved = messageRepository.save(Message.chat(room, sender, text));
        room.setLastMessageAt(saved.getCreatedAt());
        return MessageDto.from(saved, roomId);
    }
}