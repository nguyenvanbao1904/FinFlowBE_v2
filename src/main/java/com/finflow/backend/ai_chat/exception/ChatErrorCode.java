package com.finflow.backend.ai_chat.exception;

import com.finflow.backend.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ChatErrorCode implements ErrorCode {

    CHAT_THREAD_NOT_FOUND(7001, "Chat thread not found", HttpStatus.NOT_FOUND),
    CHAT_MESSAGE_CONTENT_REQUIRED(7002, "Message content is required", HttpStatus.BAD_REQUEST),
    CHAT_MESSAGE_CONTENT_TOO_LONG(7003, "Message content is too long", HttpStatus.BAD_REQUEST),
    CHAT_THREAD_TITLE_TOO_LONG(7004, "Thread title is too long", HttpStatus.BAD_REQUEST),
    CHAT_AI_UPSTREAM_ERROR(7005, "Chat AI service is temporarily unavailable", HttpStatus.BAD_GATEWAY),
    CHAT_SUMMARY_UPSTREAM_ERROR(7006, "Chat summary service is temporarily unavailable", HttpStatus.BAD_GATEWAY),
    CHAT_INVALID_MESSAGE_ROLE(7007, "Invalid message role", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ChatErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
