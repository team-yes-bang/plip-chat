package com.plip.chat.application.port.out;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface ChatStatePort {

	Optional<Instant> getReadAt(UUID userUuid, UUID agitUuid);

	Optional<Instant> getReceiptProjectedAt(UUID userUuid, UUID agitUuid);

	Optional<Instant> getLastChatAt(UUID agitUuid);

	Optional<Instant> getMemberReadAt(UUID agitUuid, UUID userUuid);

	Map<UUID, Instant> getMemberReadAtMap(UUID agitUuid);

	void markRead(UUID userUuid, UUID agitUuid, Instant readAt);

	void setReceiptProjectedAt(UUID userUuid, UUID agitUuid, Instant projectedAt);

	void updateLastChatAt(UUID agitUuid, Instant lastChatAt);
}
