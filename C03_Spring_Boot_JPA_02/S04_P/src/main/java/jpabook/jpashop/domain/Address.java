package jpabook.jpashop.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
// - 값 타입은 변경 불가능하게 설계해야 함
public class Address {

    private String city;
    private String street;
    private String zipcode;

    protected Address() {
    }

    // 변경 불가능하게 설계하기 위해서 생성자 외에 데이터를 수정할 수 있는 걸 만들면 안됨
    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
