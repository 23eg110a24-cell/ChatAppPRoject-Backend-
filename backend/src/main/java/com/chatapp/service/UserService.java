package com.chatapp.service;

import com.chatapp.domain.User;
import com.chatapp.dto.UserDto;
import com.chatapp.error.ApiException;
import com.chatapp.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Read side of the user model: lookups and directory search used by the sidebar.
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private static final int DIRECTORY_LIMIT = 50;
    private static final int SEARCH_LIMIT = 20;

    private final UserRepository userRepository;
    private final PresenceService presenceService;

    public UserService(UserRepository userRepository, PresenceService presenceService) {
        this.userRepository = userRepository;
        this.presenceService = presenceService;
    }

    public User requireById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User " + userId + " does not exist"));
    }

    /**
     * Search the directory by username or display name. A blank term lists everybody else, which
     * is what the "start a conversation" picker needs.
     */
    public List<UserDto> directory(String rawTerm, Long excludeUserId) {
        String term = rawTerm == null ? "" : rawTerm.strip();
        List<User> users = term.isEmpty()
                ? userRepository.findAllExcept(excludeUserId, PageRequest.of(0, DIRECTORY_LIMIT))
                : userRepository.search(term, PageRequest.of(0, SEARCH_LIMIT));

        return users.stream()
                .filter(user -> !user.getId().equals(excludeUserId))
                .map(this::toDto)
                .toList();
    }

    public UserDto toDto(User user) {
        return UserDto.from(user, presenceService.isOnline(user.getId()));
    }
}
