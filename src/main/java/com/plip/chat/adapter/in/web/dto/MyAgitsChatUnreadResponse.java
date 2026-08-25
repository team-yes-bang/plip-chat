package com.plip.chat.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "아지트별 미읽음 TALK 개수 배치 응답")
public class MyAgitsChatUnreadResponse {

	@Schema(description = "ACTIVE 멤버인 아지트만 포함")
	private List<Item> items;

	@Getter
	@Builder
	@Schema(description = "아지트별 미읽음 TALK 개수")
	public static class Item {

		@Schema(description = "아지트 UUID")
		private UUID agitUuid;

		@Schema(description = "미읽음 TALK 개수")
		private long unreadMessageCount;
	}
}
