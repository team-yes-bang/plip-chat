package com.plip.chat.global.config;

import com.plip.chat.adapter.in.web.ChatController;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

import java.util.Map;
import java.util.UUID;

public class UserUuidChannelInterceptor implements ChannelInterceptor {

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
		if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
			return message;
		}
		String header = accessor.getFirstNativeHeader(ChatController.USER_UUID_HEADER);
		Map<String, Object> attributes = accessor.getSessionAttributes();
		String raw = header != null && !header.isBlank()
				? header.trim()
				: attributes == null ? null : stringValue(attributes.get(UserUuidHandshakeInterceptor.USER_UUID_ATTRIBUTE));
		UUID userUuid = parseUserUuid(raw);
		if (attributes != null) {
			attributes.put(UserUuidHandshakeInterceptor.USER_UUID_ATTRIBUTE, userUuid);
		}
		return message;
	}

	private static String stringValue(Object value) {
		return value == null ? null : value.toString();
	}

	private static UUID parseUserUuid(String raw) {
		if (raw == null || raw.isBlank()) {
			throw new MessagingException("X-User-UUID is required");
		}
		try {
			return UUID.fromString(raw.trim());
		} catch (IllegalArgumentException e) {
			throw new MessagingException("X-User-UUID is invalid");
		}
	}
}
