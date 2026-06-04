package jpabook.jpashop.api;

import java.time.LocalDateTime;
import java.util.List;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.SimpleOrderQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * XToOne
 * Order
 * Order -> Member (ManyToOne)
 * Order -> Delivery (OneToOne)
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> orderV1() {
        List<Order> all = orderRepository.findAllString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); // Lazy 강제 초기화
            order.getDelivery().getAddress();
        }
        return all; // 양방향이라 무한 루프에 빠짐
    }
    // - 이렇게 만들면 사실상 절대 안됨

    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> orderV2() {
        List<Order> orders = orderRepository.findAllString(new OrderSearch());
        return orders.stream()
            .map(SimpleOrderDto::new)
            .toList();
    }

    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> orderV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        // - Member랑 Delivery를 한 번에 영속성 컨텍스트에 올려서 조회 시 바로 영속성 컨텍스트에 올림
        return orders.stream()
            .map(SimpleOrderDto::new)
            .toList();
    }

    @GetMapping("/api/v4/simple-orders")
    public List<SimpleOrderQueryDto> orderV4() {
        return orderSimpleQueryRepository.findOrderDtos();
    }

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
