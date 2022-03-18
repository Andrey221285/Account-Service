package account.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @Column
    private String employee;

    @Column
    private String  period;

//    @Min(value = 0)
    @Column
    private Long salary;

    @ManyToOne
    @JoinColumn(name="user_id", nullable=false)
    private User user;
}
