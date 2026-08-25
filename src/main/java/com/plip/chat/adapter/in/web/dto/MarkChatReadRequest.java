package com.plip.chat.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "채팅 읽음 처리 요청")
public class MarkChatReadRequest {

	@Schema(description = "읽음 시각. 없으면 서버 시각(Instant.now()) 사용", example = "2026-08-25T01:23:00Z")
	private Instant readAt;
}
