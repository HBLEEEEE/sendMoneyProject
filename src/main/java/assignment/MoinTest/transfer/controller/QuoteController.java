package assignment.MoinTest.transfer.controller;

import assignment.MoinTest.Response.BaseResponse;
import assignment.MoinTest.Response.ListResponse;
import assignment.MoinTest.Response.QuoteResponse;
import assignment.MoinTest.security.UserDetailsImpl;
import assignment.MoinTest.transfer.dto.QuoteRequestDto;
import assignment.MoinTest.transfer.dto.RequestDto;
import assignment.MoinTest.transfer.service.QuoteService;
import assignment.MoinTest.user.dto.LoginRequestDto;
import assignment.MoinTest.user.dto.SignupRequestDto;
import assignment.MoinTest.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
//TODO: 1. 아 근데 보통 api가 배포되고 앱이 배포되면 앱은 반 영구적이기때문에 응답 필드 추가 등이 아닌 호환성이 깨지는 필드 제거 응답갑 변경 등을 하기 위해선 /v1/transfer/quote 이런식으로 버저닝도 많이함
// 나 안다 이런거 보여주려면 하고 일단 복잡하면 하지마샘
@RequestMapping("/transfer")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;

    //TODO: 2. path 앞에 / 이거 빠진듯
    @PostMapping("quote")
    @ResponseBody
    public ResponseEntity<QuoteResponse> quote(@RequestBody QuoteRequestDto quoteRequestDto,
                                               @AuthenticationPrincipal UserDetailsImpl userDetails){
        return quoteService.orderQuote(quoteRequestDto, userDetails);
    }

    @PostMapping("request")
    @ResponseBody
    public ResponseEntity<BaseResponse> request(@RequestBody RequestDto requestDto,
                                                @AuthenticationPrincipal UserDetailsImpl userDetails){
        return quoteService.requestQuote(requestDto, userDetails);
    }

    @GetMapping("list")
    public ResponseEntity<ListResponse> list(@AuthenticationPrincipal UserDetailsImpl userDetails){

        return quoteService.getHistories(userDetails);
    }
}