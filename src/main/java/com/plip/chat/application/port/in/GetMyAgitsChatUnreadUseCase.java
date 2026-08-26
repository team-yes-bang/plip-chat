package com.plip.chat.application.port.in;

import com.plip.chat.application.port.in.dto.AgitChatUnreadResult;

import java.util.List;
import java.util.UUID;

public interface GetMyAgitsChatUnreadUseCase {

	List<AgitChatUnreadResult> getMyAgitsChatUnread(UUID userUuid, List<UUID> agitUuids);
}
