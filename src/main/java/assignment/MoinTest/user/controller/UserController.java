package assignment.MoinTest.user.controller;
// TODO: 1. 컨벤션 지켜주면 코딩 해본놈이다 느낌들듯 https://docs.oracle.com/javase/tutorial/java/package/namingpkgs.html

// TODO: 2. opt + comd + l 자동정렬
// TODO: 3. ctrl + opt + o 미사용 임포트문 제거 둘다 커밋이나 기타 트리거에 따라 자동으로 해주게 설정도 가능 이후 파일에선 생략하겟음

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import assignment.MoinTest.Response.BaseResponse;
import assignment.MoinTest.Response.LoginResponse;
import assignment.MoinTest.user.dto.LoginRequestDto;
import assignment.MoinTest.user.dto.SignupRequestDto;
import assignment.MoinTest.user.service.UserService;
import lombok.RequiredArgsConstructor;

@RestController
// TODO: 4. @Controller + @ResponseBody -> @RestController 어노테이션 구현 보면 고대로 선언되어있음
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

	private final UserService userService;


	//TODO: 5 https://www.baeldung.com/spring-boot-bean-validation, https://docs.spring.io/spring-framework/reference/core/validation/beanvalidation.html
	// 첫번째 링크만 대충보고 자세하게 보고프면 두번째 일단 첫번째꺼보고 대충 구현 해보샘
	// 컨트롤러 레벨에서 client로부터 오는 데이터 유효성 체크를 해주면 좋음
	// null, length, empry, min, max등
	// 그다음에 서비스 레벨에서는 비지니스 로직에 대한 유효성 체크하면댐 이미 존재하는 사용자인지 등등
	@PostMapping("/signup")
	public ResponseEntity<BaseResponse> signup(@RequestBody SignupRequestDto signupRequestDto) {

		return userService.signup(signupRequestDto);
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@RequestBody LoginRequestDto loginRequestDto) {

		return userService.login(loginRequestDto);
	}
}