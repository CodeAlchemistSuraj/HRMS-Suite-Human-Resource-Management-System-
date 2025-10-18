package com.hrms.auth.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class RefreshTokenRevokedEvent extends ApplicationEvent {
    private final UUID refreshTokenId;
    private final UUID userId;
    
    public RefreshTokenRevokedEvent(Object source, UUID refreshTokenId, UUID userId) {
        super(source);
        this.refreshTokenId = refreshTokenId;
        this.userId = userId;
    }
}