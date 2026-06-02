package hellojpa;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

public class JpaMain {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {

            Member member = new Member();
            member.setUsername("memberA");
            member.setPeriod(new Period(LocalDateTime.now(), LocalDateTime.now()));
            member.setAddress(new Address("서울", "한천로", "248"));
            em.persist(member);
            
            em.flush();
            em.clear();

            Member foundMember = em.find(Member.class, member.getId());
            System.out.println("foundMember.getUsername() = " + foundMember.getUsername());

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }

        emf.close();
    }
}

// 기본값 타입
/*
> JPA의 데이터 타입 분류
- 엔티티 타입
    - @Entity로 정의하는 객체
    - 데이터가 변해도 식별자로 지속해서 추적 가능
    - 회원 엔티티의 키나 나이 값을 변경해도 식별자로 인식 가능

- 값 타입
    - int, Integer, String처럼 단순히 값으로 사용하는 자바 기본 타입이나 객체
    - 식별자가 없고 값만 있으므로 변경 시 추적 불가
    - 숫자 100을 200으로 변경하면 완전히 다른 값으로 대체

> 값 타입 분류
- 기본값 타입
    - 자바 기본 타입(int, double)
    - 래퍼 클래스(Integer, Long)
    - String

- 임베디드 타입(embedded type, 복합 값 타입)
- 컬렉션 값 타입(collection value type)

*/

// 임베디드 타입 (복합 값 타입)
/*
> 임베디드 타입
- 새로운 값 타입을 직접 정의할 수 있음
- JPA는 임베디드 타입
- 컴포지션 값 타입
- int, String과 같은 값 타입

> 예시
- 회원 엔티티 이름, 근무 시작일, 근무 종료일, 주소 도시, 주소 번지, 주소 우편번호
- workPeriod
    - startDate
    - endDate
- homeAddress
    - city
    - street
    - zipcode

> 임베디드 타입 사용버
- @Embeddable: 값 타입을 정의하는 곳에 표시
- @Embedded: 값 타입을 사용하는 곳에 표시
- 기본 생성자 필수

> 사용하는게 사실상 동일함
- 장점:
    - 재사용성
    - 엔티티랑 같은 생명 주기
    - 불필요한 필드 제거 가능(가시성)
    - 잘 설계한 ORM 애플리케이션은 매핑한 테이블 수보다 클래스의 수가 더 많음

> 임베디드 타입과 연관관계
- 외래 키 값(엔티티를 들고 있을 수도 있음)

> 한 엔티티에서 같은 값 타입을 사용하려면?
- 컬럼영이 중복됨
- @AttributeOverrides(value = @AttributeOverride(name = "startDate", column = @Columnm("work_city"), @Attritbue...)
    - 이런식으로 하면 됨

- 참고: null로 하면 안에 모든 것에 null, 기본값으로 들어감
*/

// 값 타입과 불변 객체
/*
> 값 타입 공유 참조
- 임베디드 타입 같은 값 타입을 여러 엔티티에서 공유하면 위험함
- 부작용(side-effect) 발생

> 값 타입은 항상 불변 객체로 만들어야함 (Setter 생성 X)
*/

// 값 타입의 비교
/*
> 값 타입은 인스턴스가 달라도, 안의 값이 같으면 같은 것으로 봐야함
- 동등성: 인스턴스 참조 값을 비교 == 사용
- 동등성: 인스턴스의 값을 비교, equals() 사용
- 값 타입은 equals() 메소드를 잘 재정의해야함
    - hashCode도 잘 재정의 해주어야함!!!
*/

// 값 타입 컬렉션
/*
> RDBMS는 기본석으로 컬렉션으로 담을 수 있게 안됨
- 별도의 테이블로 뽑고 일대다로 뽑아야함

- 값 타입은 내용물 변경 시 완전히 새로 갈아끼워줘야함
- 지울 뗴 중요한 것 - equals가 잘 구현되어있어야함 remove(new Address())로 지워서 eqauls가 맞다면 지워짐

> 매우 매우 주의사항
- remove(): 이때 다 지움 - 하나 지우고 디비에서는 다 지움
- add(): 이거할 때 남아있는거 다시 넣음

- 값 타입은 엔티티와 다르게 식별자 개념이 없음
- 값은 변경하면 추적이 어려움
- 값 타입 컬렉션에 변경 사항이 발생하면, 주인 엔티티와 연관된 모든 데이터를 삭제하고,
- 값 타입 컬렉션에 있는 현재 값을 모두 다시 저장함
- 값 타입 컬렉션을 매핑하는 테이블은 모든 컬럼을 묶어서 기본 키를 구성해야함: null X, 중복 저장 X

- 자아: 그러니깐 이렇게 사용하면 안됨

> 그러면 어떻게 해야할까?
- 실무에서는 상황에 따라 값 타입 컬렉션 대신 일대다 관계를 고려함
- 일대다 관계를 위한 엔티티를 만들고 여기에서 값 타입을 사용
- 영속성 전이 + 고아 객체 제거를 사용해서 값 타입 컬렌셕처럼 사용
- ex) AddressEntity

> 언제 이렇게 사용할까?
- 진짜 단순할 때 이렇게 사용함
- 단순하게 여러 개를 선택할 때

> 양방향이 더 좋은 듯 (대신 주인도 옮기고)
*/
