package com.plip.chat.application.port.in.dto;

import com.plip.chat.domain.model.ChatMessage;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public class ChatHistoryResult {

	private final List<ChatMessage> messages;
	private final Instant nextCursorCreatedAt;
	private final UUID nextCursorId;
	private final boolean hasNext;
	private final Map<UUID, Integer> unreadMemberCounts;

	public ChatHistoryResult(
			List<ChatMessage> messages,
			Instant nextCursorCreatedAt,
			UUID nextCursorId,
			boolean hasNext
	) {
		this(messages, nextCursorCreatedAt, nextCursorId, hasNext, Map.of());
	}

	public ChatHistoryResult(
			List<ChatMessage> messages,
			Instant nextCursorCreatedAt,
			UUID nextCursorId,
			boolean hasNext,
			Map<UUID, Integer> unreadMemberCounts
	) {
		this.messages = messages == null ? List.of() : List.copyOf(messages);
		this.nextCursorCreatedAt = nextCursorCreatedAt;
		this.nextCursorId = nextCursorId;
		this.hasNext = hasNext;
		this.unreadMemberCounts = unreadMemberCounts == null ? Map.of() : Map.copyOf(unreadMemberCounts);
	}
}
