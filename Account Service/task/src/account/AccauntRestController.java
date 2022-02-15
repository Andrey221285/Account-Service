package account;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

@RestController
public class AccauntRestController {
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder encoder;

    private HashSet<String> hackedPassword = new HashSet<>(Arrays.asList("PasswordForJanuary", "PasswordForFebruary", "PasswordForMarch", "PasswordForApril",
            "PasswordForMay", "PasswordForJune", "PasswordForJuly", "PasswordForAugust",
            "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember", "PasswordForDecember"));

    @ExceptionHandler({ ConstraintViolationException.class })
    @PostMapping ("/api/auth/signup")
    public ResponseEntity<?> signup (@Valid @RequestBody User user){
        if (user.getPassword().length() < 12){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password length must be 12 chars minimum!");
        }


         if (hackedPassword.contains(user.getPassword())){
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password is in the hacker's database!");
         }


        User userOld = userRepository.findByEmailIgnoreCase(user.getEmail());
        if (userOld == null){
            user.setPassword(encoder.encode(user.getPassword()));
            userRepository.save(user);
            return new ResponseEntity<>(user,HttpStatus.OK);
        }

        throw new UserExistException();
    }

    @GetMapping("api/empl/payment")
    public ResponseEntity<?> getPayment (@AuthenticationPrincipal UserDetails details){
        if (details != null){
            User user = userRepository.findByEmailIgnoreCase(details.getUsername());
            return new ResponseEntity<>(user,HttpStatus.OK);
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }

    @PostMapping("api/auth/changepass")
    public ResponseEntity<?> changePassword (@AuthenticationPrincipal UserDetails details,@Valid @RequestBody ChangePassword changePassword ){
        if (details != null){
            if (changePassword.getPassword().length() < 12){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password length must be 12 chars minimum!");
            }
            System.out.println("password = "+ changePassword.getPassword());
            if (hackedPassword.contains(changePassword.getPassword())){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password is in the hacker's database!");
            }
            User user = userRepository.findByEmailIgnoreCase(details.getUsername());
            if( encoder.matches(changePassword.getPassword(), user.getPassword())){
                throw new  ResponseStatusException(HttpStatus.BAD_REQUEST, "The passwords must be different!");
            }
            user.setPassword(encoder.encode(changePassword.getPassword()));
            userRepository.save(user);
            HashMap<String, String> map = new HashMap<>();
            map.put("email",user.getEmail().toLowerCase());
            map.put("status", "The password has been updated successfully");

            return new ResponseEntity<>(map,HttpStatus.OK);
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }




}
