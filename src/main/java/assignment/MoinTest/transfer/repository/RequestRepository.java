package assignment.MoinTest.transfer.repository;

import assignment.MoinTest.transfer.entity.Quote;
import assignment.MoinTest.transfer.entity.Request;
import assignment.MoinTest.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findByUserAndRequestTimeBetween(User user, LocalDateTime startOfToday, LocalDateTime endOfToday);

    List<Request> findByUserAndRequestTimeBetweenOrderByRequestTimeDesc(User user, LocalDateTime startOfToday, LocalDateTime endOfToday);

    List<Request> findAllByQuote(Quote quote);

}
