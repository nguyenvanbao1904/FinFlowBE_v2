package com.finflow.backend.investment.portfolio.presentation.response;

import java.util.List;

/**
 * Response DTO cho endpoint GET /portfolios/{id}/health.
 *
 * @param latestYear    Năm của quý dữ liệu indicator gần nhất có trong DB.
 * @param latestQuarter Quý của dữ liệu indicator gần nhất.
 * @param current       Snapshot theo giá close hiện tại (market weight).
 * @param history       Chuỗi quý lịch sử (cost weight, harmonic mean).
 */
public record PortfolioHealthResponse(
        int latestYear,
        int latestQuarter,
        CurrentSnapshot current,
        List<HistoryPoint> history
) {

    /**
     * Snapshot tức thì — dùng market weight = qty × closePrice.
     *
     * @param priceType "CLOSE" | "INSUFFICIENT"
     */
    public record CurrentSnapshot(
            double totalValueClose,
            double stockValueClose,
            double cashBalance,
            Double pe,
            Double pb,
            Double ps,
            String priceType
    ) {}

    /**
     * Một điểm lịch sử theo quý — dùng cost weight = qty × avgPrice.
     * Metric = null khi coverage < 50% (chart phải vẽ đứt nét).
     *
     * @param coverage Tỷ lệ trọng số của các mã có dữ liệu hợp lệ (0.0–1.0).
     */
    public record HistoryPoint(
            int year,
            int quarter,
            Double pe,
            Double pb,
            Double ps,
            Double roe,
            Double roa,
            double coverage
    ) {}
}
