package com.mascotacare.rules.engine.controller;

import com.mascotacare.rules.engine.dto.RuleRequest;
import com.mascotacare.rules.engine.dto.RuleResponse;
import com.mascotacare.rules.engine.service.RuleAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
public class RuleAdminController {

    private final RuleAdminService service;

    @PostMapping
    public ResponseEntity<RuleResponse> create(@Valid @RequestBody RuleRequest req) {
        RuleResponse out = service.create(req);
        return ResponseEntity.created(URI.create("/api/rules/" + out.id())).body(out);
    }

    @GetMapping
    public List<RuleResponse> list() { return service.listAll(); }

    @GetMapping("/{id}")
    public RuleResponse get(@PathVariable UUID id) { return service.findById(id); }

    @PutMapping("/{id}")
    public RuleResponse update(@PathVariable UUID id, @Valid @RequestBody RuleRequest req) {
        return service.update(id, req);
    }

    @PostMapping("/{id}/toggle")
    public RuleResponse toggle(@PathVariable UUID id) { return service.toggle(id); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) { service.delete(id); }
}
