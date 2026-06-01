package hellojpa;

import jakarta.persistence.*;
import java.util.List;

public class JpaMain {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {

            // 일대다
            Member member = new Member();
            member.setUsername("member1");
            em.persist(member);

            Team team = new Team();
            team.setName("teamA");
            team.getMembers().add(member); // 이때 외래키가 저장됨
            // - 문제: 여기는 team에 insert로 처리할 수 있는 문제가 아님
            // - 업데이트 문이 날아감 (member를 업데이트 함)
            em.persist(team);

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }

        emf.close();
    }
}

// 연관관계 매핑 시 고려사항 3가지
// - 다중성
//      - 다대일: @ManyToOne
//      - 일대다: @OneToMany
//      - 일대일: @OneToOne
//      - 다대다: @ManyToMany
// - 단방향, 양방향
//      - 테이블
//          - 외래 키 하나로 양쪽 조인 가능
//          - 사실 방향이라는 개념이 없음
//      - 객체
//          - 참조용 필드가 있는 쪽으로만 참조 가능
//          - 한쪽만 참조하면 단방향
//          - 양쪽이 서로 참조하면 양방향
// - 연관관계의 주인
//      - 테이블은 외래 키 하나로 두 테이블이 연관관계를 맺음
//      - 객체 양방향 관계는 A->B, B->A 처럼 참조가 2군데
//      - 객체 양방향 관계는 참조가 2군데 있음. 둘 중 테이블의 외래 키를 관리할 곳을 지정해야함
//      - 연관관계의 주인: 외래 키를 관리하는 참조
//      - 주인의 반대편: 외래 키에 영향을 주지 않음. **단순 조회용**

// 다대일 단방향
// - 다쪽에 외래 키를 설정
// - 가장 많이 사용하는 연관관계
// - 다대일의 반대는 일대다

// 다대일 양방향
// - 반대에 있다고 컬럼에 추가되는 것이 아님(@OneToMany(mappedBy))
// - 외래 키가 있는 쪽이 연관관계의 주인
// - 양쪽을 서로 참조하도록 개발

// 일대다 단방향 (양방향은 변태같은 구조 ㅋㅋ)
// - 일이 연관관계의 주인
// - 이 모델은 별로 좋지 않음
// - 팀에만 List<Member>를 가지고 있음
// - 근데 DB에는 Team에 외래키가 들어갈 수가 없음
//      - 논리적으로 생각해보면 Team에 Member의 외래키를 들고 있다고 하면 Member 수마다 들고 있게 할 수는 없음
//      - 인원이 한정되어있다면 고려해볼 수는 있다고 하더라도
// - 이대로 설계를 진행하면 Team의 List<Members>에 값을 변경하면 Member의 Team 외래키를 건들여야함
// - 추천: 다대일 관계로 설정하는 것이 좋음

// - 단점
//      - 엔티티가 관리하는 외래 키가 다른 테이블에 있음
//      - 연관관계 관리를 위해 추가로 UPDATE SQL 실행
// - 결론: 일대다 단방향 매핑보다는 다대일 양방향 매핑을 사용하자!!

// 일대일
// - 주 테이블이나 대상 테이블 중에서 외래 키 선택 가능
//      - 주 테이블에 외래 키
//      - 대상 테이블에 외래 키
// - 외래 키에 데이터베이스 유니크 제약조건 추가 (일대일이기 때문에!!)

// - 예시 (Member - Locker)
//      - Member에 Locker_Id를 넣어도 됨
//      - Locker에 Member_Id를 넣어도 됨

// - 일대일 양방향
//      - 다대일처럼 하면 됨
//      - 연관관계의 주인은 필요함
//      - mappedBy 활용
//      - JoinColumn 활용
//      - 주 테이블에 외래 키를 두는 것을 권장
//          - 주 테이블에서 조회하는 경우가 많음 (member)
//          - 변경하는 것도 많음
// - 주의: 대상 테이블에 외래 키 **단방향**은 지원조차 안됨 (양방향은 됨)
//      - 무슨 말이냐면 Member에 Locker를 두고, 외래키는 없이
//      - Locker에 Member_Id를 외래키로 두는 경우를 말함

// - 트레이드 오프
//      - 멤버와 락커 중 누가 연관관계의 주인(외래키)를 가지는 좋을까?
//      - A: 멤버: 나중에 멤버가 락커를 여러 개 가질 수 있다고 하면 조금 수정사항이 생김
//      - B: 락커: 나중에 멤버가 락커를 여러 개 가질 수 있다고 하면 락커의 멤버키의 unique를 빼면됨
//      - C: 락커가 멤버를 여러 개 가질 경우는 위의 내용의 반대

// - 어떻게 해야할까?
//      - 조회를 많이 하는 대상에게 있는 것이 좋음 (주 테이블)
//      - 근데 락커에 있을 경우에는 지연로딩을 해도 Proxy의 한계 때문에 즉시 로딩됨
//          - 이유는 멤버를 조회했을 때 Locker를 가져와야하는데 이때 Table에는 Locker가 없기 때문에
//          - 어차피 Locker를 조회해야함 (그래야 null을 넣을지 말지 결정할 수 있음)
// - 결론: 주 테이블에 넣도록 하자 일단은

// 다대다
// - 관계형 데이터베이스는 정규화된 테이블 2개로 다대다 관계를 표현할 수 없음
// - 연결 테이블을 추가해서 일대다, 다대일 관계로 풀어내야함
// - 딜레마: 객체는 다대다가 됨 (컬렉션을 활용하면)

// - 매우 주의
//      - 편리해보이지만 실무에서 사용 X
//      - 연결 테이블이 단순히 연결만 하고 끝나지 않음
//      - 주문시간, 수량 같은 데이터가 들어올 수 있음

// - 다대다 한계 극복
//      - 연결 테이블 용 엔티티 추가 (연결 테이블을 엔티티로 승격)
//      - @ManyToMany -> @OneToMany, @ManyToOne

// - 참고:
//      - 다대다 중간 테이블에서 외래키끼리 묶어서 PK로 두는 것보다 그냥 PK를 만드는 것이 좋음 (편함, 문제도 없음)
//      - 외래 키간에 제약을 두면 됨