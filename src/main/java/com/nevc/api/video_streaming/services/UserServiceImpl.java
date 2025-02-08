package com.nevc.api.video_streaming.services;

import com.nevc.api.video_streaming.entities.User;
import com.nevc.api.video_streaming.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public User getLoggedInUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userEmailAddress;
        if (principal instanceof UserDetails) {
            userEmailAddress = ((UserDetails) principal).getUsername();
        } else {
            userEmailAddress = principal.toString();
        }
        return findByEmail(userEmailAddress).orElse(null);
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return null;
        }

        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(),
                user.getRole().getAuthorities());
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }
}
