package com.chatapp.controller;

import com.chatapp.domain.User;
import com.chatapp.dto.UserDto;
import com.chatapp.service.ProfileService;
import com.chatapp.service.UserService;
import com.chatapp.storage.FileStorageService.Kind;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final ProfileService profileService;

    public UserController(UserService userService, ProfileService profileService) {
        this.userService = userService;
        this.profileService = profileService;
    }

    @GetMapping("/me")
    public UserDto me(@AuthenticationPrincipal User currentUser) {
        return userService.toDto(userService.requireById(currentUser.getId()));
    }

    @GetMapping
    public List<UserDto> directory(@RequestParam(required = false) String q,
                                   @AuthenticationPrincipal User currentUser) {
        return userService.directory(q, currentUser.getId());
    }

    @PostMapping(path = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserDto uploadAvatar(@RequestParam("file") MultipartFile file,
                                @AuthenticationPrincipal User currentUser) {
        return profileService.setImage(currentUser.getId(), file, Kind.AVATAR);
    }

    @DeleteMapping("/me/avatar")
    public UserDto removeAvatar(@AuthenticationPrincipal User currentUser) {
        return profileService.removeImage(currentUser.getId(), Kind.AVATAR);
    }

    @PostMapping(path = "/me/banner", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserDto uploadBanner(@RequestParam("file") MultipartFile file,
                                @AuthenticationPrincipal User currentUser) {
        return profileService.setImage(currentUser.getId(), file, Kind.BANNER);
    }

    @DeleteMapping("/me/banner")
    public UserDto removeBanner(@AuthenticationPrincipal User currentUser) {
        return profileService.removeImage(currentUser.getId(), Kind.BANNER);
    }
}