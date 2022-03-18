package account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public
class ChangePasswordDto {

    @NotBlank
   // @Size(min = 12,message = "The password length must be at leat 12 chars!")
    @JsonProperty("new_password")
    private String password;
}
