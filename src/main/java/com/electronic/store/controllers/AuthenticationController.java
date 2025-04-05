package com.electronic.store.controllers;

import com.electronic.store.dtos.*;
import com.electronic.store.entities.Providers;
import com.electronic.store.entities.User;
import com.electronic.store.exceptions.BadApiRequestException;
import com.electronic.store.exceptions.ResourceNotFoundException;
import com.electronic.store.security.JwtHelper;
import com.electronic.store.services.RefreshTokenService;
import com.electronic.store.services.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtHelper jwtHelper;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private RefreshTokenService refreshTokenService;

    @Value("${app.google.client_id}")
    private String googleClientId;
    @Value("${app.google.default_password}")
    private String googleProviderDefaultPassword;

    private Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    //to regenerate token through refreshToken
    @PostMapping("/regenerate-token")
    public ResponseEntity<JwtResponse> regenerateToken(@RequestBody RefreshTokenRequest request){
        RefreshTokenDto refreshTokenDto = refreshTokenService.findByToken(request.getRefreshToken());
        RefreshTokenDto refreshTokenDto1 = refreshTokenService.verifyRefreshToken(refreshTokenDto);
        UserDto user = refreshTokenService.getUser(refreshTokenDto1);
        String jwtToken = jwtHelper.generateToken(modelMapper.map(user, User.class));

        JwtResponse response = JwtResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshTokenDto)
                .user(user)
                .build();
        return ResponseEntity.ok(response);
    }

    //method to generate token
    @PostMapping("/generate-token")
    public ResponseEntity<JwtResponse> login(@RequestBody JwtRequest request){
        logger.info("Username {}, Password {}",request.getEmail(),request.getPassword());
        this.doAuthenticate(request.getEmail(),request.getPassword());
        User user = (User)userDetailsService.loadUserByUsername(request.getEmail());
        //generate token
        String token = jwtHelper.generateToken(user);
        //send token in response

        //refresh token
        RefreshTokenDto refreshToken = refreshTokenService.createRefreshToken(user.getEmail());
        JwtResponse jwtResponse = JwtResponse
                .builder()
                .token(token)
                .user(modelMapper.map(user, UserDto.class))
                .refreshToken(refreshToken)
                .build();
        return ResponseEntity.ok(jwtResponse);
    }

    private void doAuthenticate(String email, String password) {
        try{
            Authentication authentication = new UsernamePasswordAuthenticationToken(email,password);
            authenticationManager.authenticate(authentication);
        }catch (BadCredentialsException ex){
            throw new BadCredentialsException("Invalid Username and Password !!");
        }
    }

    //handle login with google
    @PostMapping("/login-with-google")
    public ResponseEntity<JwtResponse> handleGoogleLogin(@RequestBody GoogleLoginRequest loginRequest) throws GeneralSecurityException, IOException {
        logger.info("Id Token : {}",loginRequest.getIdToken());

        //token verify
       GoogleIdTokenVerifier verifier =  new GoogleIdTokenVerifier.Builder(new ApacheHttpTransport(),new GsonFactory()).setAudience(List.of(googleClientId)).build();

        GoogleIdToken googleIdToken = verifier.verify(loginRequest.getIdToken());

        if(googleIdToken != null){
            //token verified
            GoogleIdToken.Payload payload = googleIdToken.getPayload();
            String email=payload.getEmail();
            String userName=payload.getSubject();
            String name=(String) payload.get("name");
            String pictureUrl=(String) payload.get("picture");
            String locale = (String) payload.get("locale");
            String familyName=(String) payload.get("family_name");
            String givenName=(String) payload.get("given_name");

            UserDto userDto=new UserDto();
            userDto.setName(name);
            userDto.setEmail(email);
            userDto.setImageName(pictureUrl);
            userDto.setPassword(googleProviderDefaultPassword);
            userDto.setAbout("User is created using google");
            userDto.setProvider(Providers.GOOGLE);

            UserDto user=null;
            try {
                user = userService.getUserByEmail(userDto.getEmail());

                if(user.getProvider().equals(userDto.getProvider())){
                    //continue

                }else{
                    throw new BadCredentialsException("Your email is already registered !!");
                }
            }catch (ResourceNotFoundException ex) {
                user = userService.createUser(userDto);
            }

            this.doAuthenticate(user.getEmail(),userDto.getPassword());
            User user1=modelMapper.map(user,User.class);
            String token = jwtHelper.generateToken(user1);
            //send token in response
            JwtResponse jwtResponse = JwtResponse.builder().token(token).user(user).build();
            return ResponseEntity.ok(jwtResponse);

        }else {
            logger.info("Token is invalid !!");
            throw new BadApiRequestException("Invalid Google User !!");
        }

    }

}
