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

            Movie movie = new Movie();
            movie.setDirector("aaaa");
            movie.setActor("bbbb");
            movie.setName("바람과함께사라지다.");
            movie.setPrice(10000);
            em.persist(movie);

            em.flush();
            em.clear();

            System.out.println("================================");
            Movie foundMovie = em.find(Movie.class, movie.getId());


            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }

        emf.close();
    }
}

// 구현 클래스마다 테이블 전략을 사용하면
// - 장점
//      - 서브 타입을 명확하게 구분해서 처리할 때 효과적
//      - not null 제약조건 사용 가능
// - 단점
//      - 여러 자식 테이블을 함께 조회할 때 성능이 느림(UNION SQL)
//      - 자식 테이블을 통합해서 쿼리하기 어려움

// - 기본은 조인 전략 -> 단일 테이블 전략 -> 구현클래스마다 테이블 전략

// @MappedSuperclass
// - 상속 관계가 아닌
// - 공통 매핑 정보가 필요할 때 사용
//      - 등록일 수정일

// - 참고
// - @Entity 클래스는 엔티티나 @MappedSuperclass로 지정한 클래스만 상속 가능