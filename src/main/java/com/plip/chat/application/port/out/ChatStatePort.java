package com.plip.chat.application.port.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ChatStatePort {

	Optional<Instant> getReadAt(UUID userUuid, UUID agitUuid);

	void markRead(UUID userUuid, UUID agitUuid, Instant readAt);

	void updateLastChatAt(UUID agitUuid, Instant lastChatAt);
}
