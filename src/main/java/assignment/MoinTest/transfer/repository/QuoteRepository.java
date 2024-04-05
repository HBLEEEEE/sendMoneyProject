package assignment.MoinTest.transfer.repository;

import assignment.MoinTest.transfer.entity.Quote;
import assignment.MoinTest.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuoteRepository extends JpaRepository<Quote, Long> {

    // 필요하면 조회할때 orderBy까지
    List<Quote> findAllByUser(User user);
}
