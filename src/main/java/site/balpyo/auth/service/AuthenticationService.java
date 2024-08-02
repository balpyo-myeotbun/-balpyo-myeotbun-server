package site.balpyo.auth.service;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import site.balpyo.auth.entity.User;
import site.balpyo.auth.repository.UserRepository;
import site.balpyo.common.dto.CommonResponse;

import java.util.Optional;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;

    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User authenticationToUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof UserDetailsImpl)) {
            throw new IllegalArgumentException("Principal is not an instance of UserDetailsImpl");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) principal;
        Long userId = userDetails.getId();

        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }

        return optionalUser.get();
    }

}
