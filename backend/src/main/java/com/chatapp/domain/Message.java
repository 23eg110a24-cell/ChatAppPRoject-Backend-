package com.chatapp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "messages", indexes = {
        @Index(name = "idx_messages_room_created", columnList = "room_id, created_at")
})
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room;

    /**
     * Null for system notices such as "Ada joined the channel".
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @Column(nullable = false, length = 4000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private MessageType type;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Message() {
        // required by JPA
    }

    public Message(ChatRoom room, User sender, String content, MessageType type) {
        this.room = room;
        this.sender = sender;
        this.content = content;
        this.type = type;
        this.createdAt = Instant.now();
    }

    public static Message chat(ChatRoom room, User sender, String content) {
        return new Message(room, sender, content, MessageType.CHAT);
    }

    public static Message system(ChatRoom room, String content) {
        return new Message(room, null, content, MessageType.SYSTEM);
    }

    public Long getId() {
        return id;
    }

    public ChatRoom getRoom() {
        return room;
    }

    public User getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MessageType getType() {
        return type;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Message message)) {
            return false;
        }
        return id != null && id.equals(message.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Message{id=" + id + ", room=" + (room == null ? null : room.getId()) + "}";
    }
}
