package org.itmo.eventapp.main.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.itmo.eventapp.main.model.dto.request.CreateEventRequest;
import org.itmo.eventapp.main.model.dto.request.EventRequest;
import org.itmo.eventapp.main.model.dto.response.EventResponse;
import org.itmo.eventapp.main.model.dto.response.PaginatedResponse;
import org.itmo.eventapp.main.model.dto.response.UserRoleResponse;
import org.itmo.eventapp.main.model.entity.Event;
import org.itmo.eventapp.main.model.entity.EventRole;
import org.itmo.eventapp.main.model.entity.enums.EventFormat;
import org.itmo.eventapp.main.model.entity.enums.EventStatus;
import org.itmo.eventapp.main.model.mapper.EventMapper;
import org.itmo.eventapp.main.model.mapper.EventRoleMapper;
import org.itmo.eventapp.main.service.EventService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;


@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/events")
@Validated
public class EventController {
    private final EventService eventService;

    @Operation(summary = "Создание активности мероприятия")
    @PostMapping(value = "/activity", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Integer> addActivity(@Valid EventRequest eventRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.addEvent(eventRequest).getId());
    }

    @Operation(summary = "Создание мероприятия")
    @PostMapping
    public ResponseEntity<Integer> addEventByOrganizer(@RequestBody @Valid CreateEventRequest eventRequest) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(eventService.addEventByOrganizer(eventRequest).getId());
    }

    // TODO: Return images in response
    @Operation(summary = "Обновление мероприятия")
    @ApiResponses(
        value = {
            @ApiResponse(
                content = {
                    @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = EventResponse.class))
                })
        })
    @PreAuthorize("@eventSecurityExpression.canUpdateEvent(#id)")
    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(@Min(1) @PathVariable("id") @Parameter(name = "id", description = "ID мероприятия", example = "1") Integer id,
                                                     @Valid @RequestBody EventRequest eventRequest) {
        return ResponseEntity.ok().body(EventMapper.eventToEventResponse(eventService.updateEvent(id, eventRequest)));
    }

    @Operation(summary = "Фильрация мероприятий")
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                content = {
                    @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = PaginatedResponse.class))
                })
        })
    @SuppressWarnings("java:S107")
    @PreAuthorize("@eventSecurityExpression.canGetEvents()")
    @GetMapping
    public ResponseEntity<PaginatedResponse<EventResponse>> getAllOrFilteredEvents(
        @Min(0)
        @RequestParam(value = "page", defaultValue = "0")
        @Parameter(name = "page", description = "Номер страницы, с которой начать показ мероприятий", example = "0")
        int page,
        @Min(0)
        @Max(50)
        @RequestParam(value = "size", defaultValue = "15")
        @Parameter(name = "size", description = "Число мероприятий на странице", example = "15")
        int size,
        @RequestParam(required = false)
        @Parameter(name = "parentId", description = "ID родительского мероприятия", example = "12")
        Integer parentId,
        @RequestParam(required = false)
        @Parameter(name = "title", description = "Название мероприятия", example = "День первокурсника")
        String title,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @Parameter(name = "startDate", description = "Дата начала мероприятия", example = "2024-09-01Е12:00:00")
        LocalDateTime startDate,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @Parameter(name = "endDate", description = "Дата окончания мероприятия", example = "2024-09-29Е17:00:00")
        LocalDateTime endDate,
        @RequestParam(required = false)
        @Parameter(name = "status", description = "Статус мероприятия", example = "PUBLISHED")
        EventStatus status,
        @RequestParam(required = false)
        @Parameter(name = "format", description = "Формат мероприятия", example = "OFFLINE")
        EventFormat format) {
        PaginatedResponse<Event> result = eventService.getAllOrFilteredEvents(page, size, parentId, title, startDate, endDate, status, format);
        PaginatedResponse<EventResponse> response = new PaginatedResponse<>(result.total(), EventMapper.eventsToEventResponseList(result.items()));
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "Получение мероприятия по id")
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                content = {
                    @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = EventResponse.class))
                })
        })
    @GetMapping("/{id}")
    @PreAuthorize("@eventSecurityExpression.canGetEvents()")
    public ResponseEntity<EventResponse> getEventById(
        @Min(1)
        @PathVariable("id")
        @Parameter(name = "id", description = "ID мероприятия", example = "1")
        Integer id) {
        return ResponseEntity.ok().body(EventMapper.eventToEventResponse(eventService.getEventById(id)));
    }

    @Operation(summary = "Удаление мероприятия")
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteEventById(@Min(1) @PathVariable("id") @Parameter(name = "id", description = "ID мероприятия", example = "1") Integer id) {
        eventService.deleteEventById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получение списка пользователей, имеющих роль в данном мероприятии")
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                content = {
                    @Content(
                        mediaType = "application/json",
                        array = @ArraySchema(schema = @Schema(implementation = UserRoleResponse.class)))
                })
        })
    @PreAuthorize("@eventSecurityExpression.canGetUsersHavingRoles(#id)")
    @GetMapping("/{id}/organizers")
    public ResponseEntity<List<UserRoleResponse>> getUsersHavingRoles(@Min(1) @PathVariable("id") @Parameter(name = "id", description = "ID мероприятия", example = "1") Integer id) {
        List<EventRole> eventRoles = eventService.getUsersHavingRoles(id);
        return ResponseEntity.ok().body(EventRoleMapper.eventRolesToUserRoleResponses(eventRoles));
    }

    @Operation(summary = "Копирование мероприятия")
    @PreAuthorize("@eventSecurityExpression.canGetEvents()")
    @PostMapping("/{id}/copy")
    public ResponseEntity<Integer> copyEvent(
        @Min(1) @PathVariable("id") @Parameter(name = "id", description = "ID мероприятия", example = "1") Integer id,
        @RequestParam(value = "deep", defaultValue = "false") @Parameter(name = "deep", description = "Включить копирование активностей", example = "false") boolean deep) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(eventService.copyEvent(id, deep).getId());
    }
}
