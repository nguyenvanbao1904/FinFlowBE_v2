package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.application.port.in.GetIndustryNodesPort;
import com.finflow.backend.investment.market_data.application.query.GetIndustryNodesQuery;
import com.finflow.backend.investment.market_data.application.service.MarketDataReadService;
import com.finflow.backend.investment.market_data.domain.entity.IndustryNode;
import com.finflow.backend.investment.market_data.application.dto.IndustryNodeReadOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetIndustryNodesUseCase implements GetIndustryNodesPort {

    private final MarketDataReadService readService;

    @Transactional(readOnly = true)
    @Override
    public List<IndustryNodeReadOutput> execute(GetIndustryNodesQuery request) {
        return readService.loadIndustryNodes().stream()
                .map(GetIndustryNodesUseCase::toResponse)
                .toList();
    }

    private static IndustryNodeReadOutput toResponse(IndustryNode node) {
        return new IndustryNodeReadOutput(
                node.getId(),
                node.getParent() == null ? null : node.getParent().getId(),
                node.getLevel(),
                node.getNameVi(),
                node.getIcbCode(),
                node.getDetailLabel()
        );
    }
}
