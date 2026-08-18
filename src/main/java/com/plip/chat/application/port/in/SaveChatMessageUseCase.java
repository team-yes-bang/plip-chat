package com.plip.chat.application.port.in;

import com.plip.chat.domain.model.ChatMessage;

public interface SaveChatMessageUseCase {

	ChatMessage save(ChatMessage chatMessage);
}
