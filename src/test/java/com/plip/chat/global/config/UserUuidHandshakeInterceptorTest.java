package com.plip.chat.global.config;

import com.plip.chat.adapter.in.web.ChatController;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.socket.WebSocketHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class UserUuidHandshakeInterceptorTest {

	private final UserUuidHandshakeInterceptor interceptor = new UserUuidHandshakeInterceptor();

	@Test
	void beforeHandshake_storesUserUuidFromHeader() {
		UUID userUuid = UUID.randomUUID();
		MockHttpServletRequest servletRequest = new MockHttpServletRequest();
		servletRequest.addHeader(ChatController.USER_UUID_HEADER, userUuid.toString());
		Map<String, Object> attributes = new HashMap<>();

		boolean allowed = interceptor.beforeHandshake(
				new ServletServerHttpRequest(servletRequest),
				null,
				mock(WebSocketHandler.class),
				attributes
		);

		assertThat(allowed).isTrue();
		assertThat(attributes.get(UserUuidHandshakeInterceptor.USER_UUID_ATTRIBUTE)).isEqualTo(userUuid.toString());
	}
}
