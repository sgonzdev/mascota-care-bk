package com.mascotacare.pet.service.service;

import com.mascotacare.pet.service.dto.PageResponse;
import com.mascotacare.pet.service.dto.PetRequest;
import com.mascotacare.pet.service.dto.PetResponse;
import com.mascotacare.pet.service.entity.Pet;
import com.mascotacare.pet.service.mapper.PetMapper;
import com.mascotacare.pet.service.repository.PetRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PetService {

    private final PetRepository repository;
    private final PetMapper mapper;
    private final PetValidator validator;

    public PetResponse create(PetRequest req) {
        validator.validate(req);
        Pet entity = mapper.toEntity(req);
        return mapper.toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public PageResponse<PetResponse> listAll(Pageable pageable) {
        return PageResponse.from(repository.findAll(pageable).map(mapper::toResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<PetResponse> listByUser(UUID idUsuario, Pageable pageable) {
        return PageResponse.from(
                repository.findByIdUsuario(idUsuario, pageable).map(mapper::toResponse));
    }

    @Transactional(readOnly = true)
    public PetResponse findById(UUID id) {
        return mapper.toResponse(getOrThrow(id));
    }

    public PetResponse update(UUID id, PetRequest req) {
        validator.validate(req);
        Pet entity = getOrThrow(id);
        mapper.update(entity, req);
        return mapper.toResponse(entity);
    }

    public void delete(UUID id) {
        if (!repository.existsById(id)) throw notFound(id);
        repository.deleteById(id);
    }

    private Pet getOrThrow(UUID id) {
        return repository.findById(id).orElseThrow(() -> notFound(id));
    }

    private EntityNotFoundException notFound(UUID id) {
        return new EntityNotFoundException("Mascota %s no encontrada".formatted(id));
    }
}
