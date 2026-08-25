package com.plip.chat.adapter.in.web;

import com.plip.chat.adapter.in.web.dto.ChatWsTicketResponse;
import com.plip.chat.application.exception.UnauthorizedException;
import com.plip.chat.application.port.in.IssueWsTicketUseCase;
import com.plip.chat.global.config.SwaggerConfig;
import com.plip.chat.global.web.RequestHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Chat WebSocket", description = "STOMP WebSocket 티켓 API")
@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH_SCHEME)
@RequestMapping("/api/v1/ws")
@RestController
@RequiredArgsConstructor
public class ChatWsTicketController {

	private final IssueWsTicketUseCase issueWsTicketUseCase;

	@Operation(
			summary = "WebSocket 일회용 티켓 발급",
			description = "Gateway JWT 인증 후 X-User-UUID로 발급합니다. "
					+ "반환된 ticket은 /ws/chat?ticket=... SockJS handshake에 1회만 사용할 수 있습니다."
	)
	@PostMapping("/ticket")
	@ResponseStatus(HttpStatus.CREATED)
	public ChatWsTicketResponse issueTicket(
			@Parameter(hidden = true) @RequestHeader(value = RequestHeaders.USER_UUID_HEADER, required = false) String userUuidHeader
	) {
		var result = issueWsTicketUseCase.issue(requireUserUuid(userUuidHeader));
		return ChatWsTicketResponse.builder()
				.ticket(result.ticket())
				.expiresInSeconds(result.expiresInSeconds())
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
