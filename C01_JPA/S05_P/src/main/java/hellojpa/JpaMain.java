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
//            Team team = new Team();
//            team.setName("TeamA");
//            em.persist(team);
//
//            Member member = new Member();
//            member.setUsername("member1");
//            member.setTeamId(team.getId()); // setTeam이 더 좋아보임
//            em.persist(member);
//
//            Member foundMember = em.find(Member.class, member.getId());
//
//            Long foundTeamId = foundMember.getTeamId();
//            Team fonudTeam = em.find(Team.class, foundTeamId);

            // 단방향 연관관계 매핑
//            Team team = new Team();
//            team.setName("TeamA");
//            em.persist(team);
//
//            Member member = new Member();
//            member.setUsername("member1");
//            member.setTeam(team);
//            em.persist(member);
//
//            em.flush(); // 영속성 컨텍스트 DB 반영
//            em.clear(); // 영속성 컨텍스트 비우기
//
//            System.out.println("==================================================");
//
//            // 기본 FetchType.EAGER (Join해서 다 가져옴)
//            Member foundMember = em.find(Member.class, member.getId());
//            Team foundTeam = foundMember.getTeam();
//            System.out.println(foundTeam.getName());
//
//            // Team 변경 (Member의 외래 키 업데이트)
//            Team newTeam = em.find(Team.class, 100L);
//            foundMember.setTeam(newTeam);

            // 양방향 연관관계와 연관관계의 주인 1 - 기본
//            // - 현재 단방향에서 Member -> Team (O) / Team -> Member (X)
//            // - 근데 실제 Table에서는 join을 통해서 서로 오갈 수 있게 할 수 있음
//            // - 사실상 테이블은 양방향(방향의 개념은 없긴하지만)
//            // - 문제는 객체는 그게 안됨
//            Team team = new Team();
//            team.setName("TeamA");
//            em.persist(team);
//
//            Member member = new Member();
//            member.setUsername("member1");
//            member.setTeam(team);
//            em.persist(member);
//
//            em.flush(); // 영속성 컨텍스트 DB 반영
//            em.clear(); // 영속성 컨텍스트 비우기
//
//            System.out.println("==================================================");
//
//            // 기본 FetchType.EAGER (Join해서 다 가져옴)
//            Member foundMember = em.find(Member.class, member.getId());
//            List<Member> members = foundMember.getTeam().getMembers();
//
//            for (Member m : members) {
//                System.out.println("m.getUsername() = " + m.getUsername());
//            }
//            // - 알아낸 사실: em.flush() / em.clear()를 하지 않고 실행하면 빈 리스트가 됨
//            // - 이유: 영속성 컨텍스트에는 Team의 members에 아무것도 넣지 않은 상황
//            // - 저걸 하면 디비에서 새로 긁어와서 가져오게 되지만, 안하면 영속성 컨텍스트 안에서는 team의 members에 아무것도 없음

            // 양방향 연관관계와 연관관계 주인 2 - 주의점, 정리
//            Member member = new Member();
//            member.setUsername("member1");
////            member.setTeam(team);
//            em.persist(member);
//
//            Team team = new Team();
//            team.setName("TeamA");
//            team.getMembers().add(member);
//            em.persist(team);

            Team team = new Team();
            team.setName("TeamA");
            em.persist(team);

            Member member = new Member();
            member.setUsername("member1");
//            member.changeTeam(team);
            em.persist(member);

            // 이렇게 persist를 하고 나서 하면 update 쿼리가 나감
            team.addMember(member);

            // 둘 다 넣어도 됨 -- 근데 Member가 연관관계의 주인이여서 이것만 넣어주면 알아서 됨
            // - 단, 현재 애플리케이션 메모리 상에는 team의 members에는 member가 들어가있지 않음
            // - 단, 현재는 아래 em.flush() / em.clear()를 하고 있기 때문에 문제가 없는 것 (DB에 넣는 건 문제 없음)
//            team.getMembers().add(member);

            // 양방향 매핑 관계에서의 정답은 양쪽에 값을 넣어주는 것이 맞음
//
//            em.flush();
//            em.clear();

            System.out.println("==================================================");

            // - em.flush() / em.clear()시 영속성 컨텍스트에 Team이 있기 때문에 select도 호출하지 않음
            Team foundTeam = em.find(Team.class, team.getId());
            List<Member> members = foundTeam.getMembers();

            // team.getMembers().add(member); 이거 주석하고
            if (members.isEmpty()) {
                System.out.println("EMPTY!!");
            }

            // 연관관계 편의 메서드를 작성 후에는 조회가 됨
            // - set은 사용하지 말자. Setter랑 겹침
            for (Member m : members) {
                System.out.println("m.getUsername() = " + m.getUsername());
            }

            // 양방향 연관관계에서 주의사항 - 무한루프
            // - toString(), lombok, JSON 생성 라이브러리


            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }

        emf.close();
    }
}

// 주요 용어
// - 방향: 단방향, 양방향
// - 다중성: 다대일, 일대다, 일대일, 다대다
// - 연관관계의 주인: 객체 양방향 연관관계 관리는 주인이 필요

// 일대다에서 외래 키가 있는 곳(다쪽)을 주인으로 정해라!!

// 단방향 vs 양방향 (단방향이 좋음)
// - 단방향 매핑만으로도 이미 연관관계 매핑은 완료
// - 양방향 매핑은 반대 방향으로 조회(객체 그래프 탐색) 기능이 추가된 것 뿐
// - JPQL에서 역방향으로 탐색할 일이 많음
// - 단방향 매핑을 잘하고, 양방향은 필요할 때 추가해도 됨 (테이블에 영향을 주지 않음)

// 연관관계의 주인을 정하는 기준
// - 비즈니스 로직을 기준으로 연관관계의 주인을 선택하면 안됨
// - 연관관계 주인은 외래 키의 위치를 기준으로 정해야함!!!!!
