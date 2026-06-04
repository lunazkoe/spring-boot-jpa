package jpabook.jpashop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class S01PApplication {

    public static void main(String[] args) {
        SpringApplication.run(S01PApplication.class, args);
    }

}

// JPA가 만들어준 DB 테이블을 보고 반드시 다듬어야함!!