package com.electronic.store.services.impl;

import com.electronic.store.dtos.RefreshTokenDto;
import com.electronic.store.dtos.UserDto;
import com.electronic.store.entities.RefreshToken;
import com.electronic.store.entities.User;
import com.electronic.store.exceptions.ResourceNotFoundException;
import com.electronic.store.repositories.RefreshTokenRepository;
import com.electronic.store.repositories.UserRepository;
import com.electronic.store.services.RefreshTokenService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private UserRepository userRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private ModelMapper modelMapper;

    public RefreshTokenServiceImpl(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public RefreshTokenDto createRefreshToken(String username) {
        User user = userRepository.findByEmail(username).orElseThrow(()->new ResourceNotFoundException("User not found with given username !!"));
      RefreshToken refreshToken = refreshTokenRepository.findByUser(user).orElse(null);
        if(refreshToken == null){
            refreshToken = RefreshToken.builder()
                    .user(user)
                    .token(UUID.randomUUID().toString())
                    .expiryDate(Instant.now().plusSeconds(5*24*60*60))
                    .build();
        }else {
            refreshToken.setToken(UUID.randomUUID().toString());
            refreshToken.setExpiryDate(Instant.now().plusSeconds(5*24*60*60));
        }
        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        return this.modelMapper.map(savedToken,RefreshTokenDto.class);
    }

    @Override
    public RefreshTokenDto findByToken(String token) {
        RefreshToken refreshToken=refreshTokenRepository.findByToken(token).orElseThrow(()->new ResourceNotFoundException("Token not found in database !!"));
        return this.modelMapper.map(refreshToken, RefreshTokenDto.class);
    }

    @Override
    public RefreshTokenDto verifyRefreshToken(RefreshTokenDto token) {
        var refreshToken = modelMapper.map(token, RefreshToken.class);

        if(token.getExpiryDate().compareTo(Instant.now()) < 0){
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh Token Expired !!");
        }
        return token;
    }

    @Override
    public UserDto getUser(RefreshTokenDto dto) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(dto.getToken()).orElseThrow(()-> new ResourceNotFoundException("Token not found !!"));
        User user = refreshToken.getUser();
        return modelMapper.map(user, UserDto.class);
    }
}
