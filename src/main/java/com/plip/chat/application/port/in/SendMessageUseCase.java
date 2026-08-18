package com.plip.chat.application.port.in;

import com.plip.chat.domain.model.ChatMessage;

import java.util.UUID;

public interface SendMessageUseCase {

	ChatMessage sendTalk(UUID agitUuid, UUID userUuid, String content);
}
