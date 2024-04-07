package assignment.MoinTest.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDto {

    @NotBlank(message = "userId는 공백이 될 수 없습니다.")
    @NotNull(message = "userId는 null이 될 수 없습니다.")
    private String userId;

    @NotBlank(message = "password는 공백이 될 수 없습니다.")
    @NotNull(message = "password는 null이 될 수 없습니다.")
    @Size(min = 2, max = 12, message = "password 최소 2자리, 최대 12자리입니다.")
    private String password;
}
