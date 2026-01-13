package com.gymapp.backend.services;

import com.gymapp.backend.dtos.requests.CreateMemberRequest;
import com.gymapp.backend.dtos.requests.UpdateMemberRequest;
import com.gymapp.backend.dtos.responses.MemberResponse;
import com.gymapp.backend.exceptions.DuplicateResourceException;
import com.gymapp.backend.exceptions.ResourceNotFoundException;
import com.gymapp.backend.mappers.MemberMapper;
import com.gymapp.backend.entities.Member;
import com.gymapp.backend.repositories.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;

    @Transactional
    public MemberResponse createMember(CreateMemberRequest request) {
        log.info("Creating new member with email: {}", request.getEmail());

        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Member", "email", request.getEmail());
        }

        Member member = memberMapper.toEntity(request);
        Member savedMember = memberRepository.save(member);

        log.info("Member created successfully with ID: {}", savedMember.getId());
        return memberMapper.toResponse(savedMember);
    }

    @Transactional(readOnly = true)
    public MemberResponse getMemberById(UUID id) {
        log.debug("Fetching member with ID: {}", id);
        Member member = findMemberById(id);
        return memberMapper.toResponse(member);
    }

    @Transactional(readOnly = true)
    public MemberResponse getMemberByEmail(String email) {
        log.debug("Fetching member with email: {}", email);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "email", email));
        return memberMapper.toResponse(member);
    }

    @Transactional(readOnly = true)
    public Page<MemberResponse> getAllMembers(Pageable pageable) {
        log.debug("Fetching all members with pagination");
        return memberRepository.findByActiveTrue(pageable)
                .map(memberMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<MemberResponse> searchMembers(String search, Pageable pageable) {
        log.debug("Searching members with query: {}", search);
        return memberRepository.searchMembers(search, pageable)
                .map(memberMapper::toResponse);
    }

    @Transactional
    public MemberResponse updateMember(UUID id, UpdateMemberRequest request) {
        log.info("Updating member with ID: {}", id);

        Member member = findMemberById(id);

        if (request.getEmail() != null && !request.getEmail().equals(member.getEmail())) {
            if (memberRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Member", "email", request.getEmail());
            }
        }

        memberMapper.updateEntity(request, member);
        Member updatedMember = memberRepository.save(member);

        log.info("Member updated successfully with ID: {}", updatedMember.getId());
        return memberMapper.toResponse(updatedMember);
    }

    @Transactional
    public void deleteMember(UUID id) {
        log.info("Deleting member with ID: {}", id);
        Member member = findMemberById(id);
        member.setActive(false);
        memberRepository.save(member);
        log.info("Member deactivated successfully with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public Member findMemberById(UUID id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "id", id));
    }

    @Transactional(readOnly = true)
    public Member findMemberWithSubscription(UUID id) {
        return memberRepository.findByIdWithSubscription(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "id", id));
    }
}
