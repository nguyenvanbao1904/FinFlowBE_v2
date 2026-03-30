package com.finflow.backend.investment.portfolio.infrastructure.scheduler;

import com.finflow.backend.investment.portfolio.application.service.DailyPortfolioSnapshotRetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailyPortfolioSnapshotScheduler {

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final DailyPortfolioSnapshotRetryService dailyPortfolioSnapshotRetryService;

    /** Sau giờ đóng cửa (VN); retry/backoff xử lý khi VNINDEX chưa có. */
    @Scheduled(cron = "0 5 16 * * ?", zone = "Asia/Ho_Chi_Minh")
    public void recordEndOfDaySnapshots() {
        LocalDate today = LocalDate.now(VN_ZONE);
        log.info("Triggering daily portfolio + VNINDEX snapshot job for {}...", today);
        try {
            dailyPortfolioSnapshotRetryService.recordForTradingDay(today);
        } catch (Exception e) {
            log.error("Daily portfolio snapshot job failed", e);
        }
    }
}
