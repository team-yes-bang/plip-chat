package com.plip.chat.application.service;

import com.plip.chat.application.exception.ChatAccessDeniedException;
import com.plip.chat.application.port.in.SendMessageUseCase;
import com.plip.chat.application.port.out.AgitReferenceQueryPort;
import com.plip.chat.application.port.out.ChatBroadcastPort;
import com.plip.chat.application.port.out.ChatMessageEventPort;
import com.plip.chat.application.port.out.ChatMessagePersistencePort;
import com.plip.chat.application.port.out.ChatReceiptPort;
import com.plip.chat.application.port.out.ChatStatePort;
import com.plip.chat.domain.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatCommandService implements SendMessageUseCase {

	private final AgitReferenceQueryPort agitReferenceQueryPort;
	private final ChatMessagePersistencePort chatMessagePersistencePort;
	private final ChatStatePort chatStatePort;
	private final ChatReceiptPort chatReceiptPort;
	private final ChatBroadcastPort chatBroadcastPort;
	private final ChatMessageEventPort chatMessageEventPort;

	@Override
	public ChatMessage sendTalk(UUID agitUuid, UUID userUuid, String content) {
		if (agitUuid == null) {
			throw new IllegalArgumentException("agitUuid는 필수입니다.");
		}
		if (userUuid == null) {
			throw new IllegalArgumentException("userUuid는 필수입니다.");
		}
		if (!agitReferenceQueryPort.isActiveMember(agitUuid, userUuid)) {
			throw new ChatAccessDeniedException();
		}
		ChatMessage saved = chatMessagePersistencePort.save(ChatMessage.talk(agitUuid, userUuid, content));
		int unreadMemberCount = Math.max(0, agitReferenceQueryPort.countActiveMembers(agitUuid) - 1);
		chatReceiptPort.initUnreadMemberCount(agitUuid, saved.getId(), unreadMemberCount);
		chatStatePort.updateLastChatAt(agitUuid, saved.getCreatedAt());
		chatBroadcastPort.publish(saved, unreadMemberCount);
		chatMessageEventPort.publishMessageSent(saved);
		return saved;
	}
}
