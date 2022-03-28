package account.component;

import account.AUDIT_EVENTS;
import account.entity.Audit;
import account.entity.User;
import account.repository.AuditRepository;
import account.repository.UserRepository;
import org.apache.catalina.core.ApplicationContext;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuthenticationFailureListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuditRepository auditRepository;
    @Autowired
    private HttpServletRequest request;



    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent e) {
       String path = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getRequestURI();
        final String xfHeader = request.getHeader("X-Forwarded-For");
//        if (xfHeader == null) {
//            String r =request.getContextPath();
//            System.out.println(r);
//        } else {
//            String r =xfHeader.split(",")[0];
//            System.out.println(r);
//        }
       User u = userRepository.findByEmailIgnoreCase((String) e.getAuthentication().getPrincipal());
        Audit audit = new Audit();
        audit.setAction(AUDIT_EVENTS.LOGIN_FAILED.name());
        audit.setObject(path);
        audit.setSubject((String) e.getAuthentication().getPrincipal());
        audit.setPath(path);
        auditRepository.save(audit);

       if (u != null){
           boolean isAdmin = false;
           for(var roles : u.getRoles()){
               if (roles.getName().equals("ROLE_ADMINISTRATOR")) {
                   isAdmin = true;
                   break;
               }
           }
           if (!isAdmin){
               u.loginFailureInc();
           }

           userRepository.save(u);
           if (u.getLogin_failure() > 4){
               Audit lock = new Audit();
               lock.setAction(AUDIT_EVENTS.LOCK_USER.name());
               lock.setObject("Lock user "+u.getEmail());
               lock.setSubject(u.getEmail());
               lock.setPath(path);

               Audit brute = new Audit();
               brute.setAction(AUDIT_EVENTS.BRUTE_FORCE.name());
               brute.setObject(path);
               brute.setSubject(u.getEmail());
               brute.setPath(path);

               auditRepository.save(brute);
               auditRepository.save(lock);
           }
       } else {
           //anonymus
       }
    }
}
