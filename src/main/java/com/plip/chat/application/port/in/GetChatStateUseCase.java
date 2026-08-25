package com.plip.chat.application.port.in;

import com.plip.chat.application.port.in.dto.ChatStateResult;

import java.util.UUID;

public interface GetChatStateUseCase {

	ChatStateResult getChatState(UUID agitUuid, UUID userUuid);
}
