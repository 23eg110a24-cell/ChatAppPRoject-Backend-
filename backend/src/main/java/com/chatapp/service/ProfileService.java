package com.chatapp.service;

import com.chatapp.domain.User;
import com.chatapp.dto.UserDto;
import com.chatapp.error.ApiException;
import com.chatapp.repository.UserRepository;
import com.chatapp.storage.FileStorageService;
import com.chatapp.storage.FileStorageService.Kind;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

/**
 * Write side of the profile: upload or remove a user's profile picture and banner.
 */
@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final FileStorageService storage;

    public ProfileService(UserRepository userRepository, UserService userService, FileStorageService storage) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.storage = storage;
    }

    @Transactional
    public UserDto setImage(Long userId, MultipartFile file, Kind kind) {
        User user = load(userId);
        String newUrl = storage.save(file, kind);
        String oldUrl = swap(user, kind, newUrl);
        userRepository.save(user);
        deleteAfterCommit(oldUrl);
        return userService.toDto(user);
    }

    @Transactional
    public UserDto removeImage(Long userId, Kind kind) {
        User user = load(userId);
        String oldUrl = swap(user, kind, null);
        userRepository.save(user);
        deleteAfterCommit(oldUrl);
        return userService.toDto(user);
    }

    private User load(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User " + userId + " does not exist"));
    }

    /** Stores the new URL on the user and returns the one it replaced. */
    private static String swap(User user, Kind kind, String url) {
        if (kind == Kind.AVATAR) {
            String old = user.getAvatarUrl();
            user.setAvatarUrl(url);
            return old;
        }
        String old = user.getBannerUrl();
        user.setBannerUrl(url);
        return old;
    }

    /** The old file is only deleted once the database change has really been committed. */
    private void deleteAfterCommit(String url) {
        if (url == null) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                storage.delete(url);
            }
        });
    }
}