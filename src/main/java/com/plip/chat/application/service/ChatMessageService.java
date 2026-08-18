package com.plip.chat.application.service;

import com.plip.chat.application.port.in.ListChatMessagesUseCase;
import com.plip.chat.application.port.in.SaveChatMessageUseCase;
import com.plip.chat.application.port.out.ChatMessagePersistencePort;
import com.plip.chat.domain.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatMessageService implements SaveChatMessageUseCase, ListChatMessagesUseCase {

	private final ChatMessagePersistencePort chatMessagePersistencePort;

	@Override
	public ChatMessage save(ChatMessage chatMessage) {
		if (chatMessage == null) {
			throw new IllegalArgumentException("chatMessage는 필수입니다.");
		}
		return chatMessagePersistencePort.save(chatMessage);
	}

	@Override
	public List<ChatMessage> listByAgitUuid(UUID agitUuid) {
		if (agitUuid == null) {
			throw new IllegalArgumentException("agitUuid는 필수입니다.");
		}
		return chatMessagePersistencePort.findByAgitUuidOrderByCreatedAtDesc(agitUuid);
	}
}
