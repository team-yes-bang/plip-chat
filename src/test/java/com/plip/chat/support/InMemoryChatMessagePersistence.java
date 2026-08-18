package com.plip.chat.support;

import com.plip.chat.application.port.out.ChatMessagePersistencePort;
import com.plip.chat.domain.model.ChatMessage;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class InMemoryChatMessagePersistence implements ChatMessagePersistencePort {

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
				.sorted(historyOrder())
				.toList();
	}

	@Override
	public List<ChatMessage> findHistory(UUID agitUuid, Instant cursorCreatedAt, UUID cursorId, int limit) {
		return store.stream()
				.filter(message -> message.getAgitUuid().equals(agitUuid))
				.filter(message -> isBeforeCursor(message, cursorCreatedAt, cursorId))
				.sorted(historyOrder())
				.limit(limit)
				.toList();
	}

	private static Comparator<ChatMessage> historyOrder() {
		return Comparator.comparing(ChatMessage::getCreatedAt)
				.reversed()
				.thenComparing(ChatMessage::getId, Comparator.reverseOrder());
	}

	private static boolean isBeforeCursor(ChatMessage message, Instant cursorCreatedAt, UUID cursorId) {
		if (cursorCreatedAt == null || cursorId == null) {
			return true;
		}
		int timeCompare = message.getCreatedAt().compareTo(cursorCreatedAt);
		if (timeCompare != 0) {
			return timeCompare < 0;
		}
		return message.getId().compareTo(cursorId) < 0;
	}
}
