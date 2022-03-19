package account.repository;

import account.entity.Payment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends CrudRepository<Payment, Long> {

    Payment findByEmployeeIgnoreCaseAndPeriod(String emplee, String period);
    List<Payment> findByEmployeeIgnoreCase(String emplee);

}
