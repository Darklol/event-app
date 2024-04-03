package org.itmo.eventapp.main.service;

import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.itmo.eventapp.main.model.dto.request.TaskRequest;
import org.itmo.eventapp.main.model.dto.response.TaskResponse;
import org.itmo.eventapp.main.model.entity.Event;
import org.itmo.eventapp.main.model.entity.Place;
import org.itmo.eventapp.main.model.entity.Task;
import org.itmo.eventapp.main.model.entity.User;
import org.itmo.eventapp.main.model.entity.enums.TaskStatus;
import org.itmo.eventapp.main.model.mapper.TaskMapper;
import org.itmo.eventapp.main.repository.TaskRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class TaskService {
    private final EventService eventService;
    private final UserService userService;
    private final PlaceService placeService;
    private final TaskRepository taskRepository;

    public Optional<Task> findById(int id) {
        return taskRepository.findById(id);
    }

    public TaskResponse save(TaskRequest taskRequest) {

        // task assigner from principal.name -> findByUsername
        Event event = eventService.findById(taskRequest.eventId());

        /*TODO: GET FROM PRINCIPAL*/
        User assigner = new User();
        assigner.setId(1);
        /*TODO: GET FROM PRINCIPAL*/

        User assignee = null;
        if (taskRequest.assignee()!= null) {
            assignee = userService.findById(taskRequest.assignee().id());
        }
        Place place = null;
        if (taskRequest.place()!= null) {
            place = placeService.findById(taskRequest.place().id());
        }

        Task newTask = TaskMapper.taskRequestToTask(taskRequest, event, assignee, assigner, place);

        TaskStatus status = TaskStatus.NEW;
        if (LocalDateTime.now().isAfter(newTask.getDeadline())) {
            status = TaskStatus.EXPIRED;
        }

        newTask.setStatus(status);
        newTask.setCreationTime(LocalDateTime.now());

        newTask = taskRepository.save(newTask);

        /*TODO: schedule task deadline notification for assigner & assignee */

        return TaskMapper.taskToTaskResponse(newTask);
    }

    public TaskResponse edit(Integer id, TaskRequest taskRequest) {

        Task task = taskRepository.findById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        Event event = eventService.findById(taskRequest.eventId());
        User assigner = task.getAssigner();

        User prevAssignee = task.getAssignee();

        User assignee = null;
        if (taskRequest.assignee()!= null) {
            assignee = userService.findById(taskRequest.assignee().id());
        }
        Place place = null;
        if (taskRequest.place()!= null) {
            place = placeService.findById(taskRequest.place().id());
        }

        Task newTaskData = TaskMapper.taskRequestToTask(taskRequest, event, assignee, assigner, place);
        newTaskData.setId(task.getId());
        newTaskData.setCreationTime(task.getCreationTime());
        if (LocalDateTime.now().isAfter(newTaskData.getDeadline())) {
            newTaskData.setStatus(TaskStatus.EXPIRED);
        }

        newTaskData = taskRepository.save(newTaskData);

        /*TODO: schedule task deadline notification for new assignee */
        if (prevAssignee != null) {
            /*TODO: unset task deadline notification for prev assignee */
        }

        return TaskMapper.taskToTaskResponse(newTaskData);
    }

    public void delete(Integer id) {

        taskRepository.deleteById(id);

    }


    public TaskResponse setAssignee(Integer taskId, Integer assigneeId) {

        Task task = taskRepository.findById(taskId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        User prevAssignee = task.getAssignee();

        /* TODO: ADD USER SERVICE CALL*/
        User assignee = null;
        if (assigneeId != -1) { // -1 stands for delete option
            assignee = userService.findById(assigneeId); // find by id from request
        }
        task.setAssignee(assignee);

        task = taskRepository.save(task);

        if (assignee != null) {
            /*TODO: schedule task deadline notification for new assignee */
        }
        if (prevAssignee != null) {
            /*TODO: unset task deadline notification for prev assignee */
        }

        return TaskMapper.taskToTaskResponse(task);

    }


    public TaskResponse setStatus(Integer taskId, TaskStatus taskStatus) {
        Task task = taskRepository.findById(taskId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        task.setStatus(taskStatus);
        task = taskRepository.save(task);
        return TaskMapper.taskToTaskResponse(task);
    }

    public List<TaskResponse> moveTasks(Integer dstEventId, List<Integer> taskIds) {

        Event event = eventService.findById(dstEventId);
        List<Task> tasks = taskRepository.findAllById(taskIds).stream().map(
                (task)-> {
                    task.setEvent(event);
                    return task;
                }
        ).toList();

        taskRepository.saveAll(tasks);

        return TaskMapper.tasksToTaskResponseList(tasks);
    }


    public List<TaskResponse> copyTasks(Integer dstEventId, List<Integer> taskIds) {

        Event event = eventService.findById(dstEventId);
        List<Task> tasks = taskRepository.findAllById(taskIds);

        List<Task> newTasks = new ArrayList<>();
        for (Task task: tasks) {

            Task newTask = new Task();
            newTask.setEvent(event);
            newTask.setTitle(task.getTitle());
            newTask.setDescription(task.getDescription());
            newTask.setPlace(task.getPlace());
            newTask.setAssigner(task.getAssigner());
            newTask.setAssignee(null);
            newTask.setDeadline(task.getDeadline());
            newTask.setNotificationDeadline(task.getNotificationDeadline());

            newTask.setCreationTime(LocalDateTime.now());
            TaskStatus status = TaskStatus.NEW;
            if (LocalDateTime.now().isAfter(newTask.getDeadline())) {
                status = TaskStatus.EXPIRED;
            }
            newTask.setStatus(status);

            newTasks.add(newTask);
        }

        taskRepository.saveAll(tasks);

        for (Task newTask: newTasks) {
            /*TODO: schedule task deadline notification for assigner */
        }

        return TaskMapper.tasksToTaskResponseList(tasks);
    }

}
