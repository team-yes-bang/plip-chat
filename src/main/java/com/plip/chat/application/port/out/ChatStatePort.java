package com.plip.chat.application.port.out;

import java.time.Instant;
import java.util.UUID;

public interface ChatStatePort {

	void markRead(UUID userUuid, UUID agitUuid, Instant readAt);

	void updateLastChatAt(UUID agitUuid, Instant lastChatAt);
}
