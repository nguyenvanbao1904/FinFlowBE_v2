package com.finflow.backend.investment.market_data.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Cây phân loại ngành (độ sâu khác nhau theo nhánh: BĐS 2 cấp, Bán lẻ 3+ cấp, …).
 * <p>
 * Nguồn điền: FireAnt {@code GET /icb} (industryCode, level, name) + suy luận {@code parent}
 * theo prefix mã + cấp. Công ty gán vào <strong>nút lá</strong> (thường trùng mã ICB cụ thể).
 */
@Entity
@Table(name = "industry_nodes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndustryNode {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private IndustryNode parent;

    /** 1 = nhóm rộng nhất (vd. Bán lẻ, Bất động sản). */
    @Column(name = "level", nullable = false)
    private Integer level;

    @Column(name = "name_vi", nullable = false, length = 500)
    private String nameVi;

    /**
     * Mã ICB theo nguồn (FireAnt). Trùng với mã trên hồ sơ công ty → tra nút lá.
     */
    @Column(name = "icb_code", length = 32, unique = true)
    private String icbCode;

    /** Mô tả chi tiết (vd. businessAreas) — chỉ nút lá, optional. */
    @Column(name = "detail_label", length = 2000)
    private String detailLabel;
}
