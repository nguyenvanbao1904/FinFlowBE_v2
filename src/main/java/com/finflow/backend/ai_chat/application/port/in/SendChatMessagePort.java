package com.finflow.backend.ai_chat.application.port.in;

import com.finflow.backend.ai_chat.domain.entity.ChatMessage;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import com.finflow.backend.ai_chat.application.command.SendChatMessageCommand;
import java.math.RoundingMode;
import com.finflow.backend.ai_chat.presentation.response.ChatMessageResponse;
import com.finflow.backend.ai_chat.domain.entity.ChatMessageSource;
import com.finflow.backend.ai_chat.presentation.response.ChatMessageSourceResponse;
import java.util.ArrayList;
import com.finflow.backend.ai_chat.presentation.response.SendChatMessageResponse;
import com.finflow.backend.ai_chat.domain.entity.ChatThread;

public interface SendChatMessagePort {
    SendChatMessageResponse execute(SendChatMessageCommand command);
}
