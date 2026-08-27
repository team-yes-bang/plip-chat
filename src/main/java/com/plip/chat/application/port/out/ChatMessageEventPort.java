package com.plip.chat.application.port.out;

import com.plip.chat.domain.model.ChatMessage;

public interface ChatMessageEventPort {

	void publishMessageSent(ChatMessage message);
}
