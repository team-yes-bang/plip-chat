package com.plip.chat.application.port.in;

import java.time.Instant;
import java.util.UUID;

public interface UpdateReadStateUseCase {

	void markRead(UUID agitUuid, UUID userUuid, Instant readAt);
}
