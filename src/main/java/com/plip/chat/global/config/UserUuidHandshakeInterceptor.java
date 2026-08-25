package com.plip.chat.global.config;

import com.plip.chat.application.port.out.ChatWsTicketPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class UserUuidHandshakeInterceptor implements HandshakeInterceptor {

	public static final String USER_UUID_ATTRIBUTE = "userUuid";
	public static final String TICKET_QUERY_PARAM = "ticket";

	private final ChatWsTicketPort chatWsTicketPort;

	@Override
	public boolean beforeHandshake(
			ServerHttpRequest request,
			ServerHttpResponse response,
			WebSocketHandler wsHandler,
			Map<String, Object> attributes
	) {
		String ticket = UriComponentsBuilder.fromUri(request.getURI())
				.build()
				.getQueryParams()
				.getFirst(TICKET_QUERY_PARAM);
		if (ticket == null || ticket.isBlank()) {
			return false;
		}
		Optional<UUID> userUuid = chatWsTicketPort.consume(ticket.trim());
		if (userUuid.isEmpty()) {
			return false;
		}
		attributes.put(USER_UUID_ATTRIBUTE, userUuid.get().toString());
		return true;
	}

	@Override
	public void afterHandshake(
			ServerHttpRequest request,
			ServerHttpResponse response,
			WebSocketHandler wsHandler,
			Exception exception
	) {
	}
}
