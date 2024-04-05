package assignment.MoinTest.user.controller;

import assignment.MoinTest.Response.BaseResponse;
import assignment.MoinTest.Response.LoginResponse;
import assignment.MoinTest.user.dto.LoginRequestDto;
import assignment.MoinTest.user.dto.SignupRequestDto;
import assignment.MoinTest.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<BaseResponse> signup(@RequestBody SignupRequestDto signupRequestDto) {

        return userService.signup(signupRequestDto);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequestDto loginRequestDto) {

        return userService.login(loginRequestDto);
    }
}
