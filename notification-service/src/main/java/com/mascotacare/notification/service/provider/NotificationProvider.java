package com.mascotacare.notification.service.provider;

import com.mascotacare.notification.service.entity.Notification;

public interface NotificationProvider {
    Notification.Canal supports();
    void send(String destinatario, String asunto, String contenido) throws Exception;
}
