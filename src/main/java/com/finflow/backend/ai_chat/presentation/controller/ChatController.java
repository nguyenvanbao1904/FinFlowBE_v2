package com.finflow.backend.ai_chat.presentation.controller;

import com.finflow.backend.ai_chat.application.command.DeleteChatThreadCommand;
import com.finflow.backend.ai_chat.application.port.in.CreateChatThreadPort;
import com.finflow.backend.ai_chat.application.port.in.DeleteChatThreadPort;
import com.finflow.backend.ai_chat.application.port.in.ListChatMessagesPort;
import com.finflow.backend.ai_chat.application.port.in.ListChatThreadsPort;
import com.finflow.backend.ai_chat.application.port.in.SendChatMessagePort;
import com.finflow.backend.ai_chat.application.query.ListChatMessagesQuery;
import com.finflow.backend.ai_chat.application.query.ListChatThreadsQuery;
import com.finflow.backend.ai_chat.application.command.CreateChatThreadCommand;
import com.finflow.backend.ai_chat.application.command.SendChatMessageCommand;
import com.finflow.backend.ai_chat.presentation.mapper.ChatPresentationMapper;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@ApiVersion("1")
@Tag(name = "Chat", description = "Investment AI chat APIs")
public class ChatController {

    private final CreateChatThreadPort createChatThreadUseCase;
    private final DeleteChatThreadPort deleteChatThreadUseCase;
    private final ListChatThreadsPort listChatThreadsUseCase;
    private final ListChatMessagesPort listChatMessagesUseCase;
    private final SendChatMessagePort sendChatMessageUseCase;
    private final ChatPresentationMapper mapper;

    @Operation(summary = "Create a new chat thread")
    @PostMapping("/threads")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ChatThreadResponse> createThread(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody(required = false) CreateChatThreadRequest request
    ) {
        String userId = jwt.getSubject();
        String title = request == null ? null : request.title();
        var output = createChatThreadUseCase.execute(new CreateChatThreadCommand(userId, title));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(output));
    }

    @Operation(summary = "List chat threads of current user (paginated, newest first)")
    @GetMapping("/threads")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Page<ChatThreadResponse>> getThreads(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        String userId = jwt.getSubject();
        int safeSize = Math.min(size, 100);
        PageRequest pageable = PageRequest.of(page, safeSize, Sort.by("updatedAt").descending());
        return ResponseEntity.ok(
                listChatThreadsUseCase.execute(new ListChatThreadsQuery(userId, pageable))
                        .map(mapper::toResponse));
    }

    @Operation(summary = "List messages in one chat thread (paginated, oldest first)")
    @GetMapping("/threads/{threadId}/messages")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Page<ChatMessageResponse>> getMessages(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String threadId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        String userId = jwt.getSubject();
        int safeSize = Math.min(size, 100);
        PageRequest pageable = PageRequest.of(page, safeSize, Sort.by("createdAt").ascending());
        return ResponseEntity.ok(
                listChatMessagesUseCase.execute(new ListChatMessagesQuery(userId, threadId, pageable))
                        .map(mapper::toResponse));
    }

    @Operation(summary = "Send a new message and receive assistant reply")
    @PostMapping("/threads/{threadId}/messages")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<SendChatMessageResponse> sendMessage(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String threadId,
            @Valid @RequestBody SendChatMessageRequest request
    ) {
        String userId = jwt.getSubject();
        var output = sendChatMessageUseCase.execute(
            new SendChatMessageCommand(userId, threadId, request.content())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(output));
    }

    @Operation(summary = "Delete a chat thread and all its messages")
    @DeleteMapping("/threads/{threadId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> deleteThread(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String threadId
    ) {
        String userId = jwt.getSubject();
        deleteChatThreadUseCase.execute(new DeleteChatThreadCommand(userId, threadId));
        return ResponseEntity.noContent().build();
    }
}
