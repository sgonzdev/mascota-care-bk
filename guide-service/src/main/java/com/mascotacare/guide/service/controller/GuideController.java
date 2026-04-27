package com.mascotacare.guide.service.controller;

import com.mascotacare.guide.service.dto.GuideRequest;
import com.mascotacare.guide.service.dto.GuideResponse;
import com.mascotacare.guide.service.service.GuideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/guides")
@RequiredArgsConstructor
public class GuideController {

    private final GuideService service;

    @PostMapping("/generate")
    public ResponseEntity<GuideResponse> generate(@Valid @RequestBody GuideRequest req) {
        GuideResponse out = service.generate(req);
        return ResponseEntity.created(URI.create("/api/guides/" + out.id())).body(out);
    }

    @GetMapping("/by-pet/{idMascota}")
    public List<GuideResponse> history(@PathVariable UUID idMascota) {
        return service.historyOf(idMascota);
    }

    @GetMapping("/{id}")
    public GuideResponse get(@PathVariable UUID id) {
        return service.findById(id);
    }
}
