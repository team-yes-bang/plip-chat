package com.plip.chat.application.port.in;

import java.util.UUID;

public interface UpdateReadStateUseCase {

	void markRead(UUID agitUuid, UUID userUuid);
}
