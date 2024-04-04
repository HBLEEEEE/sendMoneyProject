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
@RequestMapping("/transfer")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;

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