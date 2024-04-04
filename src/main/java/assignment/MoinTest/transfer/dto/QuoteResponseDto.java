package assignment.MoinTest.transfer.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class QuoteResponseDto {
    private Long quoteId;
    private double exchangeRate;
    private LocalDateTime expireTime;
    private double targetAmount;
}
