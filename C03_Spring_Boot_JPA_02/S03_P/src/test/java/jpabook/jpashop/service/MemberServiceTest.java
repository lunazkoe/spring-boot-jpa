package jpabook.jpashop.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Autowired EntityManager em;

    @Test
//    @Rollback(false) // insert를 보고 싶으면 활성화
    // - 트랜잭션이 커밋을 하는 순간 영속성 컨텍스트에 있는 것을 DB에 실제로 저장함 (em.flush())
    // - 근데 Test에서의 @Transactional은 롤백을 해버리기 때문에 save 시 insert가 나가지 않음
    public void 회원가입() throws Exception {
        // given
        Member member = new Member();
        member.setName("kim");

        // when
        Long savedId = memberService.join(member);
        em.flush();
//        em.clear();
        // - 이걸 하면 같은 영속성 컨텍스트에서 가져오는 것이 아니라 참조값이 바뀌어서 then이 통과할 수 없음
        // - @Transactional에 의해서 같은 영속성 컨텍스트를 사용하기 때문에 then이 통과할 수 있는 것임
        // - flush()만 하면 DB에 내용 반영이여서 then을 통과함

        // then
        assertThat(member).isEqualTo(memberRepository.findOne(savedId));
    }

    @Test
    public void 중복_회원_예외() throws Exception {
        // given
        Member memberA = new Member();
        memberA.setName("kim1");

        Member memberB = new Member();
        memberB.setName("kim1");

        // when & then
        memberService.join(memberA);
        assertThatThrownBy(() -> memberService.join(memberB))
            .isInstanceOf(IllegalStateException.class);

        // then
//        fail("예외가 발생해야 한다.");
        // - 이걸 사용하는 경우: 여기를 지나가면 안되는 경우에 사용함 (오면 테스트 fail)
    }
}
