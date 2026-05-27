package jpabook.jpashop.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders") // DB의 order by라는 예약어 때문에 orders를 사용해야 함
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // @ManyToOne은 기본이 EAGER / xToOne은 기본이 EAGER
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL) // @OneToMany는 기본이 LAZY
    // - CascadeType.ALL
    // - persist(orderItemA)
    // - persist(orderItemB)
    // - persist(orderItemC)
    // - persist(order)
    // - 이렇게 안하고
    // - persist(order)로 해도 저장이 됨 / 삭제할 때도 같이 지워짐
    private List<OrderItem> orderItems = new ArrayList<>();
    // - 컬렉션은 필드에서 초기화 하자
    // - null 문제에서 안전해짐
    // - 하이버네이트는 엔티티를 영속화 할 때, 컬렉션을 감싸서 하이버네이트가 제공하는 내장 컬렉션으로 변경함
    // - 만약, getOrders() 처럼 임의의 메서드에서 컬렉션을 잘못 생성하면 하이버네이트 내부 메커니즘에 문제가 발생할 수 있음
    // - 따라서 필드 레벨에서 생성하는 것이 가장 안전하고, 코드도 간결함
    // - **컬렉션을 바꾸지 않아야 함 - 하이버네이트가 관리하는 컬렉션으로 변경되었기 때문**
    //      - 컬렉션의 내용물을 바꾸는 건 O / 컬렉션 자체를 바꾸는 건 X

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    // 기본 네이밍 전략
    // - orderDate => order_date
    // - 직접 명시하는 것도 좋음
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status; // 주문 상태 [ORDER, CANCEL]

    // == 양방향 연관관계 편의 메서드 ==
    // - 핵심이 되는 쪽이 들고 있는 것이 좋음
    public void setMember(Member member) {
        this.member = member;
        member.getOrders().add(this);
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.setOrder(this);
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    // == 생성 메서드 ==
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
        Order order = new Order();
        order.setMember(member);
        order.setDelivery(delivery);
        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    // == 비즈니스 로직 ==
    /**
     * 주문 취소
     */
    public void cancel() {
        // 이미 배송 완료 되어버리면
        if (delivery.getStatus() == DeliveryStatus.COMP) {
            throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다.");
        }

        // status Order => Cancel로 변경
        this.setStatus(OrderStatus.CANCEL);
        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }

    // == 조회 로직 ==
    /**
     * 전체 주문 가격 조회
     */
    public int getTotalPrice() {
        int totalPrice = 0;
        for (OrderItem orderItem : orderItems) {
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;
    }
}
