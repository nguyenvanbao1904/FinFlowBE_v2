package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.domain.entity.Company;
import com.finflow.backend.investment.market_data.domain.entity.FinancialIndicator;
import com.finflow.backend.investment.market_data.domain.entity.BankIncomeStatement;
import com.finflow.backend.investment.market_data.domain.entity.NonBankIncomeStatement;
import com.finflow.backend.investment.market_data.presentation.response.InvestmentAnalysisResponse;
import com.finflow.backend.investment.portfolio.infrastructure.VpsMarketPriceClient;
import com.finflow.backend.investment.portfolio.infrastructure.VpsMarketPriceClient.MarketPriceQuote;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.finflow.backend.investment.market_data.application.usecase.InvestmentAnalysisNumberUtils.toDouble;

/**
 * Overview BCTC + bội số định giá theo chỉ số lưu trong DB; đồng thời tính thêm PE/PB/PS theo giá VPS gần nhất
 * (close/last) và cơ bản tài chính (EPS TTM, BVPS; P/S: DTT thuần TTM hoặc NH = NII+phí+khác TTM, trên CP).
 */
@Slf4j
@Component
@RequiredArgsConstructor
class InvestmentAnalysisOverviewBuilder {

    private final VpsMarketPriceClient vpsMarketPriceClient;
    private final InvestmentAnalysisRepositoryLoader repositoryLoader;

    InvestmentAnalysisResponse.Overview build(Company company, List<FinancialIndicator> indicators) {
        FinancialIndicator latest = selectOverviewIndicator(indicators);
        String industryLabel = company.getIndustryNode() == null || company.getIndustryNode().getNameVi() == null
                ? ""
                : company.getIndustryNode().getNameVi();
        String icbCode = company.getIndustryNode() == null ? null : company.getIndustryNode().getIcbCode();

        Double epsTtm = InvestmentFinancialUtils.computeEpsTtm(indicators);
        Double bvps = latest == null ? null : toDouble(latest.getBvps());
        Double cplhRaw = latest == null ? null : toDouble(latest.getCplh());

        LiveMultiples live = computeLiveMultiples(company, epsTtm, bvps, cplhRaw);

        return new InvestmentAnalysisResponse.Overview(
                company.getId(),
                company.getCompanyName(),
                company.getExchange(),
                company.getCompanyType(),
                icbCode,
                industryLabel,
                company.getDescription(),
                latest == null ? null : toDouble(latest.getRoe()),
                latest == null ? null : toDouble(latest.getRoa()),
                epsTtm,
                bvps,
                cplhRaw,
                latest == null ? null : toDouble(latest.getPe()),
                InvestmentFinancialUtils.median(indicators.stream().map(FinancialIndicator::getPe).toList()),
                InvestmentFinancialUtils.mean(indicators.stream().map(FinancialIndicator::getPe).toList()),
                latest == null ? null : toDouble(latest.getPb()),
                InvestmentFinancialUtils.median(indicators.stream().map(FinancialIndicator::getPb).toList()),
                InvestmentFinancialUtils.mean(indicators.stream().map(FinancialIndicator::getPb).toList()),
                latest == null ? null : toDouble(latest.getPs()),
                InvestmentFinancialUtils.median(indicators.stream().map(FinancialIndicator::getPs).toList()),
                InvestmentFinancialUtils.mean(indicators.stream().map(FinancialIndicator::getPs).toList()),
                live.livePe(),
                live.livePb(),
                live.livePs(),
                live.livePriceVnd(),
                live.livePriceSource()
        );
    }

    private LiveMultiples computeLiveMultiples(
            Company company,
            Double epsTtm,
            Double bvps,
            Double cplhRaw
    ) {
        try {
            Map<String, MarketPriceQuote> quotes = vpsMarketPriceClient.getClosePrices(List.of(company.getId()));
            MarketPriceQuote quote = quotes.get(company.getId());
            if (quote == null) {
                return LiveMultiples.empty();
            }
            double price = quote.priceVnd();
            String source = quote.source().name();

            Double livePe = null;
            if (epsTtm != null && epsTtm > 0) {
                livePe = price / epsTtm;
            }

            Double livePb = null;
            if (bvps != null && bvps > 0) {
                livePb = price / bvps;
            }

            Double livePs = null;
            String type = Optional.ofNullable(company.getCompanyType()).orElse("").trim();
            if (cplhRaw != null) {
                double shares = InvestmentFinancialUtils.absoluteSharesFromCplh(cplhRaw);
                if (shares > 0 && !Double.isNaN(shares)) {
                    Double ttmBase = null;
                    if ("BANK".equalsIgnoreCase(type)) {
                        List<BankIncomeStatement> bankInc = repositoryLoader.loadBankIncomesLastQuarters(company.getId(), 4);
                        ttmBase = InvestmentFinancialUtils.computeBankTopLineTtm(bankInc);
                    } else {
                        List<NonBankIncomeStatement> incomes = repositoryLoader.loadNonBankIncomesLastQuarters(company.getId(), 4);
                        ttmBase = InvestmentFinancialUtils.computeNetRevenueTtm(incomes);
                    }
                    if (ttmBase != null && ttmBase > 0) {
                        double perShare = ttmBase / shares;
                        if (perShare > 0) {
                            livePs = price / perShare;
                        }
                    }
                }
            }

            return new LiveMultiples(livePe, livePb, livePs, price, source);
        } catch (Exception e) {
            log.debug("Live multiples unavailable for {}: {}", company.getId(), e.getMessage());
            return LiveMultiples.empty();
        }
    }

    private static FinancialIndicator selectOverviewIndicator(List<FinancialIndicator> indicators) {
        if (indicators == null || indicators.isEmpty()) return null;
        Optional<FinancialIndicator> latestQ4 = indicators.stream()
                .filter(i -> i.getQuarter() == 4)
                .max(Comparator.comparingInt(FinancialIndicator::getYear));
        if (latestQ4.isPresent()) return latestQ4.get();
        return indicators.stream()
                .max(
                        Comparator.comparingInt(FinancialIndicator::getYear)
                                .thenComparingInt(FinancialIndicator::getQuarter)
                )
                .orElse(null);
    }

    private record LiveMultiples(
            Double livePe,
            Double livePb,
            Double livePs,
            Double livePriceVnd,
            String livePriceSource
    ) {
        static LiveMultiples empty() {
            return new LiveMultiples(null, null, null, null, null);
        }
    }
}
