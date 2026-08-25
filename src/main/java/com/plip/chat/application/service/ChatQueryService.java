package com.plip.chat.application.service;

import com.plip.chat.application.exception.ChatAccessDeniedException;
import com.plip.chat.application.port.in.GetChatHistoryUseCase;
import com.plip.chat.application.port.in.UpdateReadStateUseCase;
import com.plip.chat.application.port.in.dto.ChatHistoryResult;
import com.plip.chat.application.port.out.AgitReferenceQueryPort;
import com.plip.chat.application.port.out.ChatMessagePersistencePort;
import com.plip.chat.application.port.out.ChatReceiptPort;
import com.plip.chat.application.port.out.ChatStatePort;
import com.plip.chat.application.port.out.MemberReadEventPort;
import com.plip.chat.domain.event.MemberReadUpdated;
import com.plip.chat.domain.model.ChatMessage;
import com.plip.chat.domain.model.MessageType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatQueryService implements GetChatHistoryUseCase, UpdateReadStateUseCase {

	public static final int DEFAULT_PAGE_SIZE = 20;
	public static final int MAX_PAGE_SIZE = 100;

	private final AgitReferenceQueryPort agitReferenceQueryPort;
	private final ChatMessagePersistencePort chatMessagePersistencePort;
	private final ChatStatePort chatStatePort;
	private final ChatReceiptPort chatReceiptPort;
	private final MemberReadEventPort memberReadEventPort;

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
				hasNext,
				resolveUnreadMemberCounts(agitUuid, userUuid, page)
		);
	}

	@Override
	public void markRead(UUID agitUuid, UUID userUuid, Instant readAt) {
		requireIds(agitUuid, userUuid);
		requireActiveMember(agitUuid, userUuid);

		Instant requested = readAt != null ? readAt : Instant.now();
		Optional<Instant> existing = chatStatePort.getReadAt(userUuid, agitUuid);
		if (existing.isPresent() && !requested.isAfter(existing.get())) {
			return;
		}

		Instant previousReadAt = existing.orElse(null);
		chatStatePort.markRead(userUuid, agitUuid, requested);
		memberReadEventPort.publish(new MemberReadUpdated(agitUuid, userUuid, requested, previousReadAt));
	}

	private Map<UUID, Integer> resolveUnreadMemberCounts(UUID agitUuid, UUID userUuid, List<ChatMessage> messages) {
		List<UUID> myTalkMessageIds = messages.stream()
				.filter(message -> message.getType() == MessageType.TALK)
				.filter(message -> userUuid.equals(message.getSenderUuid()))
				.map(ChatMessage::getId)
				.toList();
		if (myTalkMessageIds.isEmpty()) {
			return Map.of();
		}
		return chatReceiptPort.getUnreadMemberCounts(agitUuid, myTalkMessageIds);
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
