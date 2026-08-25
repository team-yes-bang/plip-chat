package com.plip.chat.global.config;

import com.plip.chat.adapter.in.web.ChatController;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserUuidChannelInterceptorTest {

	private final UserUuidChannelInterceptor interceptor = new UserUuidChannelInterceptor();

	@Test
	void connect_storesUserUuidFromHandshakeSession() {
		UUID userUuid = UUID.randomUUID();
		StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
		accessor.setLeaveMutable(true);
		Map<String, Object> attributes = new HashMap<>();
		attributes.put(UserUuidHandshakeInterceptor.USER_UUID_ATTRIBUTE, userUuid.toString());
		accessor.setSessionAttributes(attributes);
		Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

		interceptor.preSend(message, null);

		assertThat(attributes.get(UserUuidHandshakeInterceptor.USER_UUID_ATTRIBUTE)).isEqualTo(userUuid);
	}

	@Test
	void connect_ignoresClientStompHeaderWhenSessionHasUserUuid() {
		UUID sessionUuid = UUID.randomUUID();
		UUID spoofedUuid = UUID.randomUUID();
		StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
		accessor.setLeaveMutable(true);
		accessor.setNativeHeader(ChatController.USER_UUID_HEADER, spoofedUuid.toString());
		Map<String, Object> attributes = new HashMap<>();
		attributes.put(UserUuidHandshakeInterceptor.USER_UUID_ATTRIBUTE, sessionUuid.toString());
		accessor.setSessionAttributes(attributes);
		Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

		interceptor.preSend(message, null);

		assertThat(attributes.get(UserUuidHandshakeInterceptor.USER_UUID_ATTRIBUTE)).isEqualTo(sessionUuid);
	}

	@Test
	void connect_rejectsMissingUserUuid() {
		StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
		accessor.setLeaveMutable(true);
		accessor.setSessionAttributes(new HashMap<>());
		Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

		assertThatThrownBy(() -> interceptor.preSend(message, null))
				.isInstanceOf(MessagingException.class)
				.hasMessageContaining("Gateway handshake userUuid");
	}
}
