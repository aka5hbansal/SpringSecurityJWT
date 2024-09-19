package com.example.jwtSecurity.service;

import com.example.jwtSecurity.dto.AddressDTO;
import com.example.jwtSecurity.dto.UserDTO;
import com.example.jwtSecurity.model.*;
import com.example.jwtSecurity.repository.TokenRepository;
import com.example.jwtSecurity.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    public UserDTO getProfile() {
        // Step 1: Get the currently authenticated user's username
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // This will fetch the username from the JWT token

        // Step 2: Fetch the user by username from the database
        User user = repository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));


        UserDTO userProfileDTO = new UserDTO();
        userProfileDTO.setFirstName(user.getFirstName());
        userProfileDTO.setLastName(user.getLastName());
        userProfileDTO.setUsername(user.getUsername());
        userProfileDTO.setEmail(user.getEmail());
        userProfileDTO.setPhoneNumber(user.getPhoneNumber());

        List<AddressDTO> addressDTOs = user.getAddresses().stream().map(address -> {
            AddressDTO addressDTO = new AddressDTO();
            addressDTO.setStreet(address.getStreet());
            addressDTO.setCity(address.getCity());
            addressDTO.setState(address.getState());
            addressDTO.setZipCode(address.getZipCode());
            return addressDTO;
        }).collect(Collectors.toList());

        userProfileDTO.setAddresses(addressDTOs);

        return userProfileDTO;
    }

    public List<User> getAllNonAdminUsers() {
        return repository.findAll()
                .stream()
                .filter(user -> user.getRole() != Role.ADMIN)
                .collect(Collectors.toList());
    }

    public UserDTO updateUser(UserDTO updatedUserDTO) {
        // Get the current logged-in user
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = repository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update user details
        currentUser.setFirstName(updatedUserDTO.getFirstName());
        currentUser.setLastName(updatedUserDTO.getLastName());
        currentUser.setEmail(updatedUserDTO.getEmail());
        currentUser.setPhoneNumber(updatedUserDTO.getPhoneNumber());

        if (updatedUserDTO.getAddresses() != null) {
            List<Address> existingAddresses = currentUser.getAddresses();

            // Remove addresses that are not in the updated list
            List<Address> addressesToRemove = existingAddresses.stream()
                    .filter(existingAddress -> updatedUserDTO.getAddresses().stream()
                            .noneMatch(updatedAddress -> updatedAddress.getId() != null && updatedAddress.getId().equals(existingAddress.getId())))
                    .collect(Collectors.toList());

            addressesToRemove.forEach(address -> {
                address.setUser(null);
                // Optional: if you want to remove from DB, you can do it here
                // addressRepository.delete(address);
            });

            // Update existing addresses and add new addresses
            List<Address> updatedAddresses = updatedUserDTO.getAddresses().stream()
                    .map(dto -> {
                        Address address;
                        if (dto.getId() != null) {
                            // Update existing address
                            address = existingAddresses.stream()
                                    .filter(a -> a.getId().equals(dto.getId()))
                                    .findFirst()
                                    .orElseThrow(() -> new RuntimeException("Address not found"));
                        } else {
                            // Create new address
                            address = new Address();
                        }
                        address.setStreet(dto.getStreet());
                        address.setCity(dto.getCity());
                        address.setState(dto.getState());
                        address.setZipCode(dto.getZipCode());
                        address.setUser(currentUser);
                        return address;
                    }).collect(Collectors.toList());

            currentUser.setAddresses(updatedAddresses);
        }

        // Save the updated user
        User updatedUser = repository.save(currentUser);

        // Return updated user details as DTO
        return new UserDTO(updatedUser);
    }
}
