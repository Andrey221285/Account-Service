package account.controller;

import account.AUDIT_EVENTS;
import account.entity.Audit;
import account.exceptions.UserExistException;
import account.dto.ChangePasswordDto;
import account.dto.PaymentResponseDto;
import account.dto.UserDto;
import account.entity.Group;
import account.entity.Payment;
import account.entity.User;
import account.repository.AuditRepository;
import account.repository.GroupRepository;
import account.repository.PaymentRepository;
import account.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
public class AccauntRestController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    PaymentRepository paymentRepository;
    @Autowired
    GroupRepository groupRepository;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    AuditRepository auditRepository;

    private HashSet<String> hackedPassword = new HashSet<>(Arrays.asList("PasswordForJanuary", "PasswordForFebruary", "PasswordForMarch", "PasswordForApril",
            "PasswordForMay", "PasswordForJune", "PasswordForJuly", "PasswordForAugust",
            "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember", "PasswordForDecember"));

    @ExceptionHandler({ConstraintViolationException.class})
    @PostMapping("/api/auth/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody User user) {
        if (user.getPassword().length() < 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password length must be 12 chars minimum!");
        }


        if (hackedPassword.contains(user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password is in the hacker's database!");
        }


        User userOld = userRepository.findByEmailIgnoreCase(user.getEmail());
        if (userOld == null) {
            user.setPassword(encoder.encode(user.getPassword()));
            Set<Group> g = new HashSet<>();
            if(userRepository.count()==0){
                g.add( groupRepository.findByName("ROLE_ADMINISTRATOR"));
            } else {
                g.add( groupRepository.findByName("ROLE_USER"));
            }
            user.setRoles(g);
            userRepository.save(user);
            Audit audit = new Audit();
            audit.setAction(AUDIT_EVENTS.CREATE_USER.name());
            audit.setPath("/api/auth/signup");
            audit.setSubject("Anonymous");
            audit.setObject(user.getEmail().toLowerCase());
            auditRepository.save(audit);


            return new ResponseEntity<>(new UserDto(user), HttpStatus.OK);
        }

        throw new UserExistException();
    }

    @GetMapping("api/empl/payment")
    public ResponseEntity<?> getPayment(@AuthenticationPrincipal UserDetails details,@RequestParam(required = false) String period) {
        if (details != null) {
            User user = userRepository.findByEmailIgnoreCase(details.getUsername());

            if (period == null){
                List<PaymentResponseDto> list = new ArrayList<>();
                List<Payment> payments = paymentRepository.findByEmployeeIgnoreCase(user.getEmail());
                for (Payment p : payments){
                    list.add(createPaymentMessage(user, p));
                }
                list.sort(Comparator.comparing(PaymentResponseDto::periodYearMonth).reversed());

                return new ResponseEntity<>(list, HttpStatus.OK);
            } else {
                Payment p = paymentRepository.findByEmployeeIgnoreCaseAndPeriod(user.getEmail(),period);
                if (p!=null){
                    return new ResponseEntity<>(createPaymentMessage(user, p), HttpStatus.OK);
                }else {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "указан неверный период");
                }
            }
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }


    private PaymentResponseDto createPaymentMessage(User user, Payment p) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
        YearMonth ym = YearMonth.parse(p.getPeriod(), formatter);
        return new PaymentResponseDto(user.getName(),user.getLastname(),ym,salaryToString(p.getSalary()));
    }

    private static String salaryToString(long salary){
        long dollar = salary/100;
        long cent = salary%100;
        String s = dollar + " dollar(s) " + cent + " cent(s)";
        return s;

    }

    @PostMapping("api/auth/changepass")
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal UserDetails details, @Valid @RequestBody ChangePasswordDto changePasswordDto) {
        if (details != null) {
            if (changePasswordDto.getPassword().length() < 12) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password length must be 12 chars minimum!");
            }
            System.out.println("password = " + changePasswordDto.getPassword());
            if (hackedPassword.contains(changePasswordDto.getPassword())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password is in the hacker's database!");
            }
            User user = userRepository.findByEmailIgnoreCase(details.getUsername());
            if (encoder.matches(changePasswordDto.getPassword(), user.getPassword())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The passwords must be different!");
            }
            user.setPassword(encoder.encode(changePasswordDto.getPassword()));
            userRepository.save(user);
            Audit audit = new Audit();
            audit.setAction(AUDIT_EVENTS.CHANGE_PASSWORD.name());
            audit.setObject(user.getEmail().toLowerCase());
            audit.setSubject(user.getEmail().toLowerCase());
            audit.setPath("api/auth/changepass");
            auditRepository.save(audit);


            HashMap<String, String> map = new HashMap<>();
            map.put("email", user.getEmail().toLowerCase());
            map.put("status", "The password has been updated successfully");

            return new ResponseEntity<>(map, HttpStatus.OK);
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }

    @PostMapping("api/acct/payments")
    @Transactional
    public ResponseEntity<?> addPayments(@RequestBody List<Payment> payments) {
        saveAllPayments(payments);
        HashMap<String, String> m = new HashMap<>();
        m.put("status", "Added successfully!");
        return new ResponseEntity<>(m, HttpStatus.OK);
    }


    public void saveAllPayments(List<Payment> payments) {
//        paymentRepository.saveAll(payments);

        for (var p : payments) {
            if (p.getSalary() <=0){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "отрицательная зарплата");
            }
            YearMonth ym = null;
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
                 ym = YearMonth.parse(p.getPeriod(),formatter);
            } catch (Exception e) {
                //e.printStackTrace();
            }

            if (ym == null){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "неправильная дата");
            }

            User u = userRepository.findByEmailIgnoreCase(p.getEmployee());
            if (u != null) {
                Payment pp = paymentRepository.findByEmployeeIgnoreCaseAndPeriod(p.getEmployee(), p.getPeriod());
                if (pp!=null){
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ведомость уже существует");
                }
                p.setUser(u);
                paymentRepository.save(p);
            } else {
//                throw new RuntimeException();
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "нету юзера");
            }
        }
    }

    @PutMapping("api/acct/payments")
    public ResponseEntity<?> updatePayments(@RequestBody Payment newPayment) {
        if (newPayment.getSalary() <=0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "отрицательная зарплата");
        }

        User u = userRepository.findByEmailIgnoreCase(newPayment.getEmployee());
        if (u != null) {
            Payment oldPayment = paymentRepository.findByEmployeeIgnoreCaseAndPeriod(newPayment.getEmployee(), newPayment.getPeriod());
            if (oldPayment!=null){
                oldPayment.setSalary(newPayment.getSalary());
                paymentRepository.save(oldPayment);

            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error!");
            }
        } else {
//                throw new RuntimeException();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "нету юзера");
        }
        HashMap<String, String> m = new HashMap<>();
        m.put("status", "Updated successfully!");
        return new ResponseEntity<>(m, HttpStatus.OK);
    }

    @GetMapping("api/security/events")
    public ResponseEntity<?> getAuditEvents(){
        return new ResponseEntity<>(auditRepository.findAll(),HttpStatus.OK);
    }


}
