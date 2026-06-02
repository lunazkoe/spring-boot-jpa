package hellojpa.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class Delivery extends BaseEntity {

    @Id @GeneratedValue
    @Column(name = "delivery_id")
    private Long id;

    @Embedded
    private Address address;

    private DeliveryStatus status;

    // 여기서 LAZY가 적용이 될 수 가 없음
    // - 이유: 주인이 아니여서 어차피 가져올려면 조회를 해야함
    @OneToOne(mappedBy = "delivery", fetch = FetchType.LAZY)
    private Order order;
}
