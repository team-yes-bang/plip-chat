package com.plip.chat.global.config;

import com.plip.chat.application.port.out.ChatWsTicketPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.socket.WebSocketHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserUuidHandshakeInterceptorTest {

	@Mock
	private ChatWsTicketPort chatWsTicketPort;

	@InjectMocks
	private UserUuidHandshakeInterceptor interceptor;

	@Test
	void beforeHandshake_consumesTicketAndStoresUserUuid() throws Exception {
		UUID userUuid = UUID.randomUUID();
		String ticket = UUID.randomUUID().toString();
		MockHttpServletRequest servletRequest = new MockHttpServletRequest("GET", "/ws/chat");
		servletRequest.setQueryString("ticket=" + ticket);
		Map<String, Object> attributes = new HashMap<>();

		given(chatWsTicketPort.consume(ticket)).willReturn(Optional.of(userUuid));

		boolean allowed = interceptor.beforeHandshake(
				new ServletServerHttpRequest(servletRequest),
				null,
				mock(WebSocketHandler.class),
				attributes
		);

		assertThat(allowed).isTrue();
		assertThat(attributes.get(UserUuidHandshakeInterceptor.USER_UUID_ATTRIBUTE)).isEqualTo(userUuid.toString());
		verify(chatWsTicketPort).consume(ticket);
	}

	@Test
	void beforeHandshake_rejectsMissingTicket() {
		Map<String, Object> attributes = new HashMap<>();

		boolean allowed = interceptor.beforeHandshake(
				new ServletServerHttpRequest(new MockHttpServletRequest()),
				null,
				mock(WebSocketHandler.class),
				attributes
		);

		assertThat(allowed).isFalse();
		assertThat(attributes).isEmpty();
	}

	@Test
	void beforeHandshake_rejectsInvalidOrExpiredTicket() {
		String ticket = "expired-ticket";
		MockHttpServletRequest servletRequest = new MockHttpServletRequest("GET", "/ws/chat");
		servletRequest.setQueryString("ticket=" + ticket);
		Map<String, Object> attributes = new HashMap<>();

		given(chatWsTicketPort.consume(ticket)).willReturn(Optional.empty());

		boolean allowed = interceptor.beforeHandshake(
				new ServletServerHttpRequest(servletRequest),
				null,
				mock(WebSocketHandler.class),
				attributes
		);

		assertThat(allowed).isFalse();
		assertThat(attributes).isEmpty();
	}
}
