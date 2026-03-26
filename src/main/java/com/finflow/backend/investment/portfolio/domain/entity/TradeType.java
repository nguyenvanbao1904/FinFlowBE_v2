package com.finflow.backend.investment.portfolio.domain.entity;

/**
 * Loại giao dịch trong danh mục đầu tư.
 * BUY/SELL: Giao dịch cổ phiếu (bắt buộc có symbol, quantity, price)
 * DIVIDEND: Nhận cổ tức (bắt buộc có symbol)
 * DEPOSIT/WITHDRAW: Nạp/Rút tiền mặt vào/ra danh mục (không có symbol, quantity)
 */
public enum TradeType {
    BUY,
    SELL,
    DIVIDEND,
    DEPOSIT,
    WITHDRAW
}
