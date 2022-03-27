package account.component;

import account.entity.User;
import account.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationSuccessEventListener implements ApplicationListener<AuthenticationSuccessEvent> {
    @Autowired
    private UserRepository userRepository;

    @Override
    public void onApplicationEvent(final AuthenticationSuccessEvent e) {
        User u = userRepository.findByEmailIgnoreCase((e.getAuthentication().getName()));
        if (u != null && u.getLogin_failure() > 0) {
            u.unlockUser();
            userRepository.save(u);
        } else {
            //anonymus
        }
    }
}
