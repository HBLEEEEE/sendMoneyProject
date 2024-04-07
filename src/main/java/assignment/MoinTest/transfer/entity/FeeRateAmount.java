package assignment.MoinTest.transfer.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FeeRateAmount {

    private double fee;
    private double usdRate;
    private double targetRate;
    private double usdSendAmount;
    private double targetAmount;
}
