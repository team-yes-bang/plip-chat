package com.plip.chat.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "WebSocket 일회용 티켓 발급 응답")
public class ChatWsTicketResponse {

	@Schema(description = "SockJS 연결 시 ticket 쿼리 파라미터로 1회 사용", example = "018f3f6e-8e2a-7b3c-9d4e-5f6a7b8c9d0e")
	private final String ticket;

	@Schema(description = "유효 시간(초)", example = "30")
	private final long expiresInSeconds;
}
