package com.plip.chat.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "에러 응답")
public class ErrorResponse {

	@Schema(description = "에러 코드", example = "MEMBER_NOT_ACTIVE")
	private String code;

	@Schema(description = "에러 메시지", example = "ACTIVE 멤버만 채팅을 사용할 수 있습니다.")
	private String message;
}
