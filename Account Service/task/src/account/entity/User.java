package account.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.type.descriptor.sql.LongVarcharTypeDescriptor;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Set;
import java.util.TreeSet;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "user")
public class User {

    //@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    @NotBlank
    private String name;

    @Column
    @NotBlank
    private String lastname;

    @Column
    @NotBlank
    @Email
    @Size(min = 8)
    @Pattern(regexp = ".+@acme.com")
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column
    @NotBlank
    //@Size(min = 12,message = "The password length must be at leat 12 chars!")
    private String password;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column
    private long login_failure = 0;

    @ManyToMany(fetch = FetchType.EAGER)
    //@JoinColumn(name="group_id", nullable=false)
    private Set<Group> roles;

    public void loginFailureInc(){
        login_failure++;
    }

    public void unlockUser(){
        login_failure = 0;
    }
    public void lockUsuer(){
        login_failure = 6;
    }
}
