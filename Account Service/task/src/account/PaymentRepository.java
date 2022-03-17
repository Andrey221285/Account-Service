package account;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.List;

@Component
public interface PaymentRepository extends CrudRepository<Payment, Long> {

    Payment findByEmployeeIgnoreCaseAndPeriod(String emplee, String period);
    List<Payment> findByEmployeeIgnoreCase(String emplee);

}
