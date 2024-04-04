package assignment.MoinTest.user.entity;

import lombok.Getter;

@Getter
public enum UserIdTypeEnum {

    REG_NO(IdType.REG_NO),  //개인 회원
    BUSINESS_NO(IdType.BUSINESS_NO);    //법인 회원

    private final String idType;

    UserIdTypeEnum(String idType){
        this.idType = idType;
    }

    public static class IdType{
        public static final String REG_NO = "REG_NO";
        public static final String BUSINESS_NO = "BUSINESS_NO";
    }
}
