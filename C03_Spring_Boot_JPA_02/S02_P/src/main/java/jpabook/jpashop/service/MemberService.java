package jpabook.jpashop.service;

import java.util.List;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
// - JPA의 조회 성능 최적화
// - 데이터 변경을 안 하는 곳에서는 사용하는 것이 좋음
@RequiredArgsConstructor
public class MemberService {

    // 의존성 주입은 생성자 주입을 사용하는 것이 가장 좋음!!
    private final MemberRepository memberRepository;

    /**
     * 회원 가입
     */
    @Transactional
    // - 데이터 변경이 일어나므로 readOnly = true를 사용하면 안 됨 (데이터 변경이 일어나지 않음)
    public Long join(Member member) {
        // 중복 회원 검증
        validateDuplicateMember(member);

        // 회원 저장
        memberRepository.save(member);
        // - 영속성 컨텍스트에 들어갈 때, Key가 필요한데 그 때의 Key가 id
        // - 따라서 실제로 저장이 되지 않아도 id를 넣어서 id를 조회할 수 있음
        return member.getId();
    }

    /**
     * 회원 전체 조회
     */
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    /**
     * 회원 단건 조회
     */
    public Member findOne(Long memberId) {
        return memberRepository.findOne(memberId);
    }

    private void validateDuplicateMember(Member member) {
        // 발생할 수 있는 문제점
        // - 동시성 문제
        // - 같은 이름으로 동시에 여기에 접근한다면 문제가 발생할 수 있음
        // - DB에 unique를 걸어주는 것이 좋음 (최후의 방어선)
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if (!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    /**
     * 정보 수정 (이름만)
     */
    @Transactional
    public void update(Long id, String name) {
        Member member = memberRepository.findOne(id);
        member.setName(name);
    }
}
