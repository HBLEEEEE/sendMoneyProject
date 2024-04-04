package assignment.MoinTest.jwt;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StateDto {

    private String msg;
    private int status;

    @Builder
    public StateDto(String msg, int status) {
        this.msg = msg;
        this.status = status;
    }
}