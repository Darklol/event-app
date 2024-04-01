package org.itmo.eventapp.main.model.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateEventRequest (
        @Min(value = 1, message = "Поле userId не может быть меньше 1!")
        Integer userId,
        @NotBlank(message = "Поле title не может быть пустой!")
        String title) {
}
