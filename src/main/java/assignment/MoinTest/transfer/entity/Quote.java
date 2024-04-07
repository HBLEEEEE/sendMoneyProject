package assignment.MoinTest.transfer.entity;


import assignment.MoinTest.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Quote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long quoteId;

    @Column(nullable = false)
    private double sourceAmount;

    @Column(nullable = false)
    private double fee;

    @Column(nullable = false)
    private double useExchangeRate;

    @Column(nullable = false)
    private double usdAmount;

    @Column(nullable = false)
    private String targetCurrency;

    @Column(nullable = false)
    private double exchangeRate;

    @Column(nullable = false)
    private double targetAmount;

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "request_id")
    private Request request;

}
