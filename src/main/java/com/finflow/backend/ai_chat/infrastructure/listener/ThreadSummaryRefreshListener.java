package com.finflow.backend.ai_chat.infrastructure.listener;

import com.finflow.backend.ai_chat.application.event.ThreadSummaryRefreshRequestedEvent;
import com.finflow.backend.ai_chat.application.command.RefreshThreadSummaryCommand;
import com.finflow.backend.ai_chat.application.port.in.RefreshThreadSummaryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ThreadSummaryRefreshListener {

    private final RefreshThreadSummaryPort refreshThreadSummaryPort;

    @Async
    @EventListener
    public void handle(ThreadSummaryRefreshRequestedEvent event) {
        try {
            refreshThreadSummaryPort.execute(new RefreshThreadSummaryCommand(event.threadId()));
        } catch (Exception ex) {
            log.warn("Refresh thread summary failed threadId={} reason={}", event.threadId(), ex.getMessage());
        }
    }
}
