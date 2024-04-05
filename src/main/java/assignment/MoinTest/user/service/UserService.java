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

	// TODO: 1. 재사용 가능할지 구글에 xxx thread safe 라고 쳐보면 애들이 알려줌 가능하다면 재사용 하면 좋음
    private static final String EMAIL_REGEX = "[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$";
    private static final Pattern EMAIL_REGEX_PATTERN = Pattern.compile(EMAIL_REGEX);


    @Transactional
    public ResponseEntity<BaseResponse> signup(SignupRequestDto signupRequestDto) {

		// TODO: 2 예외 처리는 https://spring.io/blog/2013/11/01/exception-handling-in-spring-mvc 여기를 참고해보면 좋음
		// 지금같이 controller -> service -> repository 뎁스 정도면 괜찮은데 svc1 -> svc2 -> svc3 계속 들어가면 svc에서 예외발생했을때 다시 3->2->1로 예외 응답 보내야하는 문제
		// + BaseResponse라는 뷰모델이 서비스 레이어에 흩어져 의존성 생김 -> 수정어려워짐 -> 대갈 아파짐
		// 위 링크의 컨트롤러어드바이스 구현해서 서비스레이어에서는 적절한 예외와 예외 코드를 던지면 어드바이스에서 잡아서 적절하게 응답하면 아주 좋음
		// 시간있으면 구현해보샘

        //아이디가 이메일 형식인지 검사
        Matcher matcher = EMAIL_REGEX_PATTERN.matcher(signupRequestDto.getUserId());

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

		// TODO: 3. if else 많이 쓰는거보다 이런식으로 하면 깔끔할듯 그리고 메서드 추출을 좀 하면 좋을듯
		// 위에는 checkParams(이메일 체크, 아이디 중복 검사), registerUser(user 생성, repo insert)
		UserIdTypeEnum userIdType = UserIdTypeEnum.findById(signupRequestDto.getIdType());
		if (userIdType == null) {
			return BaseResponse.toResponseEntity(ErrorCode.WRONG_IDTYPE);
		}
        // if (signupRequestDto.getIdType().equals("REG_NO")){
        //     user.setIdType(UserIdTypeEnum.REG_NO);
        // }else if(signupRequestDto.getIdType().equals("BUSINESS_NO")){
        //     user.setIdType(UserIdTypeEnum.BUSINESS_NO);
        // }else {
        //     return BaseResponse.toResponseEntity(ErrorCode.WRONG_IDTYPE);
        // }
        user.setIdValue(passwordEncoder.encode(signupRequestDto.getIdValue()));

        userRepository.save(user);
        return BaseResponse.toResponseEntity(SuccessCode.SIGNUP_SUCCESS);


    }

    public ResponseEntity<LoginResponse> login(LoginRequestDto loginRequestDto) {

        Optional<User> found = userRepository.findByUserId(loginRequestDto.getUserId());
        if(found.isEmpty()){
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
