package account;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Min;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @Column
    String employee;

    @Column
    String  period;

    @Min(value = 0)
    @Column
    Long salary;

    @ManyToOne
    @JoinColumn(name="user_id", nullable=false)
    private User user;
}
