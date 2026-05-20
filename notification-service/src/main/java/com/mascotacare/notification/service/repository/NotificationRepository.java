package com.mascotacare.notification.service.repository;

import com.mascotacare.notification.service.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    /** Listado para un usuario: ve TODAS las GLOBAL + sus PERSONAL. */
    Page<Notification> findByScopeOrIdUsuarioOrderByCreadaEnDesc(
            Notification.Scope scope, UUID idUsuario, Pageable pageable);

    /** Listado para admin: todas las del sistema. */
    Page<Notification> findAllByOrderByCreadaEnDesc(Pageable pageable);
}
