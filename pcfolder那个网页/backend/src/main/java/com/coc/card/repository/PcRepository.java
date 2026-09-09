package com.coc.card.repository;

import com.coc.card.entity.Pc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PcRepository extends JpaRepository<Pc, Long> {

    List<Pc> findByUserIdOrderByPcNumberAsc(Long userId);

    List<Pc> findByUserIdOrderBySortOrderAscIdAsc(Long userId);

    long countByUserId(Long userId);

    /**
     * 该用户当前最大的角色卡编号，没有卡时返回 0
     */
    @Query("select coalesce(max(p.pcNumber), 0) from Pc p where p.userId = :userId")
    int maxPcNumber(@Param("userId") Long userId);

    /**
     * 该用户当前最大的手动排序序号，没有卡时返回 0
     */
    @Query("select coalesce(max(p.sortOrder), 0) from Pc p where p.userId = :userId")
    int maxSortOrder(@Param("userId") Long userId);
}
