package com.plip.chat.application.service;

import com.plip.chat.application.exception.ChatAccessDeniedException;
import com.plip.chat.application.port.in.GetChatStateUseCase;
import com.plip.chat.application.port.in.GetMyAgitsChatUnreadUseCase;
import com.plip.chat.application.port.in.dto.AgitChatUnreadResult;
import com.plip.chat.application.port.in.dto.ChatStateResult;
import com.plip.chat.application.port.out.AgitReferenceQueryPort;
import com.plip.chat.application.port.out.ChatMessagePersistencePort;
import com.plip.chat.application.port.out.ChatStatePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatStateQueryService implements GetChatStateUseCase, GetMyAgitsChatUnreadUseCase {

	private final AgitReferenceQueryPort agitReferenceQueryPort;
	private final ChatMessagePersistencePort chatMessagePersistencePort;
	private final ChatStatePort chatStatePort;

	@Override
	public ChatStateResult getChatState(UUID agitUuid, UUID userUuid) {
		requireIds(agitUuid, userUuid);
		requireActiveMember(agitUuid, userUuid);

		Optional<Instant> readAt = chatStatePort.getReadAt(userUuid, agitUuid);
		Optional<Instant> lastChatAt = chatStatePort.getLastChatAt(agitUuid);
		long unreadMessageCount = chatMessagePersistencePort.countUnread(
				agitUuid,
				userUuid,
				readAt.orElse(null)
		);
		return new ChatStateResult(readAt.orElse(null), lastChatAt.orElse(null), unreadMessageCount);
	}

	@Override
	public List<AgitChatUnreadResult> getMyAgitsChatUnread(UUID userUuid, List<UUID> agitUuids) {
		if (userUuid == null) {
			throw new IllegalArgumentException("userUuid는 필수입니다.");
		}
		if (agitUuids == null || agitUuids.isEmpty()) {
			return List.of();
		}

		List<AgitChatUnreadResult> results = new ArrayList<>();
		for (UUID agitUuid : agitUuids) {
			if (agitUuid == null || !agitReferenceQueryPort.isActiveMember(agitUuid, userUuid)) {
				continue;
			}
			Optional<Instant> readAt = chatStatePort.getReadAt(userUuid, agitUuid);
			long unreadMessageCount = chatMessagePersistencePort.countUnread(
					agitUuid,
					userUuid,
					readAt.orElse(null)
			);
			results.add(new AgitChatUnreadResult(agitUuid, unreadMessageCount));
		}
		return List.copyOf(results);
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
}
