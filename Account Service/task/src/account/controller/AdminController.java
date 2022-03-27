package account.controller;

import account.dto.AdminChangeAccessDto;
import account.exceptions.UserNotFoundException;
import account.dto.AdminUpdateUserDto;
import account.dto.UserDto;
import account.entity.Group;
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

import javax.validation.Valid;
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
    public ResponseEntity<?> getAllUsers(@AuthenticationPrincipal UserDetails details) {
        var set = userRepository.findAll();
        TreeSet<UserDto> userDtos = new TreeSet<>(Comparator.comparing(t -> t.getId()));
        set.forEach(t -> userDtos.add(new UserDto(t)));

        return new ResponseEntity<>(userDtos, HttpStatus.OK);
    }

    @DeleteMapping(value = {"api/admin/user", "api/admin/user/{userEmail}"})
    public ResponseEntity<?> deleteUser(@AuthenticationPrincipal UserDetails details, @PathVariable String userEmail) {
        User user = userRepository.findByEmailIgnoreCase(userEmail);
        if (user != null) {
            if (user.getRoles().contains(groupRepository.findByName("ROLE_ADMINISTRATOR"))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR role!");
            }

            userRepository.delete(user);
            Map<String, String> map = new HashMap<>();
            map.put("user", user.getEmail().toLowerCase());
            map.put("status", "Deleted successfully!");
            return new ResponseEntity<>(map, HttpStatus.OK);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!");
    }

    @PutMapping("api/admin/user/role")
    public ResponseEntity<?> updateUserRole(@AuthenticationPrincipal UserDetails details, @RequestBody AdminUpdateUserDto updateUserDto) {
        User user = userRepository.findByEmailIgnoreCase(updateUserDto.getUser());
        String operation = updateUserDto.getOperation();
        if (user != null) {
            Group group = groupRepository.findByName("ROLE_" + updateUserDto.getRole());
            if (group == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found!");
            }

            if (operation.equals("GRANT")) {
                boolean isAdmin = false;
                for (var role : user.getRoles()) {
                    if (role.getName().equals("ROLE_ADMINISTRATOR")) {
                        isAdmin = true;
                        break;
                    }
                }
                if (!isAdmin && group.getName().equals("ROLE_ADMINISTRATOR")) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user cannot combine administrative and business roles!");
                }
                if (isAdmin && (group.getName().equals("ROLE_USER")
                        || group.getName().equals("ROLE_ACCOUNTANT")
                        || group.getName().equals("ROLE_AUDITOR"))) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user cannot combine administrative and business roles!");
                }

                user.getRoles().add(group);
                userRepository.save(user);
                return new ResponseEntity<>(new UserDto(user), HttpStatus.OK);
            } else if (operation.equals("REMOVE")) {
                if (group.getName().equals("ROLE_ADMINISTRATOR")) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR role!");
                }
                if (!user.getRoles().contains(group)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user does not have a role!");
                }
                if (user.getRoles().size() == 1) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user must have at least one role!");
                }
                user.getRoles().remove(group);
                userRepository.save(user);
                return new ResponseEntity<>(new UserDto(user), HttpStatus.OK);
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unknown operation:" + operation);
            }
        } else {
            throw new UserNotFoundException();
        }
    }

    @PutMapping("api/admin/user/access")
    public ResponseEntity<?> changeUserAccess(@RequestBody @Valid AdminChangeAccessDto changeAccess) {
        User user = userRepository.findByEmailIgnoreCase(changeAccess.getUser());
        if (user != null) {
            if (changeAccess.getOperation().equals("LOCK")) {
                if (user.getRoles().contains(groupRepository.findByName("ROLE_ADMINISTRATOR"))) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't lock the ADMINISTRATOR!");
                }
                user.lockUsuer();
            } else if (changeAccess.getOperation().equals("UNLOCK")){
                user.unlockUser();
            }
            userRepository.save(user);
            Map<String, String> map = new HashMap<>();

            map.put("status", "User " + user.getEmail().toLowerCase()
                    + " " + (changeAccess.getOperation().equals("LOCK") ? "locked!" : "unlocked!"));
            return new ResponseEntity<>(map, HttpStatus.OK);
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!");
    }

}
