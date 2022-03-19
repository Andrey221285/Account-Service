package account.repository;


import account.entity.Group;
import account.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository  extends CrudRepository<Group, Long> {

    public Group findByName(String name);
}
