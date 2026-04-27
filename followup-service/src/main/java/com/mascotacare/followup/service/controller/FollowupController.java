package com.mascotacare.followup.service.controller;

import com.mascotacare.followup.service.dto.FollowupRequest;
import com.mascotacare.followup.service.dto.FollowupResponse;
import com.mascotacare.followup.service.service.FollowupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/followups")
@RequiredArgsConstructor
public class FollowupController {

    private final FollowupService service;

    @PostMapping
    public ResponseEntity<FollowupResponse> register(@Valid @RequestBody FollowupRequest req) {
        FollowupResponse out = service.register(req);
        return ResponseEntity.created(URI.create("/api/followups/" + out.id())).body(out);
    }

    @GetMapping("/by-pet/{idMascota}")
    public List<FollowupResponse> history(@PathVariable UUID idMascota) {
        return service.historyOf(idMascota);
    }

    @GetMapping("/{id}")
    public FollowupResponse get(@PathVariable UUID id) {
        return service.findById(id);
    }
}
