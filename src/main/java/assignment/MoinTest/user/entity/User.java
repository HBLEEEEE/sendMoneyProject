package assignment.MoinTest.user.entity;


import assignment.MoinTest.transfer.entity.Quote;
import assignment.MoinTest.transfer.entity.Request;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private UserIdTypeEnum idType;

    @Column(nullable = false)
    private String idValue;

    @OneToMany(mappedBy = "user")
    private List<Quote> quotes = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Request> requests = new ArrayList<>();

}
