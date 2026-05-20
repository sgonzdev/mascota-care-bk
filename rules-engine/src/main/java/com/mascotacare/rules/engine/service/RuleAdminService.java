package com.mascotacare.rules.engine.service;

import com.mascotacare.rules.engine.cache.RuleCacheManager;
import com.mascotacare.rules.engine.dto.PageResponse;
import com.mascotacare.rules.engine.dto.RuleRequest;
import com.mascotacare.rules.engine.dto.RuleResponse;
import com.mascotacare.rules.engine.entity.Rule;
import com.mascotacare.rules.engine.mapper.RuleMapper;
import com.mascotacare.rules.engine.repository.RuleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RuleAdminService {

    private final RuleRepository repository;
    private final RuleMapper mapper;
    private final RuleCacheManager cache;

    public RuleResponse create(RuleRequest req) {
        RuleResponse out = mapper.toResponse(repository.save(mapper.toEntity(req)));
        cache.invalidate();
        return out;
    }

    @Transactional(readOnly = true)
    public PageResponse<RuleResponse> listAll(Pageable pageable) {
        return PageResponse.from(repository.findAll(pageable).map(mapper::toResponse));
    }

    @Transactional(readOnly = true)
    public RuleResponse findById(UUID id) {
        return mapper.toResponse(getOrThrow(id));
    }

    public RuleResponse update(UUID id, RuleRequest req) {
        Rule entity = getOrThrow(id);
        mapper.update(entity, req);
        cache.invalidate();
        return mapper.toResponse(entity);
    }

    public RuleResponse toggle(UUID id) {
        Rule entity = getOrThrow(id);
        entity.setActiva(!Boolean.TRUE.equals(entity.getActiva()));
        cache.invalidate();
        return mapper.toResponse(entity);
    }

    public void delete(UUID id) {
        if (!repository.existsById(id)) throw notFound(id);
        repository.deleteById(id);
        cache.invalidate();
    }

    private Rule getOrThrow(UUID id) {
        return repository.findById(id).orElseThrow(() -> notFound(id));
    }

    private EntityNotFoundException notFound(UUID id) {
        return new EntityNotFoundException("Regla " + id + " no encontrada");
    }
}
