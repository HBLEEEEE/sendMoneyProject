package assignment.MoinTest;

import assignment.MoinTest.Response.BaseResponse;
import assignment.MoinTest.Response.ListResponse;
import assignment.MoinTest.Response.LoginResponse;
import assignment.MoinTest.Response.QuoteResponse;
import assignment.MoinTest.security.UserDetailsImpl;
import assignment.MoinTest.transfer.dto.HistoryResponseDto;
import assignment.MoinTest.transfer.dto.QuoteRequestDto;
import assignment.MoinTest.transfer.dto.QuoteResponseDto;
import assignment.MoinTest.transfer.dto.RequestDto;
import assignment.MoinTest.transfer.entity.Request;
import assignment.MoinTest.transfer.repository.RequestRepository;
import assignment.MoinTest.transfer.service.QuoteService;
import assignment.MoinTest.user.dto.LoginRequestDto;
import assignment.MoinTest.user.dto.SignupRequestDto;
import assignment.MoinTest.user.entity.User;
import assignment.MoinTest.user.repository.UserRepository;
import assignment.MoinTest.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class MoinTestApplicationTests {

	@Autowired
	private UserService userService;

	@Autowired
	private QuoteService quoteService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RequestRepository requestRepository;

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

		ResponseEntity<BaseResponse> response2 = userService.signup(signupRequestDto);

		assertEquals(400, response2.getStatusCodeValue());
		assertEquals("중복된 UserID입니다.", response2.getBody().getResultMsg());

	}

	@Test
	void login() {

		LoginRequestDto loginRequestDto = new LoginRequestDto();
		loginRequestDto.setUserId("test@naver.com");
		loginRequestDto.setPassword("1234");

		ResponseEntity<LoginResponse> response = userService.login(loginRequestDto);

		assertEquals(200, response.getBody().getResultCode());
		assertEquals("OK", response.getBody().getResultMsg());

		LoginRequestDto loginRequestDto2 = new LoginRequestDto();
		loginRequestDto2.setUserId("test@naver.com");
		loginRequestDto2.setPassword("123444");

		ResponseEntity<LoginResponse> response2 = userService.login(loginRequestDto2);

		assertEquals(400, response2.getBody().getResultCode());
		assertEquals("비밀번호가 일치하지 않습니다.", response2.getBody().getResultMsg());

	}

	@Test
	void orderQuote() {

		User user = userRepository.findByUserId("test@naver.com").orElse(null);
		UserDetailsImpl userDetails = new UserDetailsImpl(user, "test@naver.com");

		QuoteRequestDto quoteRequestDto = new QuoteRequestDto();
		quoteRequestDto.setAmount(300000);
		quoteRequestDto.setTargetCurrency("JPY");

		ResponseEntity<QuoteResponse> response = quoteService.orderQuote(quoteRequestDto, userDetails);

		assertEquals(200, response.getBody().getResultCode());
		assertEquals("OK", response.getBody().getResultMsg());

		QuoteRequestDto quoteRequestDto2 = new QuoteRequestDto();
		quoteRequestDto2.setAmount(999999999); // USD/KRW 환율에 정말 큰 문제가 발생하지 않는 한 문제 안남
		quoteRequestDto2.setTargetCurrency("USD");

		ResponseEntity<QuoteResponse> response2 = quoteService.orderQuote(quoteRequestDto2, userDetails);

		assertEquals(200, response2.getBody().getResultCode());
		assertEquals("OK", response2.getBody().getResultMsg());


		QuoteRequestDto quoteRequestDto3 = new QuoteRequestDto();
		quoteRequestDto3.setAmount(100000);
		quoteRequestDto3.setTargetCurrency("JPYYYY");

		ResponseEntity<QuoteResponse> response3 = quoteService.orderQuote(quoteRequestDto3, userDetails);

		assertEquals(400, response3.getBody().getResultCode());
		assertEquals("JPY 혹은 USD가 아닙니다.", response3.getBody().getResultMsg());

	}

	@Test
	void requestQuote() {

		User user = userRepository.findByUserId("test@naver.com").orElse(null);
		UserDetailsImpl userDetails = new UserDetailsImpl(user, "test@naver.com");

		RequestDto requestDto = new RequestDto();
		requestDto.setQuoteId((long)1);

		ResponseEntity<BaseResponse> response = quoteService.requestQuote(requestDto, userDetails);

		assertEquals(200, response.getBody().getResultCode());
		assertEquals("OK", response.getBody().getResultMsg());

		RequestDto requestDto2 = new RequestDto();
		requestDto2.setQuoteId((long)2);

		ResponseEntity<BaseResponse> response2 = quoteService.requestQuote(requestDto2, userDetails);

		assertEquals(400, response2.getBody().getResultCode());
		assertEquals("일일 송금 최대치를 초과합니다.", response2.getBody().getResultMsg());


	}

	@Test
	void getHistories() {

		User user = userRepository.findByUserId("test@naver.com").orElse(null);
		UserDetailsImpl userDetails = new UserDetailsImpl(user, "test@naver.com");

		ResponseEntity<ListResponse> response = quoteService.getHistories(userDetails);

		LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN); // 오늘 날짜의 시작 시간
		LocalDateTime endOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MAX); // 오늘 날짜의 종료 시간
		List<Request> requests = requestRepository.findByRequestTimeBetween(startOfToday, endOfToday);

		int cnt = 0;
		double total = 0;

		for (int i = 0; i < requests.size(); i++) {
			cnt++;
			total += requests.get(i).getQuote().getUsdAmount();
		}

		assertEquals(200, response.getBody().getResultCode());
		assertEquals("OK", response.getBody().getResultMsg());
		assertEquals(cnt, response.getBody().getTodayTransferCount());
		assertEquals(total, response.getBody().getTodayTransferUsdAmount());



	}

}
