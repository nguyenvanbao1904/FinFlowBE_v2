package com.finflow.backend.ai_chat.application.port.in;

import com.finflow.backend.ai_chat.domain.entity.ChatMessage;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import com.finflow.backend.ai_chat.domain.entity.ChatThread;

public interface RefreshThreadSummaryPort {
    void execute(String threadId);
}
