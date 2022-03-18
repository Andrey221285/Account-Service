package account.repository;


import account.entity.User;
import org.springframework.data.repository.CrudRepository;


public interface GroupRepository  extends CrudRepository<Group, Long> {
}
