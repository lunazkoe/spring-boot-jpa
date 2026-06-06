package jpabook.jpashop.repository.order.query;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;

    public List<OrderQueryDto> findOrderQueryDtos() {
        List<OrderQueryDto> result = findOrders();
        result.forEach(o -> {
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());
            o.setOrderItems(orderItems);
        });
        return result;
    }

    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery(
            "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)"+
                " from OrderItem oi" +
                " join oi.item i" +
                " where oi.order.id = :orderId", OrderItemQueryDto.class)
            .setParameter("orderId", orderId)
            .getResultList();
    }

    public List<OrderQueryDto> findAllByDto_optimization() {
        List<OrderQueryDto> result = findOrders(); // 여기서 쿼리 1번

        List<OrderItemQueryDto> orderItems = findOrderItems(toOrderIds(result)); // 여기서 쿼리 1번

        Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream()
            .collect(Collectors.groupingBy(orderItemQueryDto -> orderItemQueryDto.getOrderId()));

        result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));

        return result;
    }

    public List<OrderFlatDto> findAllByDto_flat() {
        return em.createQuery(
            "select new jpabook.jpashop.repository.order.query.OrderFlatDto(o.id, m.name, o.orderDate, o.status, d.address, i.name, oi.orderPrice, oi.count)" +
                " from Order o" +
                " join o.member m" +
                " join o.delivery d" +
                " join o.orderItems oi" +
                " join oi.item i", OrderFlatDto.class)
            .getResultList();
    }

    private List<OrderItemQueryDto> findOrderItems(List<Long> orderIds) {
        List<OrderItemQueryDto> orderItems = em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)"
                    +
                    " from OrderItem oi" +
                    " join oi.item i" +
                    " where oi.order.id in :orderIds", OrderItemQueryDto.class)
            .setParameter("orderIds", orderIds)
            .getResultList();
        return orderItems;
    }
    // - 문제점 distinct를 적용하지 않았을 때처럼 데이터가 중복됨
    // - 페이징을 할 수는 있지만, 우리가 원하는 페이징이 아님 (사실상 못하는 것)

    @NonNull
    private static List<Long> toOrderIds(List<OrderQueryDto> result) {
        List<Long> orderIds = result.stream()
            .map(OrderQueryDto::getOrderId)
            .toList();
        return orderIds;
    }

    public List<OrderQueryDto> findOrders() {
        return em.createQuery(
            "select new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
                " from Order o" +
                " join o.member m" +
                " join o.delivery d", OrderQueryDto.class)
            .getResultList();
    }



}
