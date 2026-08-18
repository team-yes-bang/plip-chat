package com.plip.chat.adapter.in.web;

import com.plip.chat.adapter.in.web.dto.SendChatMessageRequest;
import com.plip.chat.application.exception.UnauthorizedException;
import com.plip.chat.application.port.in.SendMessageUseCase;
import com.plip.chat.global.config.UserUuidHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

	private final SendMessageUseCase sendMessageUseCase;

	@MessageMapping("/agits/{agitUuid}/send")
	public void send(
			@DestinationVariable UUID agitUuid,
			@Payload SendChatMessageRequest request,
			SimpMessageHeaderAccessor accessor
	) {
		UUID userUuid = requireUserUuid(accessor);
		String content = request == null ? null : request.getContent();
		sendMessageUseCase.sendTalk(agitUuid, userUuid, content);
	}

	private static UUID requireUserUuid(SimpMessageHeaderAccessor accessor) {
		Map<String, Object> attributes = accessor.getSessionAttributes();
		Object value = attributes == null ? null : attributes.get(UserUuidHandshakeInterceptor.USER_UUID_ATTRIBUTE);
		if (value instanceof UUID userUuid) {
			return userUuid;
		}
		if (value instanceof String raw && !raw.isBlank()) {
			try {
				return UUID.fromString(raw.trim());
			} catch (IllegalArgumentException e) {
				throw new UnauthorizedException();
			}
		}
		throw new UnauthorizedException();
	}
}
