package com.plip.chat.application.port.in.dto;

public record WsTicketResult(String ticket, long expiresInSeconds) {
}
