package assignment.MoinTest.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Arrays;

@Getter
@ToString
@RequiredArgsConstructor
public enum UserIdTypeEnum {

    REG_NO("REG_NO"),  //개인 회원
    BUSINESS_NO("BUSINESS_NO");    //법인 회원

    private final String idType;

    public static UserIdTypeEnum findByIdType(String idType){
        return Arrays.stream(values())
                .filter(it -> it.idType.equals(idType))
                .findAny()
                .orElse(null);
    }


}
