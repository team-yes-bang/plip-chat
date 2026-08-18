package com.plip.chat.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "채팅 전송 요청")
public class SendChatMessageRequest {

	@Schema(description = "메시지 본문", example = "안녕하세요")
	private String content;
}
