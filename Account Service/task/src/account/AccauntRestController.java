package account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;

@RestController
public class AccauntRestController {
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder encoder;


    @PostMapping ("/api/auth/signup")
    public ResponseEntity<?> signup (@Valid @RequestBody User user){
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


}
