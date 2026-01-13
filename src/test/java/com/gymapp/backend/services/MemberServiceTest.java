package com.gymapp.backend.services;

import com.gymapp.backend.dtos.requests.CreateMemberRequest;
import com.gymapp.backend.dtos.requests.UpdateMemberRequest;
import com.gymapp.backend.dtos.responses.MemberResponse;
import com.gymapp.backend.exceptions.DuplicateResourceException;
import com.gymapp.backend.exceptions.ResourceNotFoundException;
import com.gymapp.backend.mappers.MemberMapper;
import com.gymapp.backend.entities.Member;
import com.gymapp.backend.repositories.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberMapper memberMapper;

    @InjectMocks
    private MemberService memberService;

    private Member member;
    private MemberResponse memberResponse;
    private CreateMemberRequest createRequest;
    private UUID memberId;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();

        member = Member.builder()
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .phone("+1234567890")
            .dateOfBirth(LocalDate.of(1990, 1, 15))
            .active(true)
            .build();

        member.setId(memberId);
        member.setCreatedAt(LocalDateTime.now());
        member.setUpdatedAt(LocalDateTime.now());

        memberResponse = MemberResponse.builder()
                .id(memberId)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 15))
                .active(true)
                .build();

        createRequest = CreateMemberRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 15))
                .build();
    }

    @Test
    @DisplayName("Should create member successfully")
    void createMember_Success() {
        when(memberRepository.existsByEmail(createRequest.getEmail())).thenReturn(false);
        when(memberMapper.toEntity(createRequest)).thenReturn(member);
        when(memberRepository.save(any(Member.class))).thenReturn(member);
        when(memberMapper.toResponse(member)).thenReturn(memberResponse);

        MemberResponse result = memberService.createMember(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(createRequest.getEmail());
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("Should throw exception when creating member with existing email")
    void createMember_DuplicateEmail() {
        when(memberRepository.existsByEmail(createRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> memberService.createMember(createRequest))
                .isInstanceOf(DuplicateResourceException.class);

        verify(memberRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get member by ID successfully")
    void getMemberById_Success() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(memberMapper.toResponse(member)).thenReturn(memberResponse);

        MemberResponse result = memberService.getMemberById(memberId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(memberId);
    }

    @Test
    @DisplayName("Should throw exception when member not found by ID")
    void getMemberById_NotFound() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.getMemberById(memberId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should get all members with pagination")
    void getAllMembers_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Member> memberPage = new PageImpl<>(List.of(member));

        when(memberRepository.findByActiveTrue(pageable)).thenReturn(memberPage);
        when(memberMapper.toResponse(member)).thenReturn(memberResponse);

        Page<MemberResponse> result = memberService.getAllMembers(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo(member.getEmail());
    }

    @Test
    @DisplayName("Should search members successfully")
    void searchMembers_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Member> memberPage = new PageImpl<>(List.of(member));
        String search = "John";

        when(memberRepository.searchMembers(search, pageable)).thenReturn(memberPage);
        when(memberMapper.toResponse(member)).thenReturn(memberResponse);

        Page<MemberResponse> result = memberService.searchMembers(search, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should update member successfully")
    void updateMember_Success() {
        UpdateMemberRequest updateRequest = UpdateMemberRequest.builder()
                .firstName("Jane")
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class))).thenReturn(member);
        when(memberMapper.toResponse(member)).thenReturn(memberResponse);

        MemberResponse result = memberService.updateMember(memberId, updateRequest);

        assertThat(result).isNotNull();
        verify(memberMapper).updateEntity(updateRequest, member);
        verify(memberRepository).save(member);
    }

    @Test
    @DisplayName("Should throw exception when updating with duplicate email")
    void updateMember_DuplicateEmail() {
        UpdateMemberRequest updateRequest = UpdateMemberRequest.builder()
                .email("existing@example.com")
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(memberRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> memberService.updateMember(memberId, updateRequest))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("Should soft delete member successfully")
    void deleteMember_Success() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        memberService.deleteMember(memberId);

        assertThat(member.isActive()).isFalse();
        verify(memberRepository).save(member);
    }
}
