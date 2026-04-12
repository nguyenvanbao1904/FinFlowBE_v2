package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.application.port.in.GetIndustryNodesPort;
import com.finflow.backend.investment.market_data.application.service.MarketDataReadService;
import com.finflow.backend.investment.market_data.domain.entity.IndustryNode;
import com.finflow.backend.investment.market_data.presentation.response.IndustryNodeReadResponse;
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
    public List<IndustryNodeReadResponse> execute() {
        return readService.loadIndustryNodes().stream()
                .map(GetIndustryNodesUseCase::toResponse)
                .toList();
    }

    private static IndustryNodeReadResponse toResponse(IndustryNode node) {
        return new IndustryNodeReadResponse(
                node.getId(),
                node.getParent() == null ? null : node.getParent().getId(),
                node.getLevel(),
                node.getNameVi(),
                node.getIcbCode(),
                node.getDetailLabel()
        );
    }
}
