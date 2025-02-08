package com.nevc.api.video_streaming.services;

import com.nevc.api.video_streaming.entities.User;
import com.nevc.api.video_streaming.enums.Role;
import com.nevc.api.video_streaming.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testGetLoggedInUser_ValidUserDetails() {
        UserDetails userDetails = mock(UserDetails.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");
        User user = new User();
        user.setEmail("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        User result = userService.getLoggedInUser();

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void testGetLoggedInUser_InvalidUserDetails() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        User result = userService.getLoggedInUser();

        assertNull(result);
    }

    @Test
    void testLoadUserByUsername_ValidUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole(Role.ADMIN);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = userService.loadUserByUsername("test@example.com");

        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertEquals(Role.ADMIN.getAuthorities(), userDetails.getAuthorities());
    }

    @Test
    void testLoadUserByUsername_InvalidUser() {
        when(userRepository.findByEmail("invalid@example.com")).thenReturn(Optional.empty());

        UserDetails userDetails = userService.loadUserByUsername("invalid@example.com");

        assertNull(userDetails);
    }

    @Test
    void testFindByEmail() {
        User user = new User();
        user.setEmail("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByEmail("test@example.com");

        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    void testSaveUser() {
        User user = new User();
        user.setEmail("test@example.com");
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.saveUser(user);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }
}
