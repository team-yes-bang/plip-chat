package com.plip.chat.application.port.out;

import com.plip.chat.domain.model.ChatMessage;

public interface ChatBroadcastPort {

	void publish(ChatMessage message);
}
