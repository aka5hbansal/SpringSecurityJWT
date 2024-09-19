package com.example.jwtSecurity.dto;

import com.example.jwtSecurity.model.User;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String email;
    private String phoneNumber;
    private List<AddressDTO> addresses;

    public UserDTO() {
    }

    public UserDTO(User user) {
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhoneNumber();

        // Map addresses from User entity to AddressDTO
        if (user.getAddresses() != null) {
            this.addresses = user.getAddresses().stream()
                    .map(address -> new AddressDTO(address.getStreet(), address.getCity(), address.getState(), address.getZipCode()))
                    .collect(Collectors.toList());
        }
    }


    // Getters and Setters

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public List<AddressDTO> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<AddressDTO> addresses) {
        this.addresses = addresses;
    }
}

