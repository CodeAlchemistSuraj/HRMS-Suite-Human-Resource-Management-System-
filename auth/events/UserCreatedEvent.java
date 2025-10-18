package com.hrms.auth.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class UserCreatedEvent extends ApplicationEvent {
    private final UUID userId;
    
    public UserCreatedEvent(Object source, UUID userId) {
        super(source);
        this.userId = userId;
    }
}