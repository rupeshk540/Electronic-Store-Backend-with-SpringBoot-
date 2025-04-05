package com.electronic.store.dtos;

import com.electronic.store.entities.User;
import jakarta.persistence.OneToOne;
import lombok.*;

import java.time.Instant;
 @AllArgsConstructor
 @NoArgsConstructor
 @Getter
 @Setter
 @Builder
public class RefreshTokenDto {

    private int id;
    private String token;
    private Instant expiryDate;

}
