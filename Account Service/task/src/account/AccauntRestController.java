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

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@RestController
//@ControllerAdvice
public class AccauntRestController {
    @Autowired UserRepository userRepository;
    @Autowired PaymentRepository paymentRepository;
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

    @PostMapping("api/acct/payments")
    public ResponseEntity<?> addPayments (@RequestBody List<@Valid Payment> payments ){
        saveAllPayments(payments);
        HashMap<String, String> m = new HashMap<>();
        m.put("status","Added successfully!");
        return new ResponseEntity<>(m,HttpStatus.OK);
    }

    @Transactional
    public void saveAllPayments (List<@Valid Payment> payments) {
//        paymentRepository.saveAll(payments);
        for(var p : payments){
            User u = userRepository.findByEmailIgnoreCase(p.getEmployee());
            if (u!= null){
                p.setUser(u);
                paymentRepository.save(p);
            } else {
                throw new RuntimeException();
//                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ненту юзера");
            }
        }
    }

    @PutMapping("api/acct/payments")
    public ResponseEntity<?> updatePayments (@Valid @RequestBody ChangePassword changePassword ){

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity handle(Exception constraintViolationException) {
////        Set<ConstraintViolation<?>> violations = constraintViolationException.getConstraintViolations();
//        String errorMessage = "Моя ошибка: ";
////        if (!violations.isEmpty()) {
////            StringBuilder builder = new StringBuilder();
////            violations.forEach(violation -> builder.append(" " + violation.getMessage()));
////            errorMessage = builder.toString();
////        } else {
////            errorMessage = "ConstraintViolationException occured.";
////        }
//        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
//    }

}
