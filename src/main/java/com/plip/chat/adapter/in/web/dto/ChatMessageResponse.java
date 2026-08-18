package com.plip.chat.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "채팅 메시지")
public class ChatMessageResponse {

	@Schema(description = "메시지 ID (UUIDv7)", example = "018f3f6e-8e2a-7b3c-9d4e-5f6a7b8c9d0e")
	private UUID id;

	@Schema(description = "아지트 UUID")
	private UUID agitUuid;

	@Schema(description = "보낸 사람 UUID. SYSTEM 메시지는 null")
	private UUID senderUuid;

	@Schema(description = "메시지 유형", example = "TALK")
	private String type;

	@Schema(description = "본문")
	private String content;

	@Schema(description = "시스템 메시지 부가 정보")
	private Map<String, Object> payload;

	@Schema(description = "생성 시각")
	private Instant createdAt;
}
