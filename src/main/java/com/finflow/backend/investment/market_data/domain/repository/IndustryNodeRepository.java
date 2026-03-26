package com.finflow.backend.investment.market_data.domain.repository;

import com.finflow.backend.investment.market_data.domain.entity.IndustryNode;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IndustryNodeRepository extends JpaRepository<IndustryNode, UUID> {

    Optional<IndustryNode> findByIcbCode(String icbCode);

    /** Mã công ty là mã nút hoặc mã chi tiết hơn: chọn nút có {@code icb_code} dài nhất mà vẫn là prefix của mã gửi lên. */
    @Query(
            "SELECT n FROM IndustryNode n WHERE n.icbCode IS NOT NULL AND :code LIKE CONCAT(n.icbCode, '%') "
                    + "ORDER BY LENGTH(n.icbCode) DESC")
    List<IndustryNode> findDeepestWhereCodeStartsWithStoredPrefix(
            @Param("code") String code, Pageable pageable);

    /**
     * Mã công ty ngắn hơn nút lá: chọn nút có {@code icb_code} dài nhất mà bắt đầu bằng {@code code}
     * (tránh match khi {@code code} quá ngắn — gọi từ service với điều kiện độ dài).
     */
    @Query(
            "SELECT n FROM IndustryNode n WHERE n.icbCode IS NOT NULL AND n.icbCode LIKE CONCAT(:code, '%') "
                    + "ORDER BY LENGTH(n.icbCode) DESC")
    List<IndustryNode> findDeepestWhereStoredStartsWithCode(
            @Param("code") String code, Pageable pageable);
}
