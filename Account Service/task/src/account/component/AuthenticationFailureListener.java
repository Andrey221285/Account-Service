package account.component;

import account.entity.User;
import account.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFailureListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {
    @Autowired
    private UserRepository userRepository;

    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent e) {
       User u = userRepository.findByEmailIgnoreCase((String) e.getAuthentication().getPrincipal());
       if (u != null){
           u.loginFailureInc();
           userRepository.save(u);
       } else {
           //anonymus
       }
    }
}
