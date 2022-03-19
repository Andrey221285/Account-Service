package account.controller;

import account.dto.UserDto;
import account.entity.User;
import account.repository.GroupRepository;
import account.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

@RestController
public class AdminController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @GetMapping("api/admin/user")
    //@ResponseStatus(value = HttpStatus.FORBIDDEN,reason = "Access Denied!")
    public ResponseEntity<?> getAllUsers(@AuthenticationPrincipal UserDetails details) {
        var set = userRepository.findAll();
        TreeSet<UserDto> userDtos = new TreeSet<>(Comparator.comparing(t->t.getId()));
        set.forEach(t->userDtos.add(new UserDto(t)));

        return new ResponseEntity<>(userDtos, HttpStatus.OK);
    }
    @DeleteMapping(value ={"api/admin/user/","api/admin/user/{userEmail}"} )
    public ResponseEntity<?> deleteUser(@AuthenticationPrincipal UserDetails details,@PathVariable String userEmail) {
        User user = userRepository.findByEmailIgnoreCase(userEmail);
        if (user != null){
            if (user.getRoles().contains(groupRepository.findByName("ROLE_ADMINISTRATOR"))){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR role!");
            }

            userRepository.delete(user);
            Map<String,String> map = new HashMap<>();
            map.put("user", user.getEmail().toLowerCase());
            map.put("status", "Deleted successfully!");
            return new ResponseEntity<>(map, HttpStatus.OK);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found!");
    }
    @PutMapping("api/admin/user/role")
    public ResponseEntity<?> updateUserRole(@AuthenticationPrincipal UserDetails details) {

        return null;
    }


}
