package assignment.MoinTest.user.service;

import assignment.MoinTest.Response.BaseResponse;
import assignment.MoinTest.Response.LoginResponse;
import assignment.MoinTest.user.dto.LoginRequestDto;
import assignment.MoinTest.user.dto.SignupRequestDto;
import assignment.MoinTest.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    public void clearUser(){
        userRepository.deleteAll();
    }

    @Test
    void testSignup_REG_200() {
        //given
        SignupRequestDto signupRequestDto = new SignupRequestDto();
        signupRequestDto.setUserId("testREG@naver.com");
        signupRequestDto.setPassword("1234");
        signupRequestDto.setName("REGUser");
        signupRequestDto.setIdType("REG_NO");
        signupRequestDto.setIdValue("020315-1234567");

        //then
        ResponseEntity<BaseResponse> response = userService.signup(signupRequestDto);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("OK", response.getBody().getResultMsg());
    }

    @Test
    void testSignup_REG_duplicateUserId_400() {
        //given
        SignupRequestDto signupRequestDto = new SignupRequestDto();
        signupRequestDto.setUserId("testREG@naver.com");
        signupRequestDto.setPassword("1234");
        signupRequestDto.setName("REGUser");
        signupRequestDto.setIdType("REG_NO");
        signupRequestDto.setIdValue("020315-1234567");
        userService.signup(signupRequestDto);

        //when
        SignupRequestDto signupRequestDtoDuplicate = new SignupRequestDto();
        signupRequestDtoDuplicate.setUserId("testREG@naver.com");
        signupRequestDtoDuplicate.setPassword("12341234");
        signupRequestDtoDuplicate.setName("newREGUser");
        signupRequestDtoDuplicate.setIdType("REG_NO");
        signupRequestDtoDuplicate.setIdValue("123456-1234567");

        //then
        ResponseEntity<BaseResponse> response = userService.signup(signupRequestDtoDuplicate);
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("중복된 UserID입니다.", response.getBody().getResultMsg());
    }

    @Test
    void testSignup_BUSI_200() {
        //given
        SignupRequestDto signupRequestDto = new SignupRequestDto();
        signupRequestDto.setUserId("testBUSI@naver.com");
        signupRequestDto.setPassword("1234");
        signupRequestDto.setName("BUSIUser");
        signupRequestDto.setIdType("BUSINESS_NO");
        signupRequestDto.setIdValue("123-01-01478");

        //then
        ResponseEntity<BaseResponse> response = userService.signup(signupRequestDto);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("OK", response.getBody().getResultMsg());
    }

    @Test
    void testSignup_BUSI_duplicateUserId_400() {
        //given
        SignupRequestDto signupRequestDto = new SignupRequestDto();
        signupRequestDto.setUserId("testBUSI@naver.com");
        signupRequestDto.setPassword("1234");
        signupRequestDto.setName("BUSIUser");
        signupRequestDto.setIdType("BUSINESS_NO");
        signupRequestDto.setIdValue("123-01-01478");
        userService.signup(signupRequestDto);

        //when
        SignupRequestDto signupRequestDtoDuplicate = new SignupRequestDto();
        signupRequestDtoDuplicate.setUserId("testBUSI@naver.com");
        signupRequestDtoDuplicate.setPassword("12341234");
        signupRequestDtoDuplicate.setName("newBUSIUser");
        signupRequestDtoDuplicate.setIdType("BUSINESS_NO");
        signupRequestDtoDuplicate.setIdValue("123-01-12345");
        userService.signup(signupRequestDtoDuplicate);

        //then
        ResponseEntity<BaseResponse> response = userService.signup(signupRequestDtoDuplicate);
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("중복된 UserID입니다.", response.getBody().getResultMsg());
    }

    @Test
    void testSignup_wrongIdType_400() {
        //given
        SignupRequestDto signupRequstDto = new SignupRequestDto();
        signupRequstDto.setUserId("test@naver.com");
        signupRequstDto.setPassword("1234");
        signupRequstDto.setName("TestIdTypeUser");
        signupRequstDto.setIdType("guest");
        signupRequstDto.setIdValue("123");

        //then
        ResponseEntity<BaseResponse> response = userService.signup(signupRequstDto);
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("idType이 옳지 않습니다.", response.getBody().getResultMsg());
    }

    @Test
    void testLogin_REG_200() {
        //given
        SignupRequestDto signupRequestDto = new SignupRequestDto();
        signupRequestDto.setUserId("testREG@naver.com");
        signupRequestDto.setPassword("1234");
        signupRequestDto.setName("REGUser");
        signupRequestDto.setIdType("REG_NO");
        signupRequestDto.setIdValue("020315-1234567");
        userService.signup(signupRequestDto);

        //when
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setUserId("testREG@naver.com");
        loginRequestDto.setPassword("1234");

        //then
        ResponseEntity<LoginResponse> response = userService.login(loginRequestDto);
        assertEquals(200, response.getBody().getResultCode());
        assertEquals("OK", response.getBody().getResultMsg());
    }

    @Test
    void testLogin_REG_wrongPassword_400() {
        //given
        SignupRequestDto signupRequestDto = new SignupRequestDto();
        signupRequestDto.setUserId("testREG@naver.com");
        signupRequestDto.setPassword("1234");
        signupRequestDto.setName("REGUser");
        signupRequestDto.setIdType("REG_NO");
        signupRequestDto.setIdValue("020315-1234567");
        userService.signup(signupRequestDto);

        //when
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setUserId("testREG@naver.com");
        loginRequestDto.setPassword("123444");

        //then
        ResponseEntity<LoginResponse> response = userService.login(loginRequestDto);
        assertEquals(400, response.getBody().getResultCode());
        assertEquals("비밀번호가 일치하지 않습니다.", response.getBody().getResultMsg());
    }

    @Test
    @Order(9)
    void testLogin_BUSI_200() {
        //given
        SignupRequestDto signupRequestDto = new SignupRequestDto();
        signupRequestDto.setUserId("testBUSI@naver.com");
        signupRequestDto.setPassword("1234");
        signupRequestDto.setName("BUSIUser");
        signupRequestDto.setIdType("BUSINESS_NO");
        signupRequestDto.setIdValue("123-01-01478");
        userService.signup(signupRequestDto);

        //when
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setUserId("testBUSI@naver.com");
        loginRequestDto.setPassword("1234");

        //then
        ResponseEntity<LoginResponse> response = userService.login(loginRequestDto);
        assertEquals(200, response.getBody().getResultCode());
        assertEquals("OK", response.getBody().getResultMsg());
    }

    @Test
    void testLogin_BUSI_wrongPassword_400() {
        //given
        SignupRequestDto signupRequestDto = new SignupRequestDto();
        signupRequestDto.setUserId("testBUSI@naver.com");
        signupRequestDto.setPassword("1234");
        signupRequestDto.setName("BUSIUser");
        signupRequestDto.setIdType("BUSINESS_NO");
        signupRequestDto.setIdValue("123-01-01478");
        userService.signup(signupRequestDto);

        //when
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setUserId("testBUSI@naver.com");
        loginRequestDto.setPassword("123444");

        //then
        ResponseEntity<LoginResponse> response = userService.login(loginRequestDto);
        assertEquals(400, response.getBody().getResultCode());
        assertEquals("비밀번호가 일치하지 않습니다.", response.getBody().getResultMsg());
    }

    @Test
    @Order(11)
    void testLogin_wrongUserId_400() {
        //given
        SignupRequestDto signupRequestDto = new SignupRequestDto();
        signupRequestDto.setUserId("testREG@naver.com");
        signupRequestDto.setPassword("1234");
        signupRequestDto.setName("REGUser");
        signupRequestDto.setIdType("REG_NO");
        signupRequestDto.setIdValue("020315-1234567");
        userService.signup(signupRequestDto);

        //when
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setUserId("wrong@naver.com");
        loginRequestDto.setPassword("1234");

        //then
        ResponseEntity<LoginResponse> response = userService.login(loginRequestDto);
        assertEquals(404, response.getBody().getResultCode());
        assertEquals("해당 유저 정보를 찾을 수 없습니다.", response.getBody().getResultMsg());
    }
}