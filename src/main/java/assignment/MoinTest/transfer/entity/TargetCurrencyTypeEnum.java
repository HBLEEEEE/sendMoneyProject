package assignment.MoinTest.transfer.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Arrays;

@Getter
@ToString
@RequiredArgsConstructor
public enum TargetCurrencyTypeEnum {

    JPY("JPY"),
    USD("USD");

    private final String currenyType;

    public static TargetCurrencyTypeEnum findByCurrencyType(String curreny){
        return Arrays.stream(values())
                .filter(it -> it.currenyType.equals(curreny))
                .findAny()
                .orElse(null);
    }
}
