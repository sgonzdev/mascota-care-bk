package com.mascotacare.pet.service.controller;

import com.mascotacare.pet.service.dto.PetRequest;
import com.mascotacare.pet.service.dto.PetResponse;
import com.mascotacare.pet.service.service.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService service;

    @PostMapping
    public ResponseEntity<PetResponse> create(@Valid @RequestBody PetRequest req) {
        PetResponse created = service.create(req);
        return ResponseEntity.created(URI.create("/api/pets/" + created.id())).body(created);
    }

    @GetMapping
    public List<PetResponse> list(@RequestParam(required = false) UUID idUsuario) {
        return idUsuario == null ? service.listAll() : service.listByUser(idUsuario);
    }

    @GetMapping("/{id}")
    public PetResponse get(@PathVariable UUID id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public PetResponse update(@PathVariable UUID id, @Valid @RequestBody PetRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
