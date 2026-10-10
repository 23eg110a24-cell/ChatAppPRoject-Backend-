package com.chatapp.controller;

import com.chatapp.domain.User;
import com.chatapp.dto.UserDto;
import com.chatapp.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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
}