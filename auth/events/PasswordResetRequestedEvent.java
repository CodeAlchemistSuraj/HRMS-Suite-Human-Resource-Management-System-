package com.hrms.auth.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class PasswordResetRequestedEvent extends ApplicationEvent {
    private final String email;
    private final UUID userId;
    
    public PasswordResetRequestedEvent(Object source, String email, UUID userId) {
        super(source);
        this.email = email;
        this.userId = userId;
    }
    
    public PasswordResetRequestedEvent(Object source, String email) {
        super(source);
        this.email = email;
        this.userId = null;
    }
}