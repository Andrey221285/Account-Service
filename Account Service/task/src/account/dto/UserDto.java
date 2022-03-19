package account.dto;

import account.entity.Group;
import account.entity.User;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Column;
import javax.persistence.ManyToMany;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Set;
import java.util.TreeSet;

public class UserDto {
    private Long id;
    private String name;
    private String lastname;
    private String email;
    private TreeSet<String> roles;

    public UserDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.lastname = user.getLastname();
        this.email = user.getEmail().toLowerCase();
        this.roles = new TreeSet<>();
        for (var roles : user.getRoles()){
            this.roles.add(roles.getName());
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLastname() {
        return lastname;
    }

    public String getEmail() {
        return email;
    }

    public TreeSet<String> getRoles() {
        return roles;
    }
}
