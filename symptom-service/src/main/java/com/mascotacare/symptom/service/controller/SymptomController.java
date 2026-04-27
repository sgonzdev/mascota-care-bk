package com.mascotacare.symptom.service.controller;

import com.mascotacare.symptom.service.dto.SymptomRequest;
import com.mascotacare.symptom.service.dto.SymptomResponse;
import com.mascotacare.symptom.service.service.SymptomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/symptoms")
@RequiredArgsConstructor
public class SymptomController {

    private final SymptomService service;

    @PostMapping
    public ResponseEntity<SymptomResponse> register(@Valid @RequestBody SymptomRequest req) {
        SymptomResponse created = service.register(req);
        return ResponseEntity.created(URI.create("/api/symptoms/" + created.id())).body(created);
    }

    @GetMapping("/by-pet/{idMascota}")
    public List<SymptomResponse> history(@PathVariable UUID idMascota) {
        return service.historyOf(idMascota);
    }

    @GetMapping("/{id}")
    public SymptomResponse get(@PathVariable UUID id) {
        return service.findById(id);
    }
}
