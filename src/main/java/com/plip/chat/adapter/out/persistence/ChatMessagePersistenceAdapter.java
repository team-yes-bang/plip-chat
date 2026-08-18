package com.plip.chat.adapter.out.persistence;

import com.plip.chat.application.port.out.ChatMessagePersistencePort;
import com.plip.chat.domain.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class ChatMessagePersistenceAdapter implements ChatMessagePersistencePort {

	private final ChatMessageMongoRepository chatMessageMongoRepository;
	private final ChatMessagePersistenceMapper chatMessagePersistenceMapper;

	@Override
	public ChatMessage save(ChatMessage chatMessage) {
		ChatMessageMongoDocument saved = chatMessageMongoRepository.save(
				chatMessagePersistenceMapper.toDocument(chatMessage)
		);
		return chatMessagePersistenceMapper.toDomain(saved);
	}

	@Override
	public List<ChatMessage> findByAgitUuidOrderByCreatedAtDesc(UUID agitUuid) {
		return chatMessageMongoRepository.findByAgitUuidOrderByCreatedAtDesc(agitUuid.toString())
				.stream()
				.map(chatMessagePersistenceMapper::toDomain)
				.toList();
	}
}
