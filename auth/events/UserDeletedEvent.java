package com.hrms.auth.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class UserDeletedEvent extends ApplicationEvent {
    private final UUID userId;
    
    public UserDeletedEvent(Object source, UUID userId) {
        super(source);
        this.userId = userId;
    }
}