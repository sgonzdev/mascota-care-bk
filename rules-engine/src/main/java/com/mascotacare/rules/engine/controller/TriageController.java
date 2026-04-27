package com.mascotacare.rules.engine.controller;

import com.mascotacare.rules.engine.dto.EvaluationRequest;
import com.mascotacare.rules.engine.dto.EvaluationResult;
import com.mascotacare.rules.engine.service.RulesEvaluator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/triage")
@RequiredArgsConstructor
public class TriageController {

    private final RulesEvaluator evaluator;

    @PostMapping("/evaluate")
    public EvaluationResult evaluate(@Valid @RequestBody EvaluationRequest req) {
        return evaluator.evaluate(req);
    }
}
