package account.service;

import account.UserDetailsImpl;
import account.entity.Audit;
import account.entity.User;
import account.repository.AuditRepository;
import account.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl  implements UserDetailsService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    AuditRepository auditRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmailIgnoreCase(username);
        if (user == null){
//            Audit audit = new Audit();
//            audit.sert

            throw new UsernameNotFoundException("Not found: " + username);
        }
        return new UserDetailsImpl(user);
    }
}
