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
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MoinTestApplicationTests {

	@Autowired
	private UserService userService;

	@Autowired
	private QuoteService quoteService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RequestRepository requestRepository;

	// quote API test에서 만든 quote를 request API test에서 사용하기 위한 List들
	private static List<Long> REGUserQuoteNums = new ArrayList<>();

	private static List<Long> BUSIUserQuoteNums = new ArrayList<>();

	@Test
	@Order(1)
	void testSignup_REG_200() {
		SignupRequestDto signupRequestDto = new SignupRequestDto();
		signupRequestDto.setUserId("testREG@naver.com");
		signupRequestDto.setPassword("1234");
		signupRequestDto.setName("REGUser");
		signupRequestDto.setIdType("REG_NO");
		signupRequestDto.setIdValue("020315-1234567");

		ResponseEntity<BaseResponse> response = userService.signup(signupRequestDto);
		assertEquals(200, response.getStatusCodeValue());
		assertEquals("OK", response.getBody().getResultMsg());
	}

	@Test
	@Order(2)
	void testSignup_REG_400() {
		SignupRequestDto signupRequestDto = new SignupRequestDto();
		signupRequestDto.setUserId("testREG@naver.com");
		signupRequestDto.setPassword("1234");
		signupRequestDto.setName("REGUser");
		signupRequestDto.setIdType("REG_NO");
		signupRequestDto.setIdValue("020315-1234567");

		ResponseEntity<BaseResponse> response = userService.signup(signupRequestDto);
		assertEquals(400, response.getStatusCodeValue());
		assertEquals("중복된 UserID입니다.", response.getBody().getResultMsg());
	}
	
	@Test
	@Order(3)
	void testSignup_BUSI_200() {
		SignupRequestDto signupBUSI = new SignupRequestDto();
		signupBUSI.setUserId("testBUSI@naver.com");
		signupBUSI.setPassword("1234");
		signupBUSI.setName("BUSIUser");
		signupBUSI.setIdType("BUSINESS_NO");
		signupBUSI.setIdValue("123-01-01478");

		ResponseEntity<BaseResponse> response = userService.signup(signupBUSI);
		assertEquals(200, response.getStatusCodeValue());
		assertEquals("OK", response.getBody().getResultMsg());
	}

	@Test
	@Order(4)
	void testSignup_BUSI_400() {
		SignupRequestDto signupRequestDto = new SignupRequestDto();
		signupRequestDto.setUserId("testBUSI@naver.com");
		signupRequestDto.setPassword("1234");
		signupRequestDto.setName("BUSIUser");
		signupRequestDto.setIdType("BUSINESS_NO");
		signupRequestDto.setIdValue("123-01-01478");
		
		ResponseEntity<BaseResponse> response = userService.signup(signupRequestDto);
		assertEquals(400, response.getStatusCodeValue());
		assertEquals("중복된 UserID입니다.", response.getBody().getResultMsg());
	}

	@Test
	@Order(5)
	void testSignup_WrongEmail_400() {
		SignupRequestDto signupWrongEmail = new SignupRequestDto();
		signupWrongEmail.setUserId("test@navercom");
		signupWrongEmail.setPassword("1234");
		signupWrongEmail.setName("TestEmailUser");
		signupWrongEmail.setIdType("REG_NO");
		signupWrongEmail.setIdValue("020315-1234567");

		ResponseEntity<BaseResponse> signupWrongEmailResponse = userService.signup(signupWrongEmail);
		assertEquals(400, signupWrongEmailResponse.getStatusCodeValue());
		assertEquals("이메일 형식에 맞지 않습니다.", signupWrongEmailResponse.getBody().getResultMsg());

		SignupRequestDto signupWrongEmail2 = new SignupRequestDto();
		signupWrongEmail2.setUserId("testnaver.com");
		signupWrongEmail2.setPassword("1234");
		signupWrongEmail2.setName("TestEmailUser2");
		signupWrongEmail2.setIdType("BUSINESS_NO");
		signupWrongEmail2.setIdValue("123-01-01478");

		ResponseEntity<BaseResponse> signupWrongEmailResponse2 = userService.signup(signupWrongEmail2);
		assertEquals(400, signupWrongEmailResponse2.getStatusCodeValue());
		assertEquals("이메일 형식에 맞지 않습니다.", signupWrongEmailResponse2.getBody().getResultMsg());
	}

	@Test
	@Order(6)
	void testSignup_WrongIdType_400() {
		SignupRequestDto signupRequstDto = new SignupRequestDto();
		signupRequstDto.setUserId("test@naver.com");
		signupRequstDto.setPassword("1234");
		signupRequstDto.setName("TestIdTypeUser");
		signupRequstDto.setIdType("guest");
		signupRequstDto.setIdValue("123");

		ResponseEntity<BaseResponse> response = userService.signup(signupRequstDto);
		assertEquals(400, response.getStatusCodeValue());
		assertEquals("idType이 옳지 않습니다.", response.getBody().getResultMsg());
	}

	@Test
	@Order(7)
	void testLogin_REG_200() {
		LoginRequestDto loginRequestDto = new LoginRequestDto();
		loginRequestDto.setUserId("testREG@naver.com");
		loginRequestDto.setPassword("1234");

		ResponseEntity<LoginResponse> response = userService.login(loginRequestDto);
		assertEquals(200, response.getBody().getResultCode());
		assertEquals("OK", response.getBody().getResultMsg());
	}
	@Test
	@Order(8)
	void testLogin_REG_WrongPassword_400() {
		LoginRequestDto loginRequestDto = new LoginRequestDto();
		loginRequestDto.setUserId("testREG@naver.com");
		loginRequestDto.setPassword("123444");

		ResponseEntity<LoginResponse> response = userService.login(loginRequestDto);
		assertEquals(400, response.getBody().getResultCode());
		assertEquals("비밀번호가 일치하지 않습니다.", response.getBody().getResultMsg());
	}

	@Test
	@Order(9)
	void testLogin_BUSI_200() {
		LoginRequestDto loginRequestDto = new LoginRequestDto();
		loginRequestDto.setUserId("testBUSI@naver.com");
		loginRequestDto.setPassword("1234");

		ResponseEntity<LoginResponse> response = userService.login(loginRequestDto);
		assertEquals(200, response.getBody().getResultCode());
		assertEquals("OK", response.getBody().getResultMsg());
	}

	@Test
	@Order(10)
	void testLogin_BUSI_WrongPassword_400() {

		LoginRequestDto loginRequestDto = new LoginRequestDto();
		loginRequestDto.setUserId("testBUSI@naver.com");
		loginRequestDto.setPassword("123444");

		ResponseEntity<LoginResponse> response = userService.login(loginRequestDto);
		assertEquals(400, response.getBody().getResultCode());
		assertEquals("비밀번호가 일치하지 않습니다.", response.getBody().getResultMsg());
	}

	@Test
	@Order(11)
	void testLogin_WrongUserId_400() {
		LoginRequestDto loginRequestDto = new LoginRequestDto();
		loginRequestDto.setUserId("wrong@naver.com");
		loginRequestDto.setPassword("1234");

		ResponseEntity<LoginResponse> response = userService.login(loginRequestDto);
		assertEquals(404, response.getBody().getResultCode());
		assertEquals("해당 유저 정보를 찾을 수 없습니다.", response.getBody().getResultMsg());
	}

	@Test
	@Order(12)
	void testQuote_REG_JPY_200() {
		User userREG = userRepository.findByUserId("testREG@naver.com").orElse(null);
		UserDetailsImpl userREGDetails = new UserDetailsImpl(userREG, "testREG@naver.com");
		
		QuoteRequestDto quoteRequestDto = new QuoteRequestDto();
		quoteRequestDto.setAmount(100000);
		quoteRequestDto.setTargetCurrency("JPY");

		ResponseEntity<QuoteResponse> response = quoteService.orderQuote(quoteRequestDto, userREGDetails);
		assertEquals(200, response.getBody().getResultCode());
		assertEquals("OK", response.getBody().getResultMsg());
		REGUserQuoteNums.add(response.getBody().getQuote().getQuoteId());
	}
	
	@Test
	@Order(13)
	void testQuote_REG_USD_200() {
		User userREG = userRepository.findByUserId("testREG@naver.com").orElse(null);
		UserDetailsImpl userREGDetails = new UserDetailsImpl(userREG, "testREG@naver.com");

		//USD 테스트
		QuoteRequestDto quoteRequestDto = new QuoteRequestDto();
		quoteRequestDto.setAmount(100000);
		quoteRequestDto.setTargetCurrency("USD");

		ResponseEntity<QuoteResponse> response = quoteService.orderQuote(quoteRequestDto, userREGDetails);
		assertEquals(200, response.getBody().getResultCode());
		assertEquals("OK", response.getBody().getResultMsg());
		REGUserQuoteNums.add(response.getBody().getQuote().getQuoteId());

		//USD 테스트2
		QuoteRequestDto quoteRequestDto2 = new QuoteRequestDto();
		quoteRequestDto2.setAmount(3000000);
		quoteRequestDto2.setTargetCurrency("USD");

		ResponseEntity<QuoteResponse> response2 = quoteService.orderQuote(quoteRequestDto2, userREGDetails);
		assertEquals(200, response2.getBody().getResultCode());
		assertEquals("OK", response2.getBody().getResultMsg());
		REGUserQuoteNums.add(response2.getBody().getQuote().getQuoteId());
	}

	@Test
	@Order(14)
	void testQuote_REG_WrongTargetCurrency_400() {
		User userREG = userRepository.findByUserId("testREG@naver.com").orElse(null);
		UserDetailsImpl userREGDetails = new UserDetailsImpl(userREG, "testREG@naver.com");

		QuoteRequestDto quoteRequestDto = new QuoteRequestDto();
		quoteRequestDto.setAmount(100000);
		quoteRequestDto.setTargetCurrency("EUR");

		ResponseEntity<QuoteResponse> response = quoteService.orderQuote(quoteRequestDto, userREGDetails);
		assertEquals(400, response.getBody().getResultCode());
		assertEquals("JPY 혹은 USD가 아닙니다.", response.getBody().getResultMsg());
	}

	@Test
	@Order(15)
	void testQuote_REG_Negative_400() {
		User userREG = userRepository.findByUserId("testREG@naver.com").orElse(null);
		UserDetailsImpl userREGDetails = new UserDetailsImpl(userREG, "testREG@naver.com");

		QuoteRequestDto quoteRequestDto = new QuoteRequestDto();
		quoteRequestDto.setAmount(-10000);
		quoteRequestDto.setTargetCurrency("JPY");

		ResponseEntity<QuoteResponse> response = quoteService.orderQuote(quoteRequestDto, userREGDetails);
		assertEquals(400, response.getBody().getResultCode());
		assertEquals("음수 발생 요청입니다.", response.getBody().getResultMsg());
	}

	@Test
	@Order(16)
	void testQuote_BUSI_JPY_200() {
		User userBUSI = userRepository.findByUserId("testBUSI@naver.com").orElse(null);
		UserDetailsImpl userBUSIDetails = new UserDetailsImpl(userBUSI, "testBUSI@naver.com");

		QuoteRequestDto quoteRequestDto = new QuoteRequestDto();
		quoteRequestDto.setAmount(300000);
		quoteRequestDto.setTargetCurrency("JPY");

		ResponseEntity<QuoteResponse> response = quoteService.orderQuote(quoteRequestDto, userBUSIDetails);
		assertEquals(200, response.getBody().getResultCode());
		assertEquals("OK", response.getBody().getResultMsg());
		BUSIUserQuoteNums.add(response.getBody().getQuote().getQuoteId());
	}

	@Test
	@Order(17)
	void testQuote_BUSI_USD_200() {
		User userBUSI = userRepository.findByUserId("testBUSI@naver.com").orElse(null);
		UserDetailsImpl userBUSIDetails = new UserDetailsImpl(userBUSI, "testBUSI@naver.com");

		//USD 테스트
		QuoteRequestDto quoteRequestDto = new QuoteRequestDto();
		quoteRequestDto.setAmount(300000);
		quoteRequestDto.setTargetCurrency("USD");

		ResponseEntity<QuoteResponse> response = quoteService.orderQuote(quoteRequestDto, userBUSIDetails);
		assertEquals(200, response.getBody().getResultCode());
		assertEquals("OK", response.getBody().getResultMsg());
		BUSIUserQuoteNums.add(response.getBody().getQuote().getQuoteId());

		//USD 테스트2
		QuoteRequestDto quoteRequestDto2 = new QuoteRequestDto();
		quoteRequestDto2.setAmount(80000000);
		quoteRequestDto2.setTargetCurrency("USD");

		ResponseEntity<QuoteResponse> response2 = quoteService.orderQuote(quoteRequestDto2, userBUSIDetails);
		assertEquals(200, response2.getBody().getResultCode());
		assertEquals("OK", response2.getBody().getResultMsg());
		BUSIUserQuoteNums.add(response2.getBody().getQuote().getQuoteId());
	}

	@Test
	@Order(18)
	void testQuote_BUSI_WrongTargetCurrency_400() {
		User userBUSI = userRepository.findByUserId("testBUSI@naver.com").orElse(null);
		UserDetailsImpl userBUSIDetails = new UserDetailsImpl(userBUSI, "testBUSI@naver.com");

		QuoteRequestDto quoteRequestDto = new QuoteRequestDto();
		quoteRequestDto.setAmount(100000);
		quoteRequestDto.setTargetCurrency("EUR");
		ResponseEntity<QuoteResponse> response = quoteService.orderQuote(quoteRequestDto, userBUSIDetails);
		assertEquals(400, response.getBody().getResultCode());
		assertEquals("JPY 혹은 USD가 아닙니다.", response.getBody().getResultMsg());
	}

	@Test
	@Order(19)
	void testQuote_BUSI_Negative_400() {
		User userBUSI = userRepository.findByUserId("testBUSI@naver.com").orElse(null);
		UserDetailsImpl userBUSIDetails = new UserDetailsImpl(userBUSI, "testBUSI@naver.com");
		
		QuoteRequestDto quoteRequestDto = new QuoteRequestDto();
		quoteRequestDto.setAmount(-10000);
		quoteRequestDto.setTargetCurrency("JPY");
		ResponseEntity<QuoteResponse> response = quoteService.orderQuote(quoteRequestDto, userBUSIDetails);
		assertEquals(400, response.getBody().getResultCode());
		assertEquals("음수 발생 요청입니다.", response.getBody().getResultMsg());
	}

	@Test
	@Order(20)
	void testRequest_REG_UnderLimit_200() {
		User userREG = userRepository.findByUserId("testREG@naver.com").orElse(null);
		UserDetailsImpl userREGDetails = new UserDetailsImpl(userREG, "testREG@naver.com");

		// 당일 한도 금액 내 JPY 테스트
		RequestDto requestDto = new RequestDto();
		requestDto.setQuoteId(REGUserQuoteNums.get(0));
		ResponseEntity<BaseResponse> response = quoteService.requestQuote(requestDto, userREGDetails);
		assertEquals(200, response.getBody().getResultCode());
		assertEquals("OK", response.getBody().getResultMsg());

		// 당일 한도 금액 내 USD 테스트
		RequestDto requestDto2 = new RequestDto();
		requestDto2.setQuoteId(REGUserQuoteNums.get(1));
		ResponseEntity<BaseResponse> response2 = quoteService.requestQuote(requestDto2, userREGDetails);
		assertEquals(200, response2.getBody().getResultCode());
		assertEquals("OK", response2.getBody().getResultMsg());
	}

	@Test
	@Order(21)
	void testRequest_REG_OverLimit_400() {
		User userREG = userRepository.findByUserId("testREG@naver.com").orElse(null);
		UserDetailsImpl userREGDetails = new UserDetailsImpl(userREG, "testREG@naver.com");

		RequestDto requestDto = new RequestDto();
		requestDto.setQuoteId(REGUserQuoteNums.get(2));
		ResponseEntity<BaseResponse> response = quoteService.requestQuote(requestDto, userREGDetails);
		assertEquals(400, response.getBody().getResultCode());
		assertEquals("일일 송금 최대치를 초과합니다.", response.getBody().getResultMsg());
	}

	@Test
	@Order(22)
	void testRequest_BUSI_UnderLimit_200() {
		User userBUSI = userRepository.findByUserId("testBUSI@naver.com").orElse(null);
		UserDetailsImpl userBUSIDetails = new UserDetailsImpl(userBUSI, "testBUSI@naver.com");

		// 당일 한도 금액 내 JPY 테스트
		RequestDto requestDto = new RequestDto();
		requestDto.setQuoteId(BUSIUserQuoteNums.get(0));
		ResponseEntity<BaseResponse> response = quoteService.requestQuote(requestDto, userBUSIDetails);
		assertEquals(200, response.getBody().getResultCode());
		assertEquals("OK", response.getBody().getResultMsg());

		// 당일 한도 금액 내 USD 테스트
		RequestDto requestDto2 = new RequestDto();
		requestDto2.setQuoteId(BUSIUserQuoteNums.get(1));
		ResponseEntity<BaseResponse> response2 = quoteService.requestQuote(requestDto2, userBUSIDetails);
		assertEquals(200, response2.getBody().getResultCode());
		assertEquals("OK", response2.getBody().getResultMsg());
	}

	@Test
	@Order(23)
	void testRequest_BUSI_OverLimit_400() {
		User userBUSI = userRepository.findByUserId("testBUSI@naver.com").orElse(null);
		UserDetailsImpl userBUSIDetails = new UserDetailsImpl(userBUSI, "testBUSI@naver.com");

		RequestDto requestDto = new RequestDto();
		requestDto.setQuoteId(BUSIUserQuoteNums.get(2));
		ResponseEntity<BaseResponse> response = quoteService.requestQuote(requestDto, userBUSIDetails);
		assertEquals(400, response.getBody().getResultCode());
		assertEquals("일일 송금 최대치를 초과합니다.", response.getBody().getResultMsg());


	}

	@Test
	@Order(24)
	void testList_REG_200() {
		User user = userRepository.findByUserId("testREG@naver.com").orElse(null);
		UserDetailsImpl userDetails = new UserDetailsImpl(user, "testREG@naver.com");

		ResponseEntity<ListResponse> response = quoteService.getHistories(userDetails);

		LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN); // 오늘 날짜의 시작 시간
		LocalDateTime endOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MAX); // 오늘 날짜의 종료 시간
		List<Request> requests = requestRepository.findByUserAndRequestTimeBetween(user, startOfToday, endOfToday);

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

	@Test
	@Order(25)
	void testList_BUSI_200() {
		User userBUSI = userRepository.findByUserId("testBUSI@naver.com").orElse(null);
		UserDetailsImpl userBUSIDetails = new UserDetailsImpl(userBUSI, "testBUSI@naver.com");

		ResponseEntity<ListResponse> response = quoteService.getHistories(userBUSIDetails);

		LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN); // 오늘 날짜의 시작 시간
		LocalDateTime endOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MAX); // 오늘 날짜의 종료 시간
		List<Request> requests = requestRepository.findByUserAndRequestTimeBetween(userBUSI, startOfToday, endOfToday);

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
