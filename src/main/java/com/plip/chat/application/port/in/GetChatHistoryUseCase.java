package com.plip.chat.application.port.in;

import com.plip.chat.application.port.in.dto.ChatHistoryResult;

import java.time.Instant;
import java.util.UUID;

public interface GetChatHistoryUseCase {

	ChatHistoryResult getHistory(
			UUID agitUuid,
			UUID userUuid,
			Instant cursorCreatedAt,
			UUID cursorId,
			int size
	);
}
