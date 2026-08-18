package com.plip.chat.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "다음 페이지 커서")
public class ChatCursorResponse {

	@Schema(description = "마지막 메시지의 createdAt")
	private Instant createdAt;

	@Schema(description = "마지막 메시지의 id")
	private UUID id;
}
