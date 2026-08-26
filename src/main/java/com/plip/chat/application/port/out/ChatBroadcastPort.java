package com.plip.chat.application.port.out;

import com.plip.chat.domain.model.ChatMessage;

public interface ChatBroadcastPort {

	default void publish(ChatMessage message) {
		publish(message, null);
	}

	void publish(ChatMessage message, Integer unreadMemberCount);
}
