package account;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "user")
public class User {

    //@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;
    @Column
    @NotBlank
    String name;

    @Column
    @NotBlank
    String lastname;

    @Column
    @NotBlank
    @Email
    @Size(min = 8)
    @Pattern(regexp = ".+@acme.com")
    String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column
    @NotBlank
    //@Size(min = 12,message = "The password length must be at leat 12 chars!")
    String password;


}
