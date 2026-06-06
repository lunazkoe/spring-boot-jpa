package jpabook.jpashop.api;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import java.time.LocalDateTime;
import java.util.List;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            for (OrderItem orderItem : orderItems) {
                orderItem.getItem().getName();
            }
        }
        return all;
    }

    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllString(new OrderSearch());
        List<OrderDto> result = orders.stream()
            .map(o -> new OrderDto(o))
            .toList();
        return result;
    }

    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        for (Order order : orders) {
            System.out.println("order = " + order);
            System.out.println("order.getId() = " + order.getId());
        }
        return orders.stream()
            .map(OrderDto::new)
            .toList();
    }
    // 주의사항 1
    // - 1대다를 컬렉션 페치 조인하는 순간 페이징 쿼리가 안나감
    // - 무슨 말이냐면 만개가 있다면 다 가져오고 난 뒤에 페이징 처리를 진행함
    // - limit, offset이 안나감
    // - 이유: DB 자체에서는 데이터가 뻥튀기됨
    //      - 그래서 페이징을 할 수 없음
    //      - 그래서 메모리로 다 퍼올리고 페이징 처리를 함 (OOM이 날 수 있음)

    // 주의사항 2
    // - 컬렉션 페치 조인은 1개만 사용할 수 있음
    // - 컬렉션 둘 이상에 페치 조인을 사용하면 아됨
    // - 데이터가 부정합하게 조회될 수 있음

    // 일대다 페치 조인 페이징 한계 돌파
    // - 먼저 ToOne(OneToOne, ManyToOne) 관계를 모두 페치조인함
    // - ToOne 관계는 row 수를 증가시키지 않으므로 페이징 쿼리에 영향을 주지 않음
    // - 컬렉션은 지연로딩으로 조회
    // - 지연 로딩 성능 최적화를 위해 hibernate.default_batch_fetch_size / @BatchSize를 적용
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(
        @RequestParam(value = "offset", defaultValue = "0") int offset,
        @RequestParam(value = "limit", defaultValue = "100") int limit
    ) {
        List<Order> orders = orderRepository.findAllWithOrderMember(offset, limit);
        return orders.stream()
            .map(OrderDto::new)
            .toList();
    }
    // - 쿼리 호출 수가 1 + M => 1 + 1로 최적화됨
    // - 페치 조인 방식과 비교해서 쿼리 호출 수가 약간 증가하지만, DB 데이터 전송량이 감소함
    // - 컬렉션 페치 조인은 페이징이 불가능하지만 이 방법은 페이징이 가능함

    // 결론
    // - ToOne은 페치 조인해도 페이징에 영향을 주지 않음

    // 참고
    // - default_batch_fetch_size의 크기는 100-1000 사이를 선택하는 것을 권장
    // - SQL IN 절을 사용하는데, 데이터베이스에 따라 IN절 파라미터를 1000으로 제한하기도 함
    // - 하지만 애플리케이션은 100이든 1000이든 전체 데이터를 로딩해야하므로 메모리 사용량이 같음
    // - DB가 순간 부하를 어디까지 견딜 수 있는지 결정 (높으면 순간 부하가 높아짐)

    // JPA에서 DTO로 조회하기
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
        // - 쿼리가 Root 쿼리 1개
        // - 현재 OrderItem이 2개 있으므로 2개의 추가 쿼리가 나감 (루프를 돌면서 채우기 때문)
        // => 1 + N 문제
    }

    // JPA에서 DTO로 조회하기 - 컬렉션 조회 최적화
    // - 이거는 이제 몇 개든 총 2개의 쿼리가 나감
    // - oi1_0.order_id in (?, ?) - in 쿼리가 나감
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }

    // 이제 더 최적화해서 쿼리 1방으로 최적화하기
    // - OrderQueryDto로 하고 싶으면 flats를 지지고 볶고 해야함
    // - V5보다 느릴 수도 있음 (쿼리가 준다는게 무조건 좋은게 아님 / 조인을 많이 함)
    // - 심지어 페이징도 안됨
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();
        return flats.stream()
            .collect(
                groupingBy(
                    o ->
                        new OrderQueryDto(
                            o.getOrderId(),
                            o.getName(),
                            o.getOrderDate(),
                            o.getOrderStatus(),
                            o.getAddress()),
                mapping(o ->
                    new OrderItemQueryDto(o.getOrderId(),
                        o.getItemName(),
                        o.getOrderPrice(),
                        o.getCount()), toList())
            ))
            .entrySet().stream()
            .map(e -> new OrderQueryDto(
                e.getKey().getOrderId(),
                e.getKey().getName(),
                e.getKey().getOrderDate(),
                e.getKey().getOrderStatus(),
                e.getKey().getAddress(),
                e.getValue()
            ))
            .collect(toList());
    }

    // API 개발 고급 정리
    // - 엔티티 조회 후 DTO로 반드시 변환할 것!!
    // - 페치 조인으로 쿼리 수를 최적화할 것!!
    //      - 일대다 페치 조인 시 페이징이 불가능한 한계
    //      - ToOne 관계는 페치 조인으로 쿼리 수 최적화
    //      - 컬렉션은 페치 조인 대신 지연로딩을 유지하고 hibernate.default_batch_fetch_size, @BatchSize로 최적화
    // - DTO 직접 조회
    //      - JPA에서 DTO를 직접 조회
    //      - 컬렉션 조회 최적화 => IN 절을 사용해서 메모리에 미리 조회해서 최적화
    //      - 플랫 데이터 최적화 - Join 결과를 그대로 조회 후 어플리케이션에서 원하는 DTO로 직접 변환

    // 권장 순서
    // - 엔티티 조회 방식으로 우선 접근 (DTO로 변환하라는 것 / 엔티티를 그대로 반환하라는게 아님)
    //      - 페치 조인으로 쿼리 수 최적화
    //      - 컬렉션 최적화
    //          - 페이징 필요
    //          - 페이징 필요 X -> 페치 조인 사용
    // - 엔티티 조회 방식으로 해결이 안되면 DTO 조회 방식 사용
    // - DTO 조회 방식으로 해결이 안되면 NativeSQL or 스프링 JdbcTemplate

    @Getter
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
//        private List<OrderItem> orderItems;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            this.orderId = order.getId();
            this.name = order.getMember().getName();
            this.orderDate = order.getOrderDate();
            this.orderStatus = order.getStatus();
            this.address = order.getDelivery().getAddress();
//            order.getOrderItems().stream().forEach(o -> o.getItem().getName());
//            // - 이것도 DTO로 바꿔야함 (지금 DTO 안에 Entity가 있음)
//            this.orderItems = order.getOrderItems();
             this.orderItems = order.getOrderItems().stream()
                 .map(orderItem -> new OrderItemDto(orderItem))
                 .toList();
        }
    }

    @Getter
    static class OrderItemDto {

        private String itemName;
        private int orderPrice;
        private int count;

        public OrderItemDto(OrderItem orderItem) {
            this.itemName = orderItem.getItem().getName();
            this.orderPrice = orderItem.getOrderPrice();
            this.count = orderItem.getOrderPrice();
        }
    }
}
