package com.finflow.backend.finance.dashboard.application.usecase;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.finance.dashboard.application.dto.HomeInsightOutput;
import com.finflow.backend.finance.dashboard.application.dto.HomeInsightSnapshot;
import com.finflow.backend.finance.dashboard.application.port.in.GenerateHomeInsightPort;
import com.finflow.backend.finance.dashboard.application.port.out.DataAiHomeInsightPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenerateHomeInsightUseCase implements GenerateHomeInsightPort {

    private final DataAiHomeInsightPort dataAiHomeInsightPort;

    @Override
    public HomeInsightOutput execute(HomeInsightSnapshot snapshot) {
        try {
            HomeInsightOutput output = dataAiHomeInsightPort.generate(toPayload(snapshot));
            if (isBlank(output.message())) {
                return fallback(snapshot, "empty_message");
            }
            return output;
        } catch (AppException exception) {
            log.warn("Home insight upstream unavailable for user={}", snapshot.userId());
            return fallback(snapshot, "upstream_unavailable");
        }
    }

    private Map<String, Object> toPayload(HomeInsightSnapshot snapshot) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("locale", defaultString(snapshot.locale(), "vi-VN"));
        payload.put("timezone", defaultString(snapshot.timezone(), "Asia/Ho_Chi_Minh"));
        payload.put("currency", defaultString(snapshot.currency(), "VND"));
        payload.put("netWorth", snapshot.netWorth());
        payload.put("liquidAssets", snapshot.liquidAssets());
        payload.put("debtTotal", snapshot.debtTotal());
        payload.put("investmentAssets", snapshot.investmentAssets());
        payload.put("totalBalance", snapshot.totalBalance());
        payload.put("totalIncome", snapshot.totalIncome());
        payload.put("totalExpense", snapshot.totalExpense());
        payload.put("budgetTargetTotal", snapshot.budgetTargetTotal());
        payload.put("budgetSpentTotal", snapshot.budgetSpentTotal());
        payload.put("portfolioCount", snapshot.portfolioCount());
        payload.put("portfolioCashTotal", snapshot.portfolioCashTotal());
        payload.put("primaryPortfolioName", snapshot.primaryPortfolioName());
        payload.put("investmentTotalValue", snapshot.investmentTotalValue());
        return payload;
    }

    private HomeInsightOutput fallback(HomeInsightSnapshot snapshot, String warning) {
        double monthlyCashflow = snapshot.totalIncome() - snapshot.totalExpense();
        String message;
        if (snapshot.portfolioCashTotal() > 0) {
            message = "Tiền mặt trong danh mục còn %s; hãy lên kế hoạch giải ngân."
                    .formatted(compactVnd(snapshot.portfolioCashTotal()));
        } else if (monthlyCashflow > 0) {
            message = "Dòng tiền tháng dương %s; hãy phân bổ vào quỹ dự phòng hoặc đầu tư."
                    .formatted(compactVnd(monthlyCashflow));
        } else {
            message = "Thanh khoản đang ở %s; hãy giữ đủ quỹ dự phòng."
                    .formatted(compactVnd(snapshot.liquidAssets()));
        }
        return new HomeInsightOutput("Gợi ý hôm nay", shorten(message, 120), List.of(warning), false);
    }

    private String compactVnd(double value) {
        double absValue = Math.abs(value);
        if (absValue >= 1_000_000_000) {
            return String.format("%.1f tỷ", value / 1_000_000_000);
        }
        if (absValue >= 1_000_000) {
            return String.format("%.0f triệu", value / 1_000_000);
        }
        return String.format("%,.0f đ", value).replace(",", ".");
    }

    private String shorten(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        int boundary = text.lastIndexOf(' ', maxLength);
        String prefix = boundary > 0 ? text.substring(0, boundary) : text.substring(0, maxLength);
        return prefix.replaceAll("[ .,;:]+$", "") + ".";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String defaultString(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }
}
