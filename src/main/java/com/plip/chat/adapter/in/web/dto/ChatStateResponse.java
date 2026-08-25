package com.plip.chat.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@Schema(description = "아지트 채팅 상태")
public class ChatStateResponse {

	@Schema(description = "내 읽음 시각. 없으면 null")
	private Instant readAt;

	@Schema(description = "마지막 TALK 시각. 없으면 null")
	private Instant lastChatAt;

	@Schema(description = "내가 읽지 않은 TALK 개수")
	private long unreadMessageCount;
}
