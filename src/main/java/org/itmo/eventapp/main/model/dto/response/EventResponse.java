package org.itmo.eventapp.main.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.itmo.eventapp.main.model.entity.enums.EventFormat;
import org.itmo.eventapp.main.model.entity.enums.EventStatus;

import java.time.LocalDateTime;

public record EventResponse(
        @Schema(example = "3242")
        Integer id,
        @Schema(example = "12")
        Integer placeId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        @Schema(example = "День первокурсника")
        String title,
        @Schema(example = "День для всех первокурсников")
        String shortDescription,
        @Schema(example = "День для всех первокурсников, где они познакомятся с университетом ИТМО")
        String fullDescription,
        EventFormat format,
        EventStatus status,
        LocalDateTime registrationStart,
        LocalDateTime registrationEnd,
        Integer parent,
        @Schema(example = "1200")
        int participantLimit,
        @Schema(example = "16")
        int participantAgeLowest,
        int participantAgeHighest,
        LocalDateTime preparingStart,
        LocalDateTime preparingEnd
) {
}
