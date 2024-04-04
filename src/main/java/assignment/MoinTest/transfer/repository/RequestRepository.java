package assignment.MoinTest.transfer.repository;

import assignment.MoinTest.transfer.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findByRequestTimeBetween(LocalDateTime startOfToday, LocalDateTime endOfToday);

}
