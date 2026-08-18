package com.plip.chat.adapter.in.web;

import com.plip.chat.adapter.in.web.dto.ChatHistoryResponse;
import com.plip.chat.adapter.in.web.mapper.ChatWebMapper;
import com.plip.chat.application.exception.UnauthorizedException;
import com.plip.chat.application.port.in.GetChatHistoryUseCase;
import com.plip.chat.application.port.in.UpdateReadStateUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@Tag(name = "Chat", description = "채팅 내역·읽음 API")
@RequestMapping("/api/v1/agits/{agitUuid}")
@RestController
@RequiredArgsConstructor
public class ChatController {

	public static final String USER_UUID_HEADER = "X-User-UUID";

	private final GetChatHistoryUseCase getChatHistoryUseCase;
	private final UpdateReadStateUseCase updateReadStateUseCase;
	private final ChatWebMapper chatWebMapper;

	@Operation(summary = "채팅 내역 조회", description = "ACTIVE 멤버만 조회할 수 있습니다. cursorCreatedAt+cursorId로 이전 페이지를 요청합니다.")
	@Parameter(name = USER_UUID_HEADER, in = ParameterIn.HEADER, required = true, description = "Gateway가 주입하는 호출자 UUID")
	@GetMapping("/messages")
	public ChatHistoryResponse getMessages(
			@PathVariable UUID agitUuid,
			@RequestHeader(value = USER_UUID_HEADER, required = false) String userUuidHeader,
			@RequestParam(required = false) Instant cursorCreatedAt,
			@RequestParam(required = false) UUID cursorId,
			@RequestParam(defaultValue = "20") int size
	) {
		UUID userUuid = requireUserUuid(userUuidHeader);
		return chatWebMapper.toResponse(
				getChatHistoryUseCase.getHistory(agitUuid, userUuid, cursorCreatedAt, cursorId, size)
		);
	}

	@Operation(summary = "채팅 읽음 처리", description = "ACTIVE 멤버의 읽음 시각을 Redis에 저장합니다.")
	@Parameter(name = USER_UUID_HEADER, in = ParameterIn.HEADER, required = true, description = "Gateway가 주입하는 호출자 UUID")
	@PostMapping("/read")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void markRead(
			@PathVariable UUID agitUuid,
			@RequestHeader(value = USER_UUID_HEADER, required = false) String userUuidHeader
	) {
		UUID userUuid = requireUserUuid(userUuidHeader);
		updateReadStateUseCase.markRead(agitUuid, userUuid);
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
