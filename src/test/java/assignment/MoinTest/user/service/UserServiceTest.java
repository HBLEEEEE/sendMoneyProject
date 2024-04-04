package assignment.MoinTest.user.service;

import assignment.MoinTest.Response.BaseResponse;
import assignment.MoinTest.user.dto.SignupRequestDto;
import assignment.MoinTest.user.entity.User;
import assignment.MoinTest.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void signup() {

        SignupRequestDto signupRequestDto = new SignupRequestDto();
        signupRequestDto.setUserId("test@example.com");
        signupRequestDto.setPassword("password");
        signupRequestDto.setName("Test User");
        signupRequestDto.setIdType("REG_NO");
        signupRequestDto.setIdValue("123456789");

        when(userRepository.findByUserId("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        doReturn(null).when(userRepository).save(any(User.class));

        // when
        ResponseEntity<BaseResponse> response = userService.signup(signupRequestDto);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("OK", response.getBody().getResultMsg());
    }

    @Test
    void login() {
    }
}