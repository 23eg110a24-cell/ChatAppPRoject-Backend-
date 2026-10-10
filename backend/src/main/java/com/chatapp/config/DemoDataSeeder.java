package com.chatapp.config;

import com.chatapp.domain.AvatarColors;
import com.chatapp.domain.ChatRoom;
import com.chatapp.domain.Message;
import com.chatapp.domain.RoomType;
import com.chatapp.domain.User;
import com.chatapp.repository.ChatRoomRepository;
import com.chatapp.repository.MessageRepository;
import com.chatapp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
public class DemoDataSeeder implements CommandLineRunner {

    private final ChatAppProperties properties;
    private final UserRepository userRepository;
    private final ChatRoomRepository roomRepository;
    private final MessageRepository messageRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(ChatAppProperties properties,
                          UserRepository userRepository,
                          ChatRoomRepository roomRepository,
                          MessageRepository messageRepository,
                          PasswordEncoder passwordEncoder) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.messageRepository = messageRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!properties.seedDemoData() || userRepository.count() > 0) {
            return;
        }

        User ada = createUser("ada", "Ada Lovelace");
        User grace = createUser("grace", "Grace Hopper");
        User linus = createUser("linus", "Linus Torvalds");

        ChatRoom general = new ChatRoom("general", "Everyone starts here", RoomType.CHANNEL, ada);
        general.addMember(grace);
        general.addMember(linus);
        roomRepository.save(general);

        messageRepository.save(Message.system(general, "Welcome to #general"));
        messageRepository.save(Message.chat(general, ada, "Hi everyone, welcome to the chat."));
        Message last = messageRepository.save(Message.chat(general, grace, "Glad to be here."));
        general.setLastMessageAt(last.getCreatedAt());
    }

    private User createUser(String username, String displayName) {
        return userRepository.save(new User(
                username,
                username + "@example.com",
                passwordEncoder.encode("password123"),
                displayName,
                AvatarColors.forUsername(username)));
    }
}