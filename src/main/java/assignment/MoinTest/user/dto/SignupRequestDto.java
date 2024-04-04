package assignment.MoinTest.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequestDto {
    private String userId;
    private String password;
    private String name;
    private String idType;
    private String idValue;
}
