package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id") // DB 관점에서 보면 테이블명_id로 해주는 것이 좋음
    private Long id;

    private String name;

    @Embedded
    private Address address;

//    @JsonIgnore // Json에서 빠짐 - 이렇게 하면 안됨 (다른 곳에서는 필요할 수도 있음)
    @OneToMany(mappedBy = "member") // Order Table에 있는 Member에 매핑된 거울이라는 의미(읽기 전용)
    private List<Order> orders = new ArrayList<>();
}
