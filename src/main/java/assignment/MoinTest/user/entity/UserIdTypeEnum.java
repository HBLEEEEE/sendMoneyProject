package assignment.MoinTest.user.entity;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

//TODO: 1. Enum은 딱히 네이밍에 안넣어도 될거 같은데 팀에서 넣으라면 넣으샘 나는 불용어라 생각하는데 enum이라고 한번더 명시하고 싶을수도 있을듯..
//TODO: 2. ToString은 웬만하면 넣는게 좋음 로깅할때 저놈 없으면 레퍼런스만 찍힘
@Getter @ToString
@RequiredArgsConstructor
public enum UserIdTypeEnum {

    REG_NO("REG_NO"),  //개인 회원
    BUSINESS_NO("BUSINESS_NO");    //법인 회원

    // TODO: 2 final 붙인건 매우 굳인듯, UserIdTypeEnum.REG_NO.getIdType <-> UserIdType.REG_NO.getId 이런 네이밍 난 선호 갠취
    private final String idType;

    public static UserIdTypeEnum findById(String idType) {
        return Arrays.stream(values())
            .filter(it -> it.idType.equals(idType))
            .findAny()
            .orElse(null);
        // .orElseThrow(() -> new IllegalArgumentException("idType - %s".formatted(idType))) 예외를 발생시켜도 즣음
        //.orElse(UserIdTypeEnum.NONE); 이렇겐 어쩔수 없을때 빼곤 잘 안함
    }


    // TODO: 1.
    public static class IdType{
        public static final String REG_NO = "REG_NO";
        public static final String BUSINESS_NO = "BUSINESS_NO";
    }
}
