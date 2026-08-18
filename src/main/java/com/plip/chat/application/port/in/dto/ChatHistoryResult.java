package com.plip.chat.application.port.in.dto;

import com.plip.chat.domain.model.ChatMessage;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
public class ChatHistoryResult {

	private final List<ChatMessage> messages;
	private final Instant nextCursorCreatedAt;
	private final UUID nextCursorId;
	private final boolean hasNext;

	public ChatHistoryResult(
			List<ChatMessage> messages,
			Instant nextCursorCreatedAt,
			UUID nextCursorId,
			boolean hasNext
	) {
		this.messages = messages == null ? List.of() : List.copyOf(messages);
		this.nextCursorCreatedAt = nextCursorCreatedAt;
		this.nextCursorId = nextCursorId;
		this.hasNext = hasNext;
	}
}
