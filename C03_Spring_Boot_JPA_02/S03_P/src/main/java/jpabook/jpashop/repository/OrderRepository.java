package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    public List<Order> findAllString(OrderSearch orderSearch) {
        String jpql = "select o From Order o join o.member m";
        boolean isFirstCondition = true;

        // 주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }

        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }

        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
            .setMaxResults(1000); //최대 1000건

        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }

        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }

        return query.getResultList();
    }

    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);

        Root<Order> o = cq.from(Order.class);
        Join<Order, Member> m = o.join("member", JoinType.INNER);

        List<Predicate> criteria = new ArrayList<>();

        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"),
                orderSearch.getOrderStatus());
            criteria.add(status);
        }

        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name =
                cb.like(m.<String>get("name"), "%" +
                    orderSearch.getMemberName() + "%");
            criteria.add(name);
        }
        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000); // 최대 1000건

        return query.getResultList();
    }

    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery(
            "select o from Order o" +
                " join fetch o.member m" +
                " join fetch o.delivery d", Order.class
        ).getResultList();
    }

    public List<SimpleOrderQueryDto> findOrderDtos() {
        return em.createQuery(
            "select new jpabook.jpashop.repository.SimpleOrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
                " from Order o" +
                " join o.member m" +
                " join o.delivery d", SimpleOrderQueryDto.class
        ).getResultList();
    }
    // - V3랑 무슨 차이가 있는가?
    // - select 절을 내가 선택한 데이터만 퍼올려서 네트워크 비용이 감소됨
    // - V3는 다 가져옴
    // - 누가 더 좋을까?
    //      - 우열을 가리기 힘듦
    //      - V4는 재상용성이 좋지 않음 (특정 DTO에 묶여있어서)
    //      - V3는 재사용성이 좋음
    //      - 성능 자체는 V4가 조금 더 좋음
    //      - V4는 엔티티도 아니여서 뭔가 수정할 수 없음
    //      - 그래도 V3가 전체적으로 더 좋음 (단순 select 수는 그렇게 큰 영향을 주지 않음)
    //      - **이건 실 성능 테스트를 직접 해보지 않으면 절대 알 수 없음**
    //      - 그리고 기본적인 성능은 join이 많이 잡아먹음

    // 정리
    // - 그냥 완전히 최적화용 쿼리를 분리하는 것이 좋음
    // - order.simplequery 패키지 같이

    // 쿼리 방식 선택 권장 순서
    // - 우선 엔티티를 DTO로 변환하는 방법을 선택
    // - 필요하면 패치 조인으로 성능을 최적화
    // - 그래도 안되면 DTO로 직접 조회하는 방법을 사용
    // - 최후의 방법은 JPA가 제공하는 네이티브 SQL이나 스프링 JDBC Template을 사용해서 SQL을 직접 사용

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        // - v2에서 여기서 2개의 쿼리가 추가적으로 나감 (반복문을 돌고 있으므로 2 * 2 = 4개가 더 나감)
        // - 처음에 조회시 Order에서 하나의 쿼리가 나감
        //      - 결과 주문 수 2개
        //      - 2번 루프 * 2개의 추가 쿼리 = 4개 쿼리가 나감
        //      - 1 + N 문제
        //      - N(회원, 배송)
        //      - 총 1 + 회원(N) + 배송(N)
        public SimpleOrderDto(Order order) {
            this.orderId = order.getId();
            this.name = order.getMember().getName(); // 여기서 Member 쿼리 하나 나감 (Lazy 초기화)
            this.orderDate = order.getOrderDate();
            this.orderStatus = order.getStatus();
            this.address = order.getDelivery().getAddress(); // 여기서 Delivery 쿼리 하나 나감 (Lazy 초기화)
        }
    }
}
