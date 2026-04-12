package com.finflow.backend.finance.transaction.application.command;

/**
 * Input for AI-assisted transaction prefill (text analysis).
 */
public record AnalyzeTransactionCommand(String text) {}
