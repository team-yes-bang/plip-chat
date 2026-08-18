package com.plip.chat.application.service;

import com.plip.chat.application.exception.ChatAccessDeniedException;
import com.plip.chat.application.port.in.GetChatHistoryUseCase;
import com.plip.chat.application.port.in.UpdateReadStateUseCase;
import com.plip.chat.application.port.in.dto.ChatHistoryResult;
import com.plip.chat.application.port.out.AgitReferenceQueryPort;
import com.plip.chat.application.port.out.ChatMessagePersistencePort;
import com.plip.chat.application.port.out.ChatStatePort;
import com.plip.chat.domain.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatQueryService implements GetChatHistoryUseCase, UpdateReadStateUseCase {

	public static final int DEFAULT_PAGE_SIZE = 20;
	public static final int MAX_PAGE_SIZE = 100;

	private final AgitReferenceQueryPort agitReferenceQueryPort;
	private final ChatMessagePersistencePort chatMessagePersistencePort;
	private final ChatStatePort chatStatePort;

	@Override
	public ChatHistoryResult getHistory(
			UUID agitUuid,
			UUID userUuid,
			Instant cursorCreatedAt,
			UUID cursorId,
			int size
	) {
		requireIds(agitUuid, userUuid);
		requireCursorPair(cursorCreatedAt, cursorId);
		requireActiveMember(agitUuid, userUuid);

		int limit = normalizeSize(size);
		List<ChatMessage> fetched = chatMessagePersistencePort.findHistory(
				agitUuid,
				cursorCreatedAt,
				cursorId,
				limit + 1
		);
		boolean hasNext = fetched.size() > limit;
		List<ChatMessage> page = hasNext ? fetched.subList(0, limit) : fetched;
		if (page.isEmpty()) {
			return new ChatHistoryResult(List.of(), null, null, false);
		}
		ChatMessage oldest = page.get(page.size() - 1);
		return new ChatHistoryResult(
				page,
				hasNext ? oldest.getCreatedAt() : null,
				hasNext ? oldest.getId() : null,
				hasNext
		);
	}

	@Override
	public void markRead(UUID agitUuid, UUID userUuid) {
		requireIds(agitUuid, userUuid);
		requireActiveMember(agitUuid, userUuid);
		chatStatePort.markRead(userUuid, agitUuid, Instant.now());
	}

	private void requireActiveMember(UUID agitUuid, UUID userUuid) {
		if (!agitReferenceQueryPort.isActiveMember(agitUuid, userUuid)) {
			throw new ChatAccessDeniedException();
		}
	}

	private static void requireIds(UUID agitUuid, UUID userUuid) {
		if (agitUuid == null) {
			throw new IllegalArgumentException("agitUuid는 필수입니다.");
		}
		if (userUuid == null) {
			throw new IllegalArgumentException("userUuid는 필수입니다.");
		}
	}

	private static void requireCursorPair(Instant cursorCreatedAt, UUID cursorId) {
		if ((cursorCreatedAt == null) != (cursorId == null)) {
			throw new IllegalArgumentException("cursorCreatedAt과 cursorId는 함께 필요합니다.");
		}
	}

	private static int normalizeSize(int size) {
		if (size < 1) {
			return DEFAULT_PAGE_SIZE;
		}
		return Math.min(size, MAX_PAGE_SIZE);
	}
}
