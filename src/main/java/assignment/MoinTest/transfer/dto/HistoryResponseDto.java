package assignment.MoinTest.transfer.dto;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class HistoryResponseDto {

    private double sourceAmount;
    private double fee;
    private double usdExchangeRate;
    private double usdAmount;
    private String targetCurrency;
    private double exchangeRate;
    private double targetAmount;
    private String requestedDate;

}
