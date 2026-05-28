package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import java.util.List;
import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    public void save(Item item) {
        if (item.getId() == null) {
            em.persist(item); // 생성용
        } else {
            Item merge = em.merge(item);// 수정용
            // - 준영속 상태를 영속 상태로 가져옴
            // - 즉, item의 id를 기반으로 DB에서 가져와서 영속 상태로 만듦
            // - 그, 이후 item의 내용을 영속 상태의 item으로 있는 내용으로 다 변경함
            // - merge가 영속성 컨텍스트에 의해서 관리되는 객체, item은 준영속 상태

            // - 주의점
            //      - 변경 감지 기능은 원하는 속성만 선택해서 변경이 가능함
            //      - 병합을 사용하면 모든 속성이 변경됨
            //      - 병합 시 값이 없으면 null로 들어감
            //      - **변경 감지를 사용하자**
        }
    }

    public Item findOne(Long id) {
        return em.find(Item.class, id);
    }

    public List<Item> findAll() {
        return em.createQuery("select i from Item i", Item.class)
            .getResultList();
    }
}
