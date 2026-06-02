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

//            Member member = new Member();
//            member.setUsername("hello");
//
//            em.persist(member);
//
//            em.flush();
//            em.clear();
//
//            System.out.println("=====================================");
////            Member foundMember = em.find(Member.class, member.getId());
//            Member foundMember = em.getReference(Member.class, member.getId());
//            System.out.println("foundMember.getClass() = " + foundMember.getClass());
//            // - class hellojpa.Member$HibernateProxy$kGA44rtc
//            // - Proxy: id 값만 들고 있는 가짜가 반환됨
//            // - 실제 클래스를 상속 받아서 만듦
//            System.out.println("foundMember.getId() = " + foundMember.getId()); // 이때까지도 쿼리가 안나감 (ID를 이미 넣어주었으므로)
//            System.out.println("foundMember.getUsername() = " + foundMember.getUsername()); // 이때 쿼리가 나감

            //
//            Member member1 = new Member();
//            member1.setUsername("member1");
//            em.persist(member1);
//
//            Member member2 = new Member();
//            member2.setUsername("member2");
//            em.persist(member2);
//
//            em.flush();
//            em.clear();
//
//            Member foundMember1_1 = em.getReference(Member.class, member1.getId());
//            Member foundMember1_2 = em.find(Member.class, member1.getId());
//            System.out.println("A: " + foundMember1_1.getClass()); // 이 경우에는 Proxy를 영속성 컨텍스트에 있는 건가?
//            System.out.println("B: " + foundMember1_2.getClass());
//            System.out.println(foundMember1_1 == foundMember1_2);
            
            // FetchType.LAZY
//            Team team = new Team();
//            team.setName("teamA");
//            em.persist(team);
//
//            Team team2 = new Team();
//            team.setName("teamB");
//            em.persist(team2);
//
//            Member member = new Member();
//            member.setUsername("memberA");
//            member.changeTeam(team);
//            em.persist(member);
//
//            Member member2 = new Member();
//            member2.setUsername("memberB");
//            member2.changeTeam(team);
//            em.persist(member2);
//
//            em.flush();
//            em.clear();
//
//            List<Member> members = em.createQuery("select m from Member m", Member.class)
//                .getResultList();

//            System.out.println("====================================");
//            Member foundMember = em.find(Member.class, member.getId());
//            System.out.println("foundMember.getClass().getName() = " + foundMember.getClass().getName());
//            System.out.println("foundMember.getTeam().getClass().getName() = " + foundMember.getTeam().getClass().getName());
//            // - 프록시로 조회함
//            System.out.println("=====================================");
//            System.out.println("foundMember.getTeam().getName() = " + foundMember.getTeam().getName());
            // - 실제 팀에 대한 정보를 조회하는 시점 (Id 제외) Team을 가져옴(teamId 기반으로)

            // 영속성 전이
            Child child1 = new Child();
            Child child2 = new Child();

            Parent parent = new Parent();
            parent.addChild(child1);
            parent.addChild(child2);

            // 이렇게 저장을 해주어야 함 - parent만 하게 되면 child는 영송성 컨텍스트에 저장이 되지 않음
            em.persist(parent);
            em.persist(child1);
            em.persist(child2);
            // - cascade = CascadeType.ALL을 해주면 parent만해도 child도 같이 저장해줌

            em.flush();
            em.clear();

            Parent foundParent = em.find(Parent.class, parent.getId());
//            foundParent.getChildList().remove(0); // - 이때 해당 자식 엔티티를 삭제함(DB에서)
            em.remove(foundParent);

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }

        emf.close();
    }
}

// 프록시
/*
> Member를 조회할 때 Team도 함께 조회해야 할까?
- 회원과 팀 정보를 같이 출력하는 경우
- 회원만 출력하는 경우 -> 팀이 굳이 필요 없음

> 프록시 기초
- em.find() vs em.getReference()
    - em.find(): 데이터베이스를 통해서 실제 엔티티 객체 조회
    - em.getReference(): 데이터베이스 조회를 미루는 가짜(프록시) 엔티티 객체 조회

> 프록시 특징
- Proxy
    - Entity target
    - getId()
    - getName()

- Entity
    - id
    - name
    - getId()
    - getName()

- 프록시 객체를 호출하면, 실제 객체의 메소드를 호출

> 프록시 객체의 초기화
- proxy.getName() -> 근데 실제 엔티티가 아님
- 초기화 요청
- 영속성 컨텍스트 -> DB 조회 -> 실제 엔티티 생성
- target.getName()

> 주의: 프록시 특징
- 프록시 객체는 처음 사용할 때 한 번만 초기화
- 프록시 객체를 초기화할 때, 프록시 객체가 실제 엔티티로 바뀌는 것이 아님
- 초기화가 되면, 프록시를 통해서 실제 엔티티에 접근하는 것
- 프록시 객체는 원본 엔티티를 상속받음. 따라서 타입 체크 시 주의해야함 (== 비교 실패, 대신 instance of 사용)
- 영속성 컨텍스트에 찾는 엔티티가 이미 있으면 em.getReference()를 호출해도 실제 엔티티 반환
    - em.find() / em.getReference()로 **같은 걸** 가져왔으면 ==으로 true를 반환해주어야함
    - 이유는 JPA는 기본적으로 영속성 컨텍스트에 있는 같은 엔티티(같은 키값)에 대해서 ==이 true가 나와야함
- 영속성 컨텍스트의 도움을 받을 수 없는 준영속 상태일 때, 프록시를 초기화하면 문제 발생

> 프록시 확인
- 프록시 인스턴스 초기화 여부 확인
    - PersistenceUnitUtil.isLoaded(Object entity)

- 프록시 클래스 확인 방법
    - entity.getClass().getNam()

- 프록시 강제 초기화
    - initialize(entity) - 하이버네이트가 제공하는 것 - JPA는 제공안함

- 참고: JPA 표준은 강제 초기화 없음
    - 강제 호출: member.getName()
*/

// 즉시 로딩과 지연 로딩
/*
> FetchType.LAZY
- member는 로딩
- 지연 로딩으로 팀은 프록시로 조회
- 실제 team을 사용하는 시점에 초기화

> FetchType.EAGER
- 연관된 객체 전부 즉시 조회 (Join해서 가져옴)

> 선택지
- 한 번에 가져올지 따로 나눠서 가져올지 정함

> 프록시와 즉시 로딩 주의
- 가급적 지연 로딩만 사용(특히 실무에서)
- 즉시 로딩을 적용하면 예상하지 못한 SQL이 발생
- 즉시 로딩은 JPQL에서 N+1 문제를 일으킴
    - select m from Member m;
    - Eager로 세팅했는데도 쿼리가 두 개가 나옴
    - 근데 JPQL 입장에서 위 쿼리를 일단 그대로 실행
    - 근데 EAGER로 설정되어있으므로 Team도 가져와야함
    - 이때 Team을 또 select해서 가져옴
        - 주의 이때 가져오는 Team은 조회한 Member에 있는 팀을 각각 가져옴
        - 만약 멤버 2명이 같은 팀이면 Team 1개쿼리 더 실행
        - 다른 팀이면 Team 2개 쿼리 실행
- @ManyToOne / @OneToOne은 기본이 즉시 로딩 -> Lazy로 설정
- @OneToMany, @ManyToMany는 기본이 지연 로딩

- 지연 로딩으로 설정하고 즉시 로딩을 JPQL로 구현할 수 있음
- 또는 batch size
*/

// 영속성 전이
/*
> 영속성 전이란
- 특정 엔티티를 영속 상태로 만들 때, 연관된 엔티티도 함께 영속 상태로 만들고 싶을 때
- 예: 부모 엔티티를 저장할 때, 자식 엔티티도 같이 저장하고 싶을 때

> 주의
- 영속성 전이는 연관관계를 매핑하는 것과 아무 관련이 없음
- 엔티티를 영속화할 때 연관된 엔티티도 함께 영속화하는 편리함을 제공할 뿐!

- 종류
    - ALL: 모두 적용
    - PERSIST: 영속 - 저장할 때만 써야할 때, REMOVE는 적용하고 싶지 않을 때 정도 사용하고, 그 외에는 ALL을 사용하는 편
    - REMOVE: 삭제
    - MERGE: 병합
    - REFRESH: REFRESH
    - DETACH: DETACH

- 그래서 언제 사용하는 것이 좋을까?
    - 부모가 자식을 완전히 관리할 때 (소유자가 하나일 때)
    - 근데 자식이 다른 곳에서도 사용되는 경우에는 사용하면 안됨

> 고아 객체
- 고아 객체 제거: 부모 엔티티와 연관관계가 끊어진 자식 엔티티를 자동으로 삭제
- orphanRemoval = true

- 주의
    - 특정 엔티티가 개인 소유할 때 사용
    - @OneToOne, @OneToMany만 가능

- 참고
    - 개념적으로 부모를 제거하면 자식은 고아가 됨
    - 따라서 객체 제거 기능을 활성화 하면, 부모를 제거할 때 자식도 함께 제거됨
    - 이것은 CascadeType.REMOVE처럼 동작함

> 영속성 전이 + 고아 객체, 생명주기
- CascadeType.ALL + orphanRemoval = true
- 스스로 생명주기를 관리하는 엔티티는 em.persist()로 영속화, em.remove()로 제거
- 두 옵션을 모두 활성화하면 부모 엔티티를 통해서 자식의 생명 주기를 관리할 수 있음
- 도메인 주도 설계에서 Aggregate Root 개념을 구현할 때 유용
*/