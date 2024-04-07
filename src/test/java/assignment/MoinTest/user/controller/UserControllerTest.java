package assignment.MoinTest.user.controller;

import assignment.MoinTest.user.dto.LoginRequestDto;
import assignment.MoinTest.user.dto.SignupRequestDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    public static void init() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    public static void close() {
        validatorFactory.close();
    }

    @Test
    void testSignup_userIdValid_400() {
        //given
        SignupRequestDto signupRequestDto = new SignupRequestDto();
        signupRequestDto.setPassword("1234");
        signupRequestDto.setName("일반유저");
        signupRequestDto.setIdType("REG_NO");
        signupRequestDto.setIdValue("123456-1234657");

        //when
        signupRequestDto.setUserId("REGUsernaver.com");

        //then
        Set<ConstraintViolation<SignupRequestDto>> violations = validator.validate(signupRequestDto);
        ConstraintViolation<SignupRequestDto> passwordViolation = violations.stream()
                .filter(violation -> violation.getPropertyPath().toString().equals("userId"))
                .findFirst()
                .orElse(null);
        assertEquals("userID는 email 형식이어야 합니다.", passwordViolation.getMessage());
    }

    @Test
    void testSignup_passwordValid_400() {
        //given
        SignupRequestDto signupRequestDto = new SignupRequestDto();
        signupRequestDto.setUserId("REGUser@naver.com");
        signupRequestDto.setName("일반유저");
        signupRequestDto.setIdType("REG_NO");
        signupRequestDto.setIdValue("123456-1234657");

        //when
        signupRequestDto.setPassword("12");

        //then
        Set<ConstraintViolation<SignupRequestDto>> violations = validator.validate(signupRequestDto);
        ConstraintViolation<SignupRequestDto> passwordViolation = violations.stream()
                .filter(violation -> violation.getPropertyPath().toString().equals("password"))
                .findFirst()
                .orElse(null);
        assertEquals("password 최소 4자리, 최대 12자리입니다.", passwordViolation.getMessage());
    }

    @Test
    void testSignup_nameValid_400() {
        //given
        SignupRequestDto signupRequestDto = new SignupRequestDto();
        signupRequestDto.setUserId("REGUser@naver.com");
        signupRequestDto.setPassword("1234");
        signupRequestDto.setIdType("REG_NO");
        signupRequestDto.setIdValue("123456-1234657");

        //when
        signupRequestDto.setName("일");

        //then
        Set<ConstraintViolation<SignupRequestDto>> violations = validator.validate(signupRequestDto);
        ConstraintViolation<SignupRequestDto> passwordViolation = violations.stream()
                .filter(violation -> violation.getPropertyPath().toString().equals("name"))
                .findFirst()
                .orElse(null);
        assertEquals("name은 최소 2자리, 최대 10자리입니다.", passwordViolation.getMessage());
    }

    @Test
    void testSignup_idTypeValid_400() {
        //given
        SignupRequestDto signupRequestDto = new SignupRequestDto();
        signupRequestDto.setUserId("REGUser@naver.com");
        signupRequestDto.setPassword("1234");
        signupRequestDto.setName("일반유저");
        signupRequestDto.setIdValue("123456-1234657");

        //when
        signupRequestDto.setIdType("");

        //then
        Set<ConstraintViolation<SignupRequestDto>> violations = validator.validate(signupRequestDto);
        ConstraintViolation<SignupRequestDto> passwordViolation = violations.stream()
                .filter(violation -> violation.getPropertyPath().toString().equals("idType"))
                .findFirst()
                .orElse(null);
        assertEquals("idType는 공백이 될 수 없습니다.", passwordViolation.getMessage());
    }

    @Test
    void testSignup_idValueValid_400() {
        //given
        SignupRequestDto signupRequestDto = new SignupRequestDto();
        signupRequestDto.setUserId("REGUser@naver.com");
        signupRequestDto.setPassword("1234");
        signupRequestDto.setName("일반유저");
        signupRequestDto.setIdType("REG_NO");

        //when
        signupRequestDto.setIdValue("");

        //then
        Set<ConstraintViolation<SignupRequestDto>> violations = validator.validate(signupRequestDto);
        ConstraintViolation<SignupRequestDto> passwordViolation = violations.stream()
                .filter(violation -> violation.getPropertyPath().toString().equals("idValue"))
                .findFirst()
                .orElse(null);
        assertEquals("idValue는 공백이 될 수 없습니다.", passwordViolation.getMessage());
    }


    @Test
    void testLogin_userIdValid_400() {
        //given
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setPassword("12341234");

        //when
        loginRequestDto.setUserId("testtest.com");

        //then
        Set<ConstraintViolation<LoginRequestDto>> violations = validator.validate(loginRequestDto);
        ConstraintViolation<LoginRequestDto> passwordViolation = violations.stream()
                .filter(violation -> violation.getPropertyPath().toString().equals("userId"))
                .findFirst()
                .orElse(null);
        assertEquals("userID는 email 형식이어야 합니다.", passwordViolation.getMessage());
    }

    @Test
    void testLogin_passwordValid_400() {
        //given
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setUserId("test@test.com");

        //when
        loginRequestDto.setPassword("1234123412341234");


        //then
        Set<ConstraintViolation<LoginRequestDto>> violations = validator.validate(loginRequestDto);
        ConstraintViolation<LoginRequestDto> passwordViolation = violations.stream()
                .filter(violation -> violation.getPropertyPath().toString().equals("password"))
                .findFirst()
                .orElse(null);
        assertEquals("password 최소 2자리, 최대 12자리입니다.", passwordViolation.getMessage());
    }
}