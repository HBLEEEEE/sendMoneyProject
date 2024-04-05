package assignment.MoinTest.user.service;

import assignment.MoinTest.Response.BaseResponse;
import assignment.MoinTest.Response.LoginResponse;
import assignment.MoinTest.common.exception.ErrorCode;
import assignment.MoinTest.common.exception.SuccessCode;
import assignment.MoinTest.jwt.JwtUtil;
import assignment.MoinTest.user.dto.LoginRequestDto;
import assignment.MoinTest.user.dto.SignupRequestDto;
import assignment.MoinTest.user.entity.User;
import assignment.MoinTest.user.entity.UserIdTypeEnum;
import assignment.MoinTest.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public ResponseEntity<BaseResponse> signup(SignupRequestDto signupRequestDto) {

        //아이디가 이메일 형식인지 검사
        String regex = "[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(signupRequestDto.getUserId());

        if (matcher.matches() == false){
            return BaseResponse.toResponseEntity(ErrorCode.NOT_MATCH_EMAILFORM);
        }

        //아이디 중복 검사
        User found = userRepository.findByUserId(signupRequestDto.getUserId()).orElse(null);
        if (found!=null){
            return BaseResponse.toResponseEntity(ErrorCode.DUPLICATE_USER);
        }

        User user = new User();
        user.setUserId(signupRequestDto.getUserId());
        user.setPassword(passwordEncoder.encode(signupRequestDto.getPassword()));
        user.setName(signupRequestDto.getName());
        if (signupRequestDto.getIdType().equals("REG_NO")){
            user.setIdType(UserIdTypeEnum.REG_NO);
        }else if(signupRequestDto.getIdType().equals("BUSINESS_NO")){
            user.setIdType(UserIdTypeEnum.BUSINESS_NO);
        }else {
            return BaseResponse.toResponseEntity(ErrorCode.WRONG_IDTYPE);
        }
        user.setIdValue(passwordEncoder.encode(signupRequestDto.getIdValue()));

        userRepository.save(user);
        return BaseResponse.toResponseEntity(SuccessCode.SIGNUP_SUCCESS);


    }

    public ResponseEntity<LoginResponse> login(LoginRequestDto loginRequestDto) {

        Optional<User> found = userRepository.findByUserId(loginRequestDto.getUserId());
        if(!found.isPresent()){
            return LoginResponse.toResponseEntity(ErrorCode.USER_NOT_FOUND);
        }

        if (passwordEncoder.matches(loginRequestDto.getPassword(), found.get().getPassword())){

            String token = jwtUtil.createToken(found.get().getUserId(), found.get().getIdType());
            String split = token.split(" ")[1];

            return LoginResponse.toResponseEntity(SuccessCode.LOGIN_SUCCESS, split);
        }else {
            return LoginResponse.toResponseEntity(ErrorCode.INVALIDATION_PASSWORD);
        }

    }
}
