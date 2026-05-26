package jpabook.jpashop;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;

    @Test
    @Transactional // em으로 할 때는 항상 트랜잭션 안에서 실행되어야함
    @Rollback(false)
    void testMember() throws Exception {
        // given
        Member member = new Member();
        member.setUsername("memberA");

        // when
        Long savedId = memberRepository.save(member);
        Member findMember = memberRepository.find(savedId);

        // then
        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo("memberA");
        assertThat(findMember).isEqualTo(member);
        // - 같은 트랜잭션 안에서 저장 조회 시 영속성 컨텍스트가 같음
        // - 영속성 컨텍스트에서 같은 ID를 가지고 있으면 같은 엔티티로 봄
    }
}