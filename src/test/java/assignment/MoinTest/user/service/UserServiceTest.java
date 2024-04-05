package assignment.MoinTest.user.service;

import assignment.MoinTest.Response.BaseResponse;
import assignment.MoinTest.Response.LoginResponse;
import assignment.MoinTest.user.dto.LoginRequestDto;
import assignment.MoinTest.user.dto.SignupRequestDto;
import assignment.MoinTest.user.entity.User;
import assignment.MoinTest.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private UserService userService;

    @Test
    void signup() {

        SignupRequestDto signupRequestDto = new SignupRequestDto();
        signupRequestDto.setUserId("test@naver.com");
        signupRequestDto.setPassword("1234");
        signupRequestDto.setName("Test User");
        signupRequestDto.setIdType("REG_NO");
        signupRequestDto.setIdValue("123456789");

        ResponseEntity<BaseResponse> response = userService.signup(signupRequestDto);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("OK", response.getBody().getResultMsg());

    }

    @Test
    void login() {

        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setUserId("test@naver.com");
        loginRequestDto.setPassword("1234");

        ResponseEntity<LoginResponse> response = userService.login(loginRequestDto);

        assertEquals(200, response.getBody().getResultCode());
        assertEquals("OK", response.getBody().getResultMsg());

    }
}