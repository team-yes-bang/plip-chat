package com.plip.chat.global.config;

import com.plip.chat.adapter.in.web.ChatController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class UserUuidHandshakeInterceptor implements HandshakeInterceptor {

	static final String USER_UUID_ATTRIBUTE = "userUuid";

	@Override
	public boolean beforeHandshake(
			ServerHttpRequest request,
			ServerHttpResponse response,
			WebSocketHandler wsHandler,
			Map<String, Object> attributes
	) {
		HttpHeaders headers = request.getHeaders();
		String userUuid = headers.getFirst(ChatController.USER_UUID_HEADER);
		if (userUuid != null && !userUuid.isBlank()) {
			attributes.put(USER_UUID_ATTRIBUTE, userUuid.trim());
		}
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
