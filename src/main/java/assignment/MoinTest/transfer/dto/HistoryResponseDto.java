package assignment.MoinTest.transfer.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
