package com.chatapp.service;

import com.chatapp.domain.ChatRoom;
import com.chatapp.domain.Message;
import com.chatapp.domain.RoomType;
import com.chatapp.domain.User;
import com.chatapp.dto.CreateRoomRequest;
import com.chatapp.dto.RoomDto;
import com.chatapp.error.ApiException;
import com.chatapp.repository.ChatRoomRepository;
import com.chatapp.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoomService {

    private final ChatRoomRepository roomRepository;
    private final MessageRepository messageRepository;
    private final UserService userService;

    public RoomService(ChatRoomRepository roomRepository,
                       MessageRepository messageRepository,
                       UserService userService) {
        this.roomRepository = roomRepository;
        this.messageRepository = messageRepository;
        this.userService = userService;
    }

    /** Sidebar: every room the user belongs to, most recently active first. */
    @Transactional(readOnly = true)
    public List<RoomDto> listForUser(Long userId) {
        List<ChatRoom> rooms = roomRepository.findAllForMember(userId);
        Map<Long, String> previews = latestPreviews(rooms);
        return rooms.stream()
                .sorted(Comparator.comparing(RoomService::activityAt, Comparator.reverseOrder()))
                .map(room -> RoomDto.from(room, userId, previews.get(room.getId())))
                .toList();
    }

    /** Channel browser: all channels, optionally filtered by name. */
    @Transactional(readOnly = true)
    public List<RoomDto> browseChannels(String rawTerm, Long viewerId) {
        String term = rawTerm == null ? "" : rawTerm.strip();
        List<ChatRoom> channels = term.isEmpty()
                ? roomRepository.findAllChannels()
                : roomRepository.searchChannels(term);
        return channels.stream()
                .map(room -> RoomDto.from(room, viewerId, null))
                .toList();
    }

    /** Finds or creates the one-and-only direct conversation between two users. */
    public RoomDto openDirect(Long viewerId, Long partnerId) {
        if (viewerId.equals(partnerId)) {
            throw ApiException.badRequest("You cannot start a conversation with yourself");
        }
        User viewer = userService.requireById(viewerId);
        User partner = userService.requireById(partnerId);
        String key = ChatRoom.directKeyFor(viewerId, partnerId);

        ChatRoom room = roomRepository.findByDirectKey(key).orElseGet(() -> {
            ChatRoom created = new ChatRoom("Direct message", null, RoomType.DIRECT, null);
            created.setDirectKey(key);
            created.addMember(viewer);
            created.addMember(partner);
            return roomRepository.save(created);
        });
        return RoomDto.from(room, viewerId, null);
    }

    public RoomDto createChannel(Long ownerId, CreateRoomRequest request) {
        User owner = userService.requireById(ownerId);
        String description = request.description() == null ? null : request.description().strip();
        ChatRoom room = new ChatRoom(request.name().strip(), description, RoomType.CHANNEL, owner);
        ChatRoom saved = roomRepository.save(room);
        return RoomDto.from(saved, ownerId, null);
    }

    public RoomDto joinChannel(Long userId, Long roomId) {
        ChatRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> ApiException.notFound("Room " + roomId + " does not exist"));
        if (room.isDirect()) {
            throw ApiException.forbidden("Direct conversations cannot be joined");
        }
        if (!room.hasMember(userId)) {
            room.addMember(userService.requireById(userId));
        }
        return RoomDto.from(room, userId, null);
    }

    /** Loads a room and makes sure the user belongs to it. */
    public ChatRoom requireMember(Long roomId, Long userId) {
        ChatRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> ApiException.notFound("Room " + roomId + " does not exist"));
        if (!roomRepository.existsByIdAndMembersId(roomId, userId)) {
            throw ApiException.forbidden("You are not a member of this room");
        }
        return room;
    }

    private Map<Long, String> latestPreviews(List<ChatRoom> rooms) {
        if (rooms.isEmpty()) {
            return Map.of();
        }
        List<Long> roomIds = rooms.stream().map(ChatRoom::getId).toList();
        return messageRepository.findLatestPerRoom(roomIds).stream()
                .collect(Collectors.toMap(
                        message -> message.getRoom().getId(),
                        Message::getContent,
                        (first, second) -> first));
    }

    private static Instant activityAt(ChatRoom room) {
        return room.getLastMessageAt() != null ? room.getLastMessageAt() : room.getCreatedAt();
    }
}