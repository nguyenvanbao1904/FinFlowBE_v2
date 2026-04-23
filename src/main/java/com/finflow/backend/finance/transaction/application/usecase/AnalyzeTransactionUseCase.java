package com.finflow.backend.finance.transaction.application.usecase;

import com.finflow.backend.finance.transaction.application.query.AnalyzeTransactionQuery;
import com.finflow.backend.finance.transaction.application.dto.AnalyzeTransactionOutput;
import com.finflow.backend.finance.transaction.application.port.in.AnalyzeTransactionPort;
import com.finflow.backend.finance.transaction.application.port.out.AnalyzeTransactionWithAiPort;
import com.finflow.backend.finance.transaction.application.result.TransactionPrefillResult;
import com.finflow.backend.finance.transaction.application.service.AnalyzeTransactionHelper;
import com.finflow.backend.finance.transaction.application.service.AnalyzeTransactionLoader;
import com.finflow.backend.finance.transaction.application.service.AnalyzeTransactionLoader.DbSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Analyzes free-text input and returns a pre-filled transaction suggestion.
 *
 * <p>DB reads are performed by {@link AnalyzeTransactionLoader} (a separate Spring bean)
 * so that its {@code @Transactional(readOnly=true)} is properly intercepted by the
 * CGLIB proxy — avoiding the self-invocation bypass that would occur if the loading
 * were done via {@code this.loadDbData()} inside this class.
 *
 * <p>The AI HTTP call ({@link AnalyzeTransactionWithAiPort#analyze}) is intentionally
 * outside any transaction boundary so that no DB connection is held during the
 * (potentially slow) AI round-trip.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AnalyzeTransactionUseCase implements AnalyzeTransactionPort {

    private final AnalyzeTransactionLoader loader;
    private final AnalyzeTransactionWithAiPort analyzeTransactionWithAiPort;
    private final AnalyzeTransactionHelper helper;

    @Override
    public AnalyzeTransactionOutput execute(AnalyzeTransactionQuery request) {
        String userId = request.userId();
        String text = request.text();
        log.debug("Analyzing transaction text for userId: '{}' text: '{}'", userId, text);

        // Phase 1: load DB data through a properly proxied bean — @Transactional(readOnly=true) is honoured
        DbSnapshot snapshot = loader.load(userId);

        // Phase 2: call AI service (no transaction — DB connection already released)
        try {
            List<Map<String, ?>> categoryPayload = helper.buildCategoryPayload(snapshot.categories());
            List<Map<String, ?>> accountPayload = helper.buildAccountPayload(snapshot.accounts());
            List<Map<String, ?>> historyPayload = helper.buildHistoryPayload(snapshot.recentTransactions());

            TransactionPrefillResult result = analyzeTransactionWithAiPort.analyze(
                    text, categoryPayload, accountPayload, historyPayload);
            return helper.mapPrefillResultToResponse(result, snapshot.categories(), snapshot.accounts());
        } catch (Exception e) {
            log.warn("AI prefill failed, fallback to local heuristic. reason={}", e.getMessage());
            return helper.fallbackResponse(snapshot.categories());
        }
    }
}
