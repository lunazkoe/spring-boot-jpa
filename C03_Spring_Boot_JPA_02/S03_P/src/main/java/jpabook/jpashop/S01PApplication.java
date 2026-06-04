package jpabook.jpashop;

import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class S01PApplication {

    public static void main(String[] args) {
        SpringApplication.run(S01PApplication.class, args);
    }

    @Bean
    Hibernate5JakartaModule hibernate5JakartaModule() {
        return new Hibernate5JakartaModule();
    }
}

// 지연 로딩과 조회 성능 최적화

/* 간단한 주문 조회 V1: 엔티티를 직접 노출
> 엔티티를 직접 노출할 대는 양방향 연관관계가 걸린 곳은 꼭! 한 곳을 @JsonIgnore처리 - 무한 루프 방지
> 엔티티를 API 응답으로 외부 로 노출하는 것은 좋지 않음 -> DTO로 변환하자!!
> 지연 로딩을 피하려고 즉시 로딩을 하면 절대 안됨 -> Fetch Join을 사용해야함
*/