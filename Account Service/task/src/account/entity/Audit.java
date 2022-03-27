package account.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "audit")
public class Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private LocalDateTime date;
    @Column
    private String action;
    @Column
    private String subject = "Anonymous";
    @Column
    private String object;
    @Column
    private String path;

    @PrePersist
    public void onPrepersist() {
        date = LocalDateTime.now();
    }

    @PreUpdate
    public void onPreUpdate() {
        date = LocalDateTime.now();
    }

}
