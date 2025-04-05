package com.electronic.store.dtos;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JwtResponse {

    private String token;
    UserDto user;
    private RefreshTokenDto refreshToken;
}
