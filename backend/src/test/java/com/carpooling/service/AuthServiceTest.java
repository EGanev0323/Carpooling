package com.carpooling.service;

import com.carpooling.dto.RegisterRequest;
import com.carpooling.dto.UserResponse;
import com.carpooling.entity.User;
import com.carpooling.exception.DuplicateResourceException;
import com.carpooling.mapper.UserMapper;
import com.carpooling.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest validRequest;
    private User savedUser;
    private UserResponse expectedResponse;

    @BeforeEach
    void setUp() {
        validRequest = new RegisterRequest(
                "testuser", "test@example.com", "password123",
                "Test", "User", "0888000000", false);

        savedUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("encoded_password")
                .firstName("Test")
                .lastName("User")
                .isDriver(false)
                .isPassenger(true)
                .isActive(true)
                .avgRating(BigDecimal.ZERO)
                .ratingCount(0)
                .tripsAsDriver(0)
                .tripsAsPassenger(0)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        expectedResponse = new UserResponse(1L, "testuser", "test@example.com",
                "Test", "User", "0888000000", null,
                false, true, false, BigDecimal.ZERO, 0, 0, 0, savedUser.getCreatedAt());
    }

    @Test
    void register_withValidRequest_returnsUserResponse() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userMapper.toEntity(validRequest)).thenReturn(savedUser);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toResponse(savedUser)).thenReturn(expectedResponse);

        UserResponse result = authService.register(validRequest);

        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo("testuser");
        assertThat(result.email()).isEqualTo("test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_withDuplicateUsername_throwsDuplicateResourceException() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(validRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("testuser");

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_withDuplicateEmail_throwsDuplicateResourceException() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(validRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("test@example.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_encodesPassword() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.toEntity(validRequest)).thenReturn(savedUser);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toResponse(savedUser)).thenReturn(expectedResponse);

        authService.register(validRequest);

        verify(passwordEncoder).encode("password123");
    }
}
