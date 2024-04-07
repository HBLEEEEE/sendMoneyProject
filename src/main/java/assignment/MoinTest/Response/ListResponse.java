package assignment.MoinTest.Response;


import assignment.MoinTest.common.exception.ErrorCode;
import assignment.MoinTest.common.exception.SuccessCode;
import assignment.MoinTest.transfer.dto.HistoryResponseDto;
import assignment.MoinTest.user.entity.User;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Builder
public class ListResponse {

    private final int resultCode;
    private final String resultMsg;
    private final String userId;
    private final String name;
    private final int todayTransferCount;
    private final double todayTransferUsdAmount;
    private final List<HistoryResponseDto> history;
    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static ResponseEntity<ListResponse> toResponseEntity(SuccessCode successCode, User user, int todayTransferCount, double todayTransferUsdAmount, List<HistoryResponseDto> histories) {
        return ResponseEntity
                .status(successCode.getHttpStatus())
                .header("Content-Type", "application/json;charset=UTF-8" )
                .body(ListResponse.builder()
                        .resultCode(successCode.getHttpStatus().value())
                        .resultMsg(successCode.getDetail())
                        .userId(user.getUserId())
                        .name(user.getName())
                        .todayTransferCount(todayTransferCount)
                        .todayTransferUsdAmount(todayTransferUsdAmount)
                        .history(histories)
                        .build());
    }

    public static ResponseEntity<ListResponse> toResponseEntity(ErrorCode errorCode){
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .header("Content-Type", "application/json;charset=UTF-8" )
                .body(ListResponse.builder()
                        .resultCode(errorCode.getHttpStatus().value())
                        .resultMsg(errorCode.getMsg())
                        .build());
    }




}
