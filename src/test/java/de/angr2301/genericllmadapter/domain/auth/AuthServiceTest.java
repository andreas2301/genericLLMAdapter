package de.angr2301.genericllmadapter.domain.auth;

import de.angr2301.genericllmadapter.config.JwtService;
import de.angr2301.genericllmadapter.domain.user.Role;
import de.angr2301.genericllmadapter.domain.user.User;
import de.angr2301.genericllmadapter.domain.user.UserRepository;
import de.angr2301.genericllmadapter.dto.auth.AuthenticationRequest;
import de.angr2301.genericllmadapter.dto.auth.AuthenticationResponse;
import de.angr2301.genericllmadapter.dto.auth.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Tests für AuthService
 * Testet Registrierung, Login und JWT-Token-Generierung
 */

class AuthServiceTest {

}