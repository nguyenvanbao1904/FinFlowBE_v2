package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.application.command.SyncIndustryNodesCommand;
import com.finflow.backend.investment.market_data.application.port.in.SyncIndustryNodesPort;
import com.finflow.backend.investment.market_data.domain.entity.IndustryNode;
import com.finflow.backend.investment.market_data.domain.repository.IndustryNodeRepository;
import com.finflow.backend.investment.market_data.application.dto.IndustryNodeRequestInput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncIndustryNodesUseCase implements SyncIndustryNodesPort {

    private final IndustryNodeRepository industryNodeRepository;

    @Transactional
    @Override
    public void execute(SyncIndustryNodesCommand command) {
        List<IndustryNodeRequestInput> requests = command.request();
        log.info("Syncing {} industry tree nodes...", requests.size());
        List<IndustryNodeRequestInput> ordered = requests.stream()
                .sorted(Comparator.comparing(IndustryNodeRequestInput::level)
                        .thenComparing(d -> d.icbCode() != null ? d.icbCode() : ""))
                .toList();

        // --- single bulk fetch for all node IDs ---
        List<UUID> ids = ordered.stream().map(IndustryNodeRequestInput::id).toList();
        Map<UUID, IndustryNode> existingById = industryNodeRepository.findAllById(ids)
                .stream()
                .collect(Collectors.toMap(IndustryNode::getId, Function.identity()));

        List<IndustryNode> toSave = new ArrayList<>(ordered.size());
        for (IndustryNodeRequestInput dto : ordered) {
            IndustryNode node = existingById.getOrDefault(dto.id(), new IndustryNode());
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
            toSave.add(node);
        }

        industryNodeRepository.saveAll(toSave);
        log.info("Successfully synced {} industry nodes", toSave.size());
    }

    private static String emptyToNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s.trim();
    }
}
