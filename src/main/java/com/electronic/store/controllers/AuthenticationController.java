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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/auth")
@Tag(name = "AuthController", description = "APIs for Authentication !!")
@SecurityRequirement(name = "scheme1")
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


    //generate token when login
    @PostMapping("/generate-token")
    public ResponseEntity<JwtResponse> login(@RequestBody JwtRequest request, HttpServletResponse response) {
        this.doAuthenticate(request.getEmail(), request.getPassword());
        User user = (User) userDetailsService.loadUserByUsername(request.getEmail());

        String jwtToken = jwtHelper.generateToken(user);
        RefreshTokenDto refreshToken = refreshTokenService.createRefreshToken(user.getEmail());

        // ✅ Send refreshToken in HttpOnly cookie
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken.getToken())
                .httpOnly(true)
                .secure(true)        // enable in production (HTTPS)
                .sameSite("Strict")
                .path("/auth")       // cookie valid for auth APIs
                .maxAge(7 * 24 * 60 * 60) // 7 days
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        JwtResponse jwtResponse = JwtResponse.builder()
                .token(jwtToken)
                .user(modelMapper.map(user, UserDto.class))
                .build();

        return ResponseEntity.ok(jwtResponse);
    }


    //to regenerate token through refreshToken
    @PostMapping("/regenerate-token")
    public ResponseEntity<JwtResponse> regenerateToken(HttpServletRequest request, HttpServletResponse response) {
        // ✅ Get refreshToken from cookie
        Cookie[] cookies = request.getCookies();
        if (cookies == null) throw new RuntimeException("No cookies found");

        String refreshTokenValue = Arrays.stream(cookies)
                .filter(c -> "refreshToken".equals(c.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        RefreshTokenDto refreshTokenDto = refreshTokenService.findByToken(refreshTokenValue);
        RefreshTokenDto verifiedToken = refreshTokenService.verifyRefreshToken(refreshTokenDto);
        UserDto user = refreshTokenService.getUser(verifiedToken);

        String jwtToken = jwtHelper.generateToken(modelMapper.map(user, User.class));
        RefreshTokenDto newRefreshToken = refreshTokenService.createRefreshToken(user.getEmail());

        // ✅ Replace cookie with new refresh token
        ResponseCookie cookie = ResponseCookie.from("refreshToken", newRefreshToken.getToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/auth")
                .maxAge(7 * 24 * 60 * 60)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        JwtResponse jwtResponse = JwtResponse.builder()
                .token(jwtToken)
                .user(user)
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

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Arrays.stream(cookies)
                    .filter(c -> "refreshToken".equals(c.getName()))
                    .findFirst()
                    .ifPresent(c -> refreshTokenService.deleteByToken(c.getValue())); // Invalidate token in DB
        }

        // Clear refreshToken cookie
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/auth")
                .maxAge(0) // expire immediately
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok("Logged out successfully");
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
          //  userDto.setAbout("User is created using google");
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
