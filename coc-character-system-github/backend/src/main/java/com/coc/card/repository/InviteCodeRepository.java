package com.coc.card.repository;

import com.coc.card.entity.InviteCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InviteCodeRepository extends JpaRepository<InviteCode, Long> {

    Optional<InviteCode> findByCode(String code);

    List<InviteCode> findAllByOrderByCreatedAtDesc();

    List<InviteCode> findByCreatedBy(Long createdBy);

    boolean existsByCode(String code);
}
