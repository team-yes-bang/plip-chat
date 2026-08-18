package com.plip.chat.application.port.in;

import com.plip.chat.domain.model.ChatMessage;

import java.util.List;
import java.util.UUID;

public interface ListChatMessagesUseCase {

	List<ChatMessage> listByAgitUuid(UUID agitUuid);
}
