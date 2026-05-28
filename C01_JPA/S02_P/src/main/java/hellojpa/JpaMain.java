package hellojpa;

import hellojpa.domain.Member;
import jakarta.persistence.*;

public class JpaMain {

    public static void main(String[] args) {

        // JPA 기본 동작 개요
        // - Persistence에서 EntityManagerFactory를 생성
        // - EntityManagerFactory에서 EntityManager를 생성
        // - EntityManager를 사용
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        // 반드시 트랜잭션 안에서 실행되어야 함
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        //code
        try {
            Member member = em.find(Member.class, 1L);
            member.setName("HelloA_Update");

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            // 반드시 닫아줘야함
            em.close();
        }

        emf.close();
    }

    // 엔티티 매니저 팩토리는 하나만 생성하고 애플리케이션 전체에서 공유
    // 엔티티 매니저는 쓰레드간에 공유 X(사용하고 버려야 함)
    // JPA의 모든 데이터 변경은 트랜잭션 안에서 실행해야함
}
