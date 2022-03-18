package account.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
public class AdminController {



    //GET api/admin/user
    //DELETE api/admin/user
    //PUT api/admin/user/role

    @GetMapping("api/admin/user")
    public ResponseEntity<?> getAllUsers(@AuthenticationPrincipal UserDetails details) {
        return null;
    }
    @DeleteMapping("api/admin/user")
    public ResponseEntity<?> deleteUser(@AuthenticationPrincipal UserDetails details) {

        return null;
    }
    @PutMapping("api/admin/user/role")
    public ResponseEntity<?> updateUserRole(@AuthenticationPrincipal UserDetails details) {

        return null;
    }


}
