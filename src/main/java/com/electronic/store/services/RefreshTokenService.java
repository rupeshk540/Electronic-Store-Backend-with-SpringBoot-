package com.electronic.store.services;

import com.electronic.store.dtos.RefreshTokenDto;
import com.electronic.store.dtos.UserDto;

public interface RefreshTokenService {

    //create
    RefreshTokenDto createRefreshToken(String username);

    //find by token
    RefreshTokenDto findByToken(String token);

    //verify
    RefreshTokenDto verifyRefreshToken(RefreshTokenDto refreshTokenDto);

    UserDto getUser(RefreshTokenDto dto);
}
