package assignment.MoinTest.transfer.service;

import assignment.MoinTest.Response.BaseResponse;
import assignment.MoinTest.Response.ListResponse;
import assignment.MoinTest.Response.QuoteResponse;
import assignment.MoinTest.security.UserDetailsImpl;
import assignment.MoinTest.transfer.dto.QuoteRequestDto;
import assignment.MoinTest.transfer.dto.RequestDto;
import assignment.MoinTest.transfer.entity.Quote;
import assignment.MoinTest.transfer.entity.Request;
import assignment.MoinTest.transfer.repository.QuoteRepository;
import assignment.MoinTest.transfer.repository.RequestRepository;
import assignment.MoinTest.user.dto.SignupRequestDto;
import assignment.MoinTest.user.entity.User;
import assignment.MoinTest.user.repository.UserRepository;
import assignment.MoinTest.user.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class QuoteServiceTest {

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private RequestRepository requestRepository;

    @BeforeEach
    public void setUp(){
        SignupRequestDto signupRequestDto = new SignupRequestDto();
        signupRequestDto.setUserId("testREG@naver.com");
        signupRequestDto.setPassword("1234");
        signupRequestDto.setName("REGUser");
        signupRequestDto.setIdType("REG_NO");
        signupRequestDto.setIdValue("020315-1234567");
        userService.signup(signupRequestDto);

        SignupRequestDto signupBUSI = new SignupRequestDto();
        signupBUSI.setUserId("testBUSI@naver.com");
        signupBUSI.setPassword("1234");
        signupBUSI.setName("BUSIUser");
        signupBUSI.setIdType("BUSINESS_NO");
        signupBUSI.setIdValue("123-01-01478");
        userService.signup(signupBUSI);
    }

    @AfterEach
    public void clear(){
        quoteRepository.deleteAll();
        requestRepository.deleteAll();
        userRepository.deleteAll();
    }

    private class RequestWorker implements Runnable{

        private RequestDto requestDto;

        private UserDetailsImpl userDetails;

        public RequestWorker(RequestDto requestDto, UserDetailsImpl userDetails) {
            this.requestDto = requestDto;
            this.userDetails = userDetails;
        }

        @Override
        public void run() {
            quoteService.request(requestDto, userDetails);
        }
    }

    @Test
    void testRequest_concurrencyControll_200() throws InterruptedException {
        //given
        User userREG = userRepository.findByUserId("testREG@naver.com").orElse(null);
        UserDetailsImpl userDetails = new UserDetailsImpl(userREG, "testREG@naver.com");

        QuoteRequestDto quoteRequestDto = new QuoteRequestDto();
        quoteRequestDto.setAmount(100000);
        quoteRequestDto.setTargetCurrency("USD");
        ResponseEntity<QuoteResponse> response = quoteService.quote(quoteRequestDto, userDetails);
        Quote quote = quoteRepository.findById(response.getBody().getQuote().getQuoteId()).orElse(null);

        RequestDto requestDto = new RequestDto();
        requestDto.setQuoteId(quote.getQuoteId());

        //when
        CountDownLatch countDownLatch = new CountDownLatch(20);
        List<RequestWorker> workers = Stream.
                generate(() -> new RequestWorker(requestDto, userDetails))
                .limit(20)
                .collect(Collectors.toList());

        workers.forEach(worker -> new Thread(worker).start());

        countDownLatch.await(1, TimeUnit.SECONDS);

        //then
        List<Request> requests = requestRepository.findAllByQuote(quote);
        assertEquals(1, requests.size());
    }

    @Test
    void testQuote_REG_JPY_200() {
        //given
        User userREG = userRepository.findByUserId("testREG@naver.com").orElse(null);
        UserDetailsImpl userREGDetails = new UserDetailsImpl(userREG, "testREG@naver.com");

        //when
        QuoteRequestDto quoteRequestDto = new QuoteRequestDto();
        quoteRequestDto.setAmount(100000);
        quoteRequestDto.setTargetCurrency("JPY");

        //then
        ResponseEntity<QuoteResponse> response = quoteService.quote(quoteRequestDto, userREGDetails);
        assertEquals(200, response.getBody().getResultCode());
        assertEquals("OK", response.getBody().getResultMsg());
    }

    @Test
    void testQuote_REG_USD_200() {
        //given
        User userREG = userRepository.findByUserId("testREG@naver.com").orElse(null);
        UserDetailsImpl userREGDetails = new UserDetailsImpl(userREG, "testREG@naver.com");

        //when
        QuoteRequestDto quoteRequestDto = new QuoteRequestDto();
        quoteRequestDto.setAmount(100000);
        quoteRequestDto.setTargetCurrency("USD");

        //then
        ResponseEntity<QuoteResponse> response = quoteService.quote(quoteRequestDto, userREGDetails);
        assertEquals(200, response.getBody().getResultCode());
        assertEquals("OK", response.getBody().getResultMsg());
    }

    @Test
    void testQuote_REG_wrongTargetCurrency_400() {
        //given
        User userREG = userRepository.findByUserId("testREG@naver.com").orElse(null);
        UserDetailsImpl userREGDetails = new UserDetailsImpl(userREG, "testREG@naver.com");

        //when
        QuoteRequestDto quoteRequestDto = new QuoteRequestDto();
        quoteRequestDto.setAmount(100000);
        quoteRequestDto.setTargetCurrency("EUR");

        //then
        ResponseEntity<QuoteResponse> response = quoteService.quote(quoteRequestDto, userREGDetails);
        assertEquals(400, response.getBody().getResultCode());
        assertEquals("JPY 혹은 USD가 아닙니다.", response.getBody().getResultMsg());
    }

    @Test
    void testQuote_REG_negative_400() {
        //given
        User userREG = userRepository.findByUserId("testREG@naver.com").orElse(null);
        UserDetailsImpl userREGDetails = new UserDetailsImpl(userREG, "testREG@naver.com");

        //when
        QuoteRequestDto quoteRequestDto = new QuoteRequestDto();
        quoteRequestDto.setAmount(-10000);
        quoteRequestDto.setTargetCurrency("JPY");

        //then
        ResponseEntity<QuoteResponse> response = quoteService.quote(quoteRequestDto, userREGDetails);
        assertEquals(400, response.getBody().getResultCode());
        assertEquals("음수 발생 요청입니다.", response.getBody().getResultMsg());
    }

    @Test
    void testQuote_BUSI_JPY_200() {
        //given
        User userBUSI = userRepository.findByUserId("testBUSI@naver.com").orElse(null);
        UserDetailsImpl userBUSIDetails = new UserDetailsImpl(userBUSI, "testBUSI@naver.com");

        //when
        QuoteRequestDto quoteRequestDto = new QuoteRequestDto();
        quoteRequestDto.setAmount(300000);
        quoteRequestDto.setTargetCurrency("JPY");

        //then
        ResponseEntity<QuoteResponse> response = quoteService.quote(quoteRequestDto, userBUSIDetails);
        assertEquals(200, response.getBody().getResultCode());
        assertEquals("OK", response.getBody().getResultMsg());
    }

    @Test
    void testQuote_BUSI_USD_200() {
        //given
        User userBUSI = userRepository.findByUserId("testBUSI@naver.com").orElse(null);
        UserDetailsImpl userBUSIDetails = new UserDetailsImpl(userBUSI, "testBUSI@naver.com");

        //when
        QuoteRequestDto quoteRequestDto = new QuoteRequestDto();
        quoteRequestDto.setAmount(300000);
        quoteRequestDto.setTargetCurrency("USD");

        //then
        ResponseEntity<QuoteResponse> response = quoteService.quote(quoteRequestDto, userBUSIDetails);
        assertEquals(200, response.getBody().getResultCode());
        assertEquals("OK", response.getBody().getResultMsg());
    }

    @Test
    void testQuote_BUSI_wrongTargetCurrency_400() {
        //given
        User userBUSI = userRepository.findByUserId("testBUSI@naver.com").orElse(null);
        UserDetailsImpl userBUSIDetails = new UserDetailsImpl(userBUSI, "testBUSI@naver.com");

        //when
        QuoteRequestDto quoteRequestDto = new QuoteRequestDto();
        quoteRequestDto.setAmount(100000);
        quoteRequestDto.setTargetCurrency("EUR");

        //then
        ResponseEntity<QuoteResponse> response = quoteService.quote(quoteRequestDto, userBUSIDetails);
        assertEquals(400, response.getBody().getResultCode());
        assertEquals("JPY 혹은 USD가 아닙니다.", response.getBody().getResultMsg());
    }

    @Test
    void testQuote_BUSI_negative_400() {
        //given
        User userBUSI = userRepository.findByUserId("testBUSI@naver.com").orElse(null);
        UserDetailsImpl userBUSIDetails = new UserDetailsImpl(userBUSI, "testBUSI@naver.com");

        //when
        QuoteRequestDto quoteRequestDto = new QuoteRequestDto();
        quoteRequestDto.setAmount(-10000);
        quoteRequestDto.setTargetCurrency("JPY");

        //then
        ResponseEntity<QuoteResponse> response = quoteService.quote(quoteRequestDto, userBUSIDetails);
        assertEquals(400, response.getBody().getResultCode());
        assertEquals("음수 발생 요청입니다.", response.getBody().getResultMsg());
    }

    @Test
    void testRequest_REG_underLimit_200() {
        //given
        User userREG = userRepository.findByUserId("testREG@naver.com").orElse(null);
        UserDetailsImpl userREGDetails = new UserDetailsImpl(userREG, "testREG@naver.com");

        //when
        QuoteRequestDto quoteRequestDto = new QuoteRequestDto();
        quoteRequestDto.setAmount(300000);
        quoteRequestDto.setTargetCurrency("JPY");
        ResponseEntity<QuoteResponse> quoteResponse = quoteService.quote(quoteRequestDto, userREGDetails);

        QuoteRequestDto quoteRequestDto2 = new QuoteRequestDto();
        quoteRequestDto2.setAmount(300000);
        quoteRequestDto2.setTargetCurrency("USD");
        ResponseEntity<QuoteResponse> quoteResponse2 = quoteService.quote(quoteRequestDto2, userREGDetails);

        //then
        RequestDto requestDto = new RequestDto();
        requestDto.setQuoteId(quoteResponse.getBody().getQuote().getQuoteId());
        ResponseEntity<BaseResponse> response = quoteService.request(requestDto, userREGDetails);
        assertEquals(200, response.getBody().getResultCode());
        assertEquals("OK", response.getBody().getResultMsg());

        RequestDto requestDto2 = new RequestDto();
        requestDto2.setQuoteId(quoteResponse2.getBody().getQuote().getQuoteId());
        ResponseEntity<BaseResponse> response2 = quoteService.request(requestDto2, userREGDetails);
        assertEquals(200, response2.getBody().getResultCode());
        assertEquals("OK", response2.getBody().getResultMsg());
    }

    @Test
    void testRequest_REG_overLimit_200() {
        //given
        User userREG = userRepository.findByUserId("testREG@naver.com").orElse(null);
        UserDetailsImpl userREGDetails = new UserDetailsImpl(userREG, "testREG@naver.com");

        //when
        QuoteRequestDto quoteRequestDto = new QuoteRequestDto();
        quoteRequestDto.setAmount(300000);
        quoteRequestDto.setTargetCurrency("JPY");
        ResponseEntity<QuoteResponse> quoteResponse = quoteService.quote(quoteRequestDto, userREGDetails);

        QuoteRequestDto quoteRequestDto2 = new QuoteRequestDto();
        quoteRequestDto2.setAmount(300000);
        quoteRequestDto2.setTargetCurrency("USD");
        ResponseEntity<QuoteResponse> quoteResponse2 = quoteService.quote(quoteRequestDto2, userREGDetails);

        QuoteRequestDto quoteRequestDto3 = new QuoteRequestDto();
        quoteRequestDto3.setAmount(800000);
        quoteRequestDto3.setTargetCurrency("USD");
        ResponseEntity<QuoteResponse> quoteResponse3 = quoteService.quote(quoteRequestDto3, userREGDetails);

        //then
        RequestDto requestDto = new RequestDto();
        requestDto.setQuoteId(quoteResponse.getBody().getQuote().getQuoteId());
        quoteService.request(requestDto, userREGDetails);
        RequestDto requestDto2 = new RequestDto();
        requestDto2.setQuoteId(quoteResponse2.getBody().getQuote().getQuoteId());
        quoteService.request(requestDto2, userREGDetails);

        RequestDto requestDto3 = new RequestDto();
        requestDto3.setQuoteId(quoteResponse3.getBody().getQuote().getQuoteId());
        ResponseEntity<BaseResponse> response3 = quoteService.request(requestDto3, userREGDetails);
        assertEquals(400, response3.getBody().getResultCode());
        assertEquals("일일 송금 최대치를 초과합니다.", response3.getBody().getResultMsg());
    }

    @Test
    void testRequest_BUSI_underLimit_200() {
        //given
        User userBUSI = userRepository.findByUserId("testBUSI@naver.com").orElse(null);
        UserDetailsImpl userBUSIDetails = new UserDetailsImpl(userBUSI, "testBUSI@naver.com");

        //when
        QuoteRequestDto quoteRequestDto = new QuoteRequestDto();
        quoteRequestDto.setAmount(300000);
        quoteRequestDto.setTargetCurrency("JPY");
        ResponseEntity<QuoteResponse> quoteResponse = quoteService.quote(quoteRequestDto, userBUSIDetails);

        QuoteRequestDto quoteRequestDto2 = new QuoteRequestDto();
        quoteRequestDto2.setAmount(1300000);
        quoteRequestDto2.setTargetCurrency("USD");
        ResponseEntity<QuoteResponse> quoteResponse2 = quoteService.quote(quoteRequestDto2, userBUSIDetails);

        //then
        RequestDto requestDto = new RequestDto();
        requestDto.setQuoteId(quoteResponse.getBody().getQuote().getQuoteId());
        ResponseEntity<BaseResponse> response = quoteService.request(requestDto, userBUSIDetails);
        assertEquals(200, response.getBody().getResultCode());
        assertEquals("OK", response.getBody().getResultMsg());

        RequestDto requestDto2 = new RequestDto();
        requestDto2.setQuoteId(quoteResponse2.getBody().getQuote().getQuoteId());
        ResponseEntity<BaseResponse> response2 = quoteService.request(requestDto2, userBUSIDetails);
        assertEquals(200, response2.getBody().getResultCode());
        assertEquals("OK", response2.getBody().getResultMsg());
    }

    @Test
    void testRequest_BUSI_overLimit_400() {
        //given
        User userBUSI = userRepository.findByUserId("testBUSI@naver.com").orElse(null);
        UserDetailsImpl userBUSIDetails = new UserDetailsImpl(userBUSI, "testBUSI@naver.com");

        //when
        QuoteRequestDto quoteRequestDto = new QuoteRequestDto();
        quoteRequestDto.setAmount(300000);
        quoteRequestDto.setTargetCurrency("JPY");
        ResponseEntity<QuoteResponse> quoteResponse = quoteService.quote(quoteRequestDto, userBUSIDetails);

        QuoteRequestDto quoteRequestDto2 = new QuoteRequestDto();
        quoteRequestDto2.setAmount(1300000);
        quoteRequestDto2.setTargetCurrency("USD");
        ResponseEntity<QuoteResponse> quoteResponse2 = quoteService.quote(quoteRequestDto2, userBUSIDetails);

        QuoteRequestDto quoteRequestDto3 = new QuoteRequestDto();
        quoteRequestDto3.setAmount(6000000);
        quoteRequestDto3.setTargetCurrency("USD");
        ResponseEntity<QuoteResponse> quoteResponse3 = quoteService.quote(quoteRequestDto3, userBUSIDetails);

        //then
        RequestDto requestDto = new RequestDto();
        requestDto.setQuoteId(quoteResponse.getBody().getQuote().getQuoteId());
        quoteService.request(requestDto, userBUSIDetails);
        RequestDto requestDto2 = new RequestDto();
        requestDto2.setQuoteId(quoteResponse2.getBody().getQuote().getQuoteId());
        quoteService.request(requestDto2, userBUSIDetails);

        RequestDto requestDto3 = new RequestDto();
        requestDto3.setQuoteId(quoteResponse3.getBody().getQuote().getQuoteId());
        ResponseEntity<BaseResponse> response3 = quoteService.request(requestDto3, userBUSIDetails);
        assertEquals(400, response3.getBody().getResultCode());
        assertEquals("일일 송금 최대치를 초과합니다.", response3.getBody().getResultMsg());
    }

    @Test
    void testList_REG_200() {
        //given
        User user = userRepository.findByUserId("testREG@naver.com").orElse(null);
        UserDetailsImpl userDetails = new UserDetailsImpl(user, "testREG@naver.com");

        //when
        QuoteRequestDto quoteRequestDto = new QuoteRequestDto();
        quoteRequestDto.setAmount(300000);
        quoteRequestDto.setTargetCurrency("JPY");
        ResponseEntity<QuoteResponse> quoteResponse = quoteService.quote(quoteRequestDto, userDetails);

        QuoteRequestDto quoteRequestDto2 = new QuoteRequestDto();
        quoteRequestDto2.setAmount(300000);
        quoteRequestDto2.setTargetCurrency("USD");
        ResponseEntity<QuoteResponse> quoteResponse2 = quoteService.quote(quoteRequestDto2, userDetails);

        RequestDto requestDto = new RequestDto();
        requestDto.setQuoteId(quoteResponse.getBody().getQuote().getQuoteId());
        quoteService.request(requestDto, userDetails);

        RequestDto requestDto2 = new RequestDto();
        requestDto2.setQuoteId(quoteResponse2.getBody().getQuote().getQuoteId());
        quoteService.request(requestDto2, userDetails);

        //then
        ResponseEntity<ListResponse> response = quoteService.list(userDetails);

        LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN); // 오늘 날짜의 시작 시간
        LocalDateTime endOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MAX); // 오늘 날짜의 종료 시간
        List<Request> requests = requestRepository.findByUserAndRequestTimeBetween(user, startOfToday, endOfToday);

        int todayCount = requests.size();
        double todayTotalUsd = requests.stream()
                .mapToDouble(request -> request.getQuote().getUsdAmount())
                .sum();

        assertEquals(200, response.getBody().getResultCode());
        assertEquals("OK", response.getBody().getResultMsg());
        assertEquals(todayCount, response.getBody().getTodayTransferCount());
        assertEquals(todayTotalUsd, response.getBody().getTodayTransferUsdAmount());
    }

    @Test
    void testList_BUSI_200() {
        //given
        User userBUSI = userRepository.findByUserId("testBUSI@naver.com").orElse(null);
        UserDetailsImpl userBUSIDetails = new UserDetailsImpl(userBUSI, "testBUSI@naver.com");

        //when
        QuoteRequestDto quoteRequestDto = new QuoteRequestDto();
        quoteRequestDto.setAmount(300000);
        quoteRequestDto.setTargetCurrency("JPY");
        ResponseEntity<QuoteResponse> quoteResponse = quoteService.quote(quoteRequestDto, userBUSIDetails);

        QuoteRequestDto quoteRequestDto2 = new QuoteRequestDto();
        quoteRequestDto2.setAmount(1300000);
        quoteRequestDto2.setTargetCurrency("USD");
        ResponseEntity<QuoteResponse> quoteResponse2 = quoteService.quote(quoteRequestDto2, userBUSIDetails);

        RequestDto requestDto = new RequestDto();
        requestDto.setQuoteId(quoteResponse.getBody().getQuote().getQuoteId());
        quoteService.request(requestDto, userBUSIDetails);
        RequestDto requestDto2 = new RequestDto();
        requestDto2.setQuoteId(quoteResponse2.getBody().getQuote().getQuoteId());
        quoteService.request(requestDto2, userBUSIDetails);

        //then
        ResponseEntity<ListResponse> response = quoteService.list(userBUSIDetails);

        LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN); // 오늘 날짜의 시작 시간
        LocalDateTime endOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MAX); // 오늘 날짜의 종료 시간
        List<Request> requests = requestRepository.findByUserAndRequestTimeBetween(userBUSI, startOfToday, endOfToday);

        int todayCount = requests.size();
        double todayTotalUsd = requests.stream()
                .mapToDouble(request -> request.getQuote().getUsdAmount())
                .sum();

        assertEquals(200, response.getBody().getResultCode());
        assertEquals("OK", response.getBody().getResultMsg());
        assertEquals(todayCount, response.getBody().getTodayTransferCount());
        assertEquals(todayTotalUsd, response.getBody().getTodayTransferUsdAmount());
    }
}