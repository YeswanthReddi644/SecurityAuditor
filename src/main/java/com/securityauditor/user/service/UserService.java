package com.securityauditor.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.securityauditor.user.dto.CreateUserRequest;
import com.securityauditor.user.dto.UserResponse;
import com.securityauditor.user.entity.User;
import com.securityauditor.user.exception.EmailAlreadyExistsException;
import com.securityauditor.user.repository.UserRepository;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(
	        UserRepository userRepository,
	        PasswordEncoder passwordEncoder) {

	    this.userRepository = userRepository;
	    this.passwordEncoder = passwordEncoder;
	}

    public UserResponse createUser(CreateUserRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
        	throw new EmailAlreadyExistsException("Email already exists");
        }

        String hashedPassword =
                passwordEncoder.encode(request.getPassword());

        User user = new User(
                request.getName(),
                request.getEmail(),
                hashedPassword
        );

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail()
        );
    }
}