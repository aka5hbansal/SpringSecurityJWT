package com.example.jwtSecurity.service;

import com.example.jwtSecurity.dto.UserDTO;
import com.example.jwtSecurity.model.*;
import com.example.jwtSecurity.repository.TokenRepository;
import com.example.jwtSecurity.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService, TokenRepository tokenRepository) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenRepository = tokenRepository;
    }

    public AuthenticationResponse register(UserDTO request) {
        if (repository.findByUsername(request.getUsername()).isPresent()) {
            return new AuthenticationResponse(null, null, "User already exists");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(Role.USER);

        User savedUser = repository.save(user);

        List<Address> addresses = request.getAddresses().stream()
                .map(dto -> {
                    Address address = new Address();
                    address.setStreet(dto.getStreet());
                    address.setCity(dto.getCity());
                    address.setState(dto.getState());
                    address.setZipCode(dto.getZipCode());
                    address.setUser(savedUser); // Set the user for address
                    return address;
                }).collect(Collectors.toList());

        savedUser.setAddresses(addresses);
        user = repository.save(savedUser);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        saveUserToken(accessToken, refreshToken, user);

        return new AuthenticationResponse(accessToken, refreshToken, "User registration was successful");
    }

    private void saveUserToken(String accessToken, String refreshToken, User user) {
        Token token = new Token();
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setLoggedOut(false);
        token.setUser(user);
        tokenRepository.save(token);
    }
}
