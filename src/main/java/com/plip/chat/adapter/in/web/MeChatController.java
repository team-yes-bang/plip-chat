package com.plip.chat.adapter.in.web;

import com.plip.chat.adapter.in.web.dto.MyAgitsChatUnreadResponse;
import com.plip.chat.application.exception.UnauthorizedException;
import com.plip.chat.application.port.in.GetMyAgitsChatUnreadUseCase;
import com.plip.chat.application.port.in.dto.AgitChatUnreadResult;
import com.plip.chat.global.config.SwaggerConfig;
import com.plip.chat.global.web.RequestHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Chat", description = "채팅 내역·읽음 API")
@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH_SCHEME)
@RequestMapping("/api/v1/me/agits")
@RestController
@RequiredArgsConstructor
public class MeChatController {

	public static final String USER_UUID_HEADER = RequestHeaders.USER_UUID_HEADER;

	private final GetMyAgitsChatUnreadUseCase getMyAgitsChatUnreadUseCase;

	@Operation(
			summary = "내 아지트 미읽음 TALK 배치 조회",
			description = "요청한 agitUuids 중 ACTIVE 멤버인 아지트만 unreadMessageCount를 반환합니다."
	)
	@GetMapping("/chat-unread")
	public MyAgitsChatUnreadResponse getMyAgitsChatUnread(
			@Parameter(hidden = true) @RequestHeader(value = USER_UUID_HEADER, required = false) String userUuidHeader,
			@RequestParam(required = false) List<UUID> agitUuids
	) {
		UUID userUuid = requireUserUuid(userUuidHeader);
		List<MyAgitsChatUnreadResponse.Item> items = getMyAgitsChatUnreadUseCase
				.getMyAgitsChatUnread(userUuid, agitUuids)
				.stream()
				.map(MeChatController::toItem)
				.toList();
		return MyAgitsChatUnreadResponse.builder().items(items).build();
	}

	private static MyAgitsChatUnreadResponse.Item toItem(AgitChatUnreadResult result) {
		return MyAgitsChatUnreadResponse.Item.builder()
				.agitUuid(result.agitUuid())
				.unreadMessageCount(result.unreadMessageCount())
				.build();
	}

	private static UUID requireUserUuid(String header) {
		if (header == null || header.isBlank()) {
			throw new UnauthorizedException();
		}
		try {
			return UUID.fromString(header.trim());
		} catch (IllegalArgumentException e) {
			throw new UnauthorizedException();
		}
	}
}
