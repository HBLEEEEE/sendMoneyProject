package assignment.MoinTest.user.repository;

import assignment.MoinTest.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUserId(String userId);

    List<User> findAllByUserId(String userId);
}
