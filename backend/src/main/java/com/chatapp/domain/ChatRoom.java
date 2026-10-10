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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "chat_rooms", indexes = {
        @Index(name = "idx_chat_rooms_direct_key", columnList = "direct_key", unique = true)
})
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(length = 280)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RoomType type;

    /**
     * Stable, deterministic key for DIRECT rooms, e.g. {@code dm:7:12}. Null for channels.
     */
    @Column(name = "direct_key", length = 64)
    private String directKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "room_members",
            joinColumns = @JoinColumn(name = "room_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> members = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant lastMessageAt;

    protected ChatRoom() {
        // required by JPA
    }

    public ChatRoom(String name, String description, RoomType type, User owner) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.owner = owner;
        this.createdAt = Instant.now();
        if (owner != null) {
            this.members.add(owner);
        }
    }

    /**
     * Builds the deterministic key used to look up (and de-duplicate) a direct conversation
     * between two users, independent of the order they are passed in.
     */
    public static String directKeyFor(Long firstUserId, Long secondUserId) {
        long low = Math.min(firstUserId, secondUserId);
        long high = Math.max(firstUserId, secondUserId);
        return "dm:" + low + ":" + high;
    }

    public void addMember(User user) {
        members.add(user);
    }

    public void removeMember(User user) {
        members.remove(user);
    }

    public boolean hasMember(Long userId) {
        return members.stream().anyMatch(member -> Objects.equals(member.getId(), userId));
    }

    public boolean isDirect() {
        return type == RoomType.DIRECT;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RoomType getType() {
        return type;
    }

    public String getDirectKey() {
        return directKey;
    }

    public void setDirectKey(String directKey) {
        this.directKey = directKey;
    }

    public User getOwner() {
        return owner;
    }

    public Set<User> getMembers() {
        return members;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(Instant lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ChatRoom room)) {
            return false;
        }
        return id != null && id.equals(room.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "ChatRoom{id=" + id + ", name='" + name + "', type=" + type + "}";
    }
}
