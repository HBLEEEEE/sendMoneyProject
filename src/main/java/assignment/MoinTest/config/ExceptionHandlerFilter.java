package assignment.MoinTest.config;

import assignment.MoinTest.common.exception.CustomException;
import assignment.MoinTest.common.exception.ErrorCode;
import assignment.MoinTest.jwt.StateDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class ExceptionHandlerFilter extends OncePerRequestFilter {

    private void customExceptionHandler(HttpServletResponse response, ErrorCode errorCode) {
        log.info(errorCode.getMsg());
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try {
            String json = new ObjectMapper().writeValueAsString(new StateDto(errorCode.getMsg(), errorCode.getHttpStatus().value()));
            response.getWriter().write(json);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try{
            filterChain.doFilter(request,response);
        } catch (CustomException e) {
            customExceptionHandler(response, e.getErrorCode());
        }
    }
}