package com.plip.chat.adapter.in.web;

import com.plip.chat.adapter.in.web.dto.SendChatMessageRequest;
import com.plip.chat.application.exception.UnauthorizedException;
import com.plip.chat.application.port.in.SendMessageUseCase;
import com.plip.chat.global.config.UserUuidHandshakeInterceptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ChatWebSocketControllerTest {

	@Mock
	private SendMessageUseCase sendMessageUseCase;

	@InjectMocks
	private ChatWebSocketController chatWebSocketController;

	@Test
	void send_delegatesToUseCaseWithSessionUserUuid() {
		UUID agitUuid = UUID.randomUUID();
		UUID userUuid = UUID.randomUUID();
		SendChatMessageRequest request = new SendChatMessageRequest();
		request.setContent("안녕");

		chatWebSocketController.send(agitUuid, request, accessorWithUser(userUuid));

		verify(sendMessageUseCase).sendTalk(agitUuid, userUuid, "안녕");
	}

	@Test
	void send_passesRawContentToUseCase() {
		UUID agitUuid = UUID.randomUUID();
		UUID userUuid = UUID.randomUUID();
		SendChatMessageRequest request = new SendChatMessageRequest();
		request.setContent("  hi  ");

		chatWebSocketController.send(agitUuid, request, accessorWithUser(userUuid));

		verify(sendMessageUseCase).sendTalk(agitUuid, userUuid, "  hi  ");
	}

	@Test
	void send_rejectsMissingUserUuid() {
		UUID agitUuid = UUID.randomUUID();
		SendChatMessageRequest request = new SendChatMessageRequest();
		request.setContent("안녕");

		assertThatThrownBy(() -> chatWebSocketController.send(agitUuid, request, accessorWithUser(null)))
				.isInstanceOf(UnauthorizedException.class);
		verifyNoInteractions(sendMessageUseCase);
	}

	private static SimpMessageHeaderAccessor accessorWithUser(UUID userUuid) {
		SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create();
		Map<String, Object> attributes = new HashMap<>();
		if (userUuid != null) {
			attributes.put(UserUuidHandshakeInterceptor.USER_UUID_ATTRIBUTE, userUuid);
		}
		accessor.setSessionAttributes(attributes);
		return accessor;
	}
}
