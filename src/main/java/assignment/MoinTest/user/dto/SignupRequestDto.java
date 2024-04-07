package assignment.MoinTest.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequestDto {

    @NotBlank(message = "userId는 공백이 될 수 없습니다.")
    @NotNull(message = "userId는 null이 될 수 없습니다.")
    @Email(message = "userID는 email 형식이어야 합니다.")
    private String userId;

    @NotBlank(message = "password는 공백이 될 수 없습니다.")
    @NotNull(message = "password는 null이 될 수 없습니다.")
    @Size(min = 4, max = 12, message = "password 최소 4자리, 최대 12자리입니다.")
    private String password;

    @NotBlank(message = "name은 공백이 될 수 없습니다.")
    @NotNull(message = "name은 null이 될 수 없습니다.")
    @Size(min = 2, max = 10, message = "name은 최소 2자리, 최대 10자리입니다.")
    private String name;

    @NotBlank(message = "idType는 공백이 될 수 없습니다.")
    @NotNull(message = "idType는 null이 될 수 없습니다.")
    private String idType;

    @NotBlank(message = "idValue는 공백이 될 수 없습니다.")
    @NotNull(message = "idValue는 null이 될 수 없습니다.")
    private String idValue;
}
