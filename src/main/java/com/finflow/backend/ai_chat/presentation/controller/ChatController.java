package com.finflow.backend.ai_chat.presentation.controller;

import com.finflow.backend.ai_chat.application.port.in.CreateChatThreadPort;
import com.finflow.backend.ai_chat.application.port.in.ListChatMessagesPort;
import com.finflow.backend.ai_chat.application.port.in.ListChatThreadsPort;
import com.finflow.backend.ai_chat.application.port.in.SendChatMessagePort;
import com.finflow.backend.ai_chat.application.command.CreateChatThreadCommand;
import com.finflow.backend.ai_chat.application.command.SendChatMessageCommand;
import com.finflow.backend.ai_chat.presentation.request.CreateChatThreadRequest;
import com.finflow.backend.ai_chat.presentation.request.SendChatMessageRequest;
import com.finflow.backend.ai_chat.presentation.response.ChatMessageResponse;
import com.finflow.backend.ai_chat.presentation.response.ChatThreadResponse;
import com.finflow.backend.ai_chat.presentation.response.SendChatMessageResponse;
import com.finflow.backend.common.versioning.ApiVersion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@ApiVersion("1")
@Tag(name = "Chat", description = "Investment AI chat APIs")
public class ChatController {

    private final CreateChatThreadPort createChatThreadUseCase;
    private final ListChatThreadsPort listChatThreadsUseCase;
    private final ListChatMessagesPort listChatMessagesUseCase;
    private final SendChatMessagePort sendChatMessageUseCase;

    @Operation(summary = "Create a new chat thread")
    @PostMapping("/threads")
    public ResponseEntity<ChatThreadResponse> createThread(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody(required = false) CreateChatThreadRequest request
    ) {
        String userId = jwt.getSubject();
        String title = request == null ? null : request.title();
        ChatThreadResponse response = createChatThreadUseCase.execute(
            new CreateChatThreadCommand(userId, title)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "List all chat threads of current user")
    @GetMapping("/threads")
    public ResponseEntity<List<ChatThreadResponse>> getThreads(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(listChatThreadsUseCase.execute(userId));
    }

    @Operation(summary = "List messages in one chat thread")
    @GetMapping("/threads/{threadId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String threadId
    ) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(listChatMessagesUseCase.execute(userId, threadId));
    }

    @Operation(summary = "Send a new message and receive assistant reply")
    @PostMapping("/threads/{threadId}/messages")
    public ResponseEntity<SendChatMessageResponse> sendMessage(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String threadId,
            @Valid @RequestBody SendChatMessageRequest request
    ) {
        String userId = jwt.getSubject();
        SendChatMessageResponse response = sendChatMessageUseCase.execute(
            new SendChatMessageCommand(userId, threadId, request.content())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
