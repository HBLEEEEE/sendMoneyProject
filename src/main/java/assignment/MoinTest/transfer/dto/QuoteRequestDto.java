package assignment.MoinTest.transfer.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuoteRequestDto {
    private double amount;
    private String targetCurrency;
}
