package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.domain.entity.IndustryNode;
import com.finflow.backend.investment.market_data.domain.repository.IndustryNodeRepository;
import com.finflow.backend.investment.market_data.presentation.request.IndustryNodeRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncIndustryNodesUseCase {

    private final IndustryNodeRepository industryNodeRepository;

    @Transactional
    public void execute(List<IndustryNodeRequestDTO> requests) {
        log.info("Syncing {} industry tree nodes...", requests.size());
        List<IndustryNodeRequestDTO> ordered = requests.stream()
                .sorted(Comparator.comparing(IndustryNodeRequestDTO::level)
                        .thenComparing(d -> d.icbCode() != null ? d.icbCode() : ""))
                .toList();

        for (IndustryNodeRequestDTO dto : ordered) {
            IndustryNode node = industryNodeRepository.findById(dto.id()).orElseGet(IndustryNode::new);
            node.setId(dto.id());
            node.setLevel(dto.level());
            node.setNameVi(dto.nameVi());
            node.setIcbCode(emptyToNull(dto.icbCode()));
            node.setDetailLabel(emptyToNull(dto.detailLabel()));
            if (dto.parentId() != null) {
                node.setParent(industryNodeRepository.getReferenceById(dto.parentId()));
            } else {
                node.setParent(null);
            }
            industryNodeRepository.save(node);
        }
        log.info("Successfully synced {} industry nodes", ordered.size());
    }

    private static String emptyToNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s.trim();
    }
}
