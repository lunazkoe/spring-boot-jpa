package hellojpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

@Entity
public class Locker {

    @Id @GeneratedValue
    @Column(name = "locker_id")
    private Long id;

    private String name;

    @OneToOne(mappedBy = "locker")
    private Member member;
}
