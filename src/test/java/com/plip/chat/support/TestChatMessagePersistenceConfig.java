package com.plip.chat.support;

import com.plip.chat.application.port.out.ChatMessagePersistencePort;
import com.plip.chat.domain.model.ChatMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Configuration
@Profile("test")
public class TestChatMessagePersistenceConfig {

	@Bean
	public ChatMessagePersistencePort chatMessagePersistencePort() {
		return new ChatMessagePersistencePort() {
			private final List<ChatMessage> store = new ArrayList<>();

			@Override
			public ChatMessage save(ChatMessage chatMessage) {
				store.add(chatMessage);
				return chatMessage;
			}

			@Override
			public List<ChatMessage> findByAgitUuidOrderByCreatedAtDesc(UUID agitUuid) {
				return store.stream()
						.filter(message -> message.getAgitUuid().equals(agitUuid))
						.sorted(Comparator.comparing(ChatMessage::getCreatedAt).reversed())
						.toList();
			}
		};
	}
}
