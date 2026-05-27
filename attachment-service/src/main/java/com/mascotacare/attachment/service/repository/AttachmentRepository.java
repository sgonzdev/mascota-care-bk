package com.mascotacare.attachment.service.repository;

import com.mascotacare.attachment.service.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {
    List<Attachment> findByIdConsultaOrderByCreadoEnDesc(UUID idConsulta);
}
