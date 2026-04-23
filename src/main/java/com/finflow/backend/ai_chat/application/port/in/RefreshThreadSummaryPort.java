package com.finflow.backend.ai_chat.application.port.in;
import com.finflow.backend.ai_chat.application.command.RefreshThreadSummaryCommand;

public interface RefreshThreadSummaryPort {
    void execute(RefreshThreadSummaryCommand command);
}
