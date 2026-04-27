package com.mascotacare.notification.service.controller;

import com.mascotacare.notification.service.dto.NotificationRequest;
import com.mascotacare.notification.service.dto.NotificationResponse;
import com.mascotacare.notification.service.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @PostMapping("/send")
    public ResponseEntity<NotificationResponse> send(@Valid @RequestBody NotificationRequest req) {
        NotificationResponse out = service.send(req);
        return ResponseEntity.created(URI.create("/api/notifications/" + out.id())).body(out);
    }

    @GetMapping
    public List<NotificationResponse> list() { return service.listAll(); }

    @GetMapping("/{id}")
    public NotificationResponse get(@PathVariable UUID id) { return service.findById(id); }
}
