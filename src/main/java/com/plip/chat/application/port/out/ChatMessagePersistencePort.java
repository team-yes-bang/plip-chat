package com.plip.chat.application.port.out;

import com.plip.chat.domain.model.ChatMessage;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ChatMessagePersistencePort {

	ChatMessage save(ChatMessage chatMessage);

	List<ChatMessage> findByAgitUuidOrderByCreatedAtDesc(UUID agitUuid);

	List<ChatMessage> findHistory(UUID agitUuid, Instant cursorCreatedAt, UUID cursorId, int limit);

	long countUnread(UUID agitUuid, UUID userUuid, Instant readAt);

	List<ChatMessage> findTalkByAgitAndCreatedAtRange(
			UUID agitUuid,
			Instant afterExclusive,
			Instant toInclusive,
			UUID excludeSenderUuid
	);
}
