package com.chatapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateRoomRequest(

        @NotBlank(message = "Channel name is required")
        @Size(min = 2, max = 80, message = "Channel name must be between 2 and 80 characters")
        @Pattern(
                regexp = "^[A-Za-z0-9 ._-]+$",
                message = "Channel name may only contain letters, numbers, spaces, dots, underscores or hyphens")
        String name,

        @Size(max = 280, message = "Description is too long")
        String description
) {
}
