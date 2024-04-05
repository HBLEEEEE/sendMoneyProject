package assignment.MoinTest.transfer.repository;

import assignment.MoinTest.transfer.entity.Quote;
import assignment.MoinTest.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuoteRepository extends JpaRepository<Quote, Long> {

    List<Quote> findAllByUser(User user);

    List<Quote> findAllByUserOrderByRequestedAtDesc(User user);

}
