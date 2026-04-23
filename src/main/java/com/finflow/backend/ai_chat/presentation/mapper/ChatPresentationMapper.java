package com.finflow.backend.ai_chat.presentation.mapper;

import com.finflow.backend.ai_chat.application.dto.ChatMessageOutput;
import com.finflow.backend.ai_chat.application.dto.ChatMessageSourceOutput;
import com.finflow.backend.ai_chat.application.dto.ChatThreadOutput;
import com.finflow.backend.ai_chat.application.dto.SendChatMessageOutput;
import com.finflow.backend.ai_chat.presentation.response.ChatMessageResponse;
import com.finflow.backend.ai_chat.presentation.response.ChatMessageSourceResponse;
import com.finflow.backend.ai_chat.presentation.response.ChatThreadResponse;
import com.finflow.backend.ai_chat.presentation.response.SendChatMessageResponse;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE,
        unmappedSourcePolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface ChatPresentationMapper {

    ChatThreadResponse toResponse(ChatThreadOutput output);

    ChatMessageResponse toResponse(ChatMessageOutput output);

    ChatMessageSourceResponse toResponse(ChatMessageSourceOutput output);

    SendChatMessageResponse toResponse(SendChatMessageOutput output);
}
