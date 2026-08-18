package com.plip.chat.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "채팅 내역 응답")
public class ChatHistoryResponse {

	@Schema(description = "메시지 목록 (createdAt 내림차순)")
	private List<ChatMessageResponse> messages;

	@Schema(description = "다음 페이지 커서. 더 없으면 null")
	private ChatCursorResponse nextCursor;

	@Schema(description = "다음 페이지 존재 여부")
	private boolean hasNext;
}
