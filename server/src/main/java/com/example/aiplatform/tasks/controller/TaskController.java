package com.example.aiplatform.tasks.controller;

import com.example.aiplatform.auth.security.AuthenticatedUser;
import com.example.aiplatform.tasks.dto.CreateTaskRequest;
import com.example.aiplatform.tasks.dto.DeleteTaskResponse;
import com.example.aiplatform.tasks.dto.TaskResponse;
import com.example.aiplatform.tasks.dto.UpdateTaskRequest;
import com.example.aiplatform.tasks.dto.UpdateTaskStatusRequest;
import com.example.aiplatform.tasks.entity.TaskPriority;
import com.example.aiplatform.tasks.entity.TaskStatus;
import com.example.aiplatform.tasks.service.TaskService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<TaskResponse> getList(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) UUID learningGoalId
    ) {
        return taskService.getList(user.id(), status, priority, learningGoalId);
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateTaskRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(taskService.create(user.id(), request));
    }

    @GetMapping("/{taskId}")
    public TaskResponse getById(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID taskId
    ) {
        return taskService.getById(user.id(), taskId);
    }

    @PutMapping("/{taskId}")
    public TaskResponse update(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateTaskRequest request
    ) {
        return taskService.update(user.id(), taskId, request);
    }

    @DeleteMapping("/{taskId}")
    public DeleteTaskResponse delete(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID taskId
    ) {
        return taskService.delete(user.id(), taskId);
    }

    @PatchMapping("/{taskId}/status")
    public TaskResponse changeStatus(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateTaskStatusRequest request
    ) {
        return taskService.changeStatus(user.id(), taskId, request);
    }
}
