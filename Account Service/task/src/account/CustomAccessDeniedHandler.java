package account;

import account.entity.Audit;
import account.repository.AuditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private AuditRepository auditRepository;

    public CustomAccessDeniedHandler(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exc) throws IOException, ServletException {
        Authentication auth
                = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            System.out.println("User: " + auth.getName()
                    + " attempted to access the protected URL: "
                    + request.getRequestURI());
            Audit audit = new Audit();
            audit.setAction(AUDIT_EVENTS.ACCESS_DENIED.name());
            audit.setSubject(auth.getName());
            audit.setObject(request.getRequestURI());
            audit.setPath(request.getRequestURI());
            auditRepository.save(audit);
        }


        response.sendError(HttpStatus.FORBIDDEN.value(), "Access Denied!");
    }
}
