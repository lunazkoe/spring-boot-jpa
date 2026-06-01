package hellojpa;

import hellojpa.domain.Member;
import hellojpa.domain.Order;
import jakarta.persistence.*;

public class JpaMain {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {

            // 아래는 객체지향스럽지 않음
            // - 객체 설계를 테이블 설계에 맞춘 방식
            Order order = em.find(Order.class, 1L);
            Long memberId = order.getMemberId();
            Member member = em.find(Member.class, memberId);

            // 아래가 더 객체지향스러움
//            Order order = em.find(Order.class, 1L);
//            order.getMember();

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }

        emf.close();
    }
}
