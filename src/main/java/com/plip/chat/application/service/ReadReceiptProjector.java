package com.plip.chat.application.service;

import com.plip.chat.application.port.out.ChatMessagePersistencePort;
import com.plip.chat.application.port.out.ChatReceiptBroadcastPort;
import com.plip.chat.application.port.out.ChatReceiptPort;
import com.plip.chat.domain.event.MemberReadUpdated;
import com.plip.chat.domain.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReadReceiptProjector {

	private final ChatMessagePersistencePort chatMessagePersistencePort;
	private final ChatReceiptPort chatReceiptPort;
	private final ChatReceiptBroadcastPort chatReceiptBroadcastPort;
	private final UnreadMemberCountCalculator unreadMemberCountCalculator;

	public void onMemberReadUpdated(MemberReadUpdated event) {
		for (ChatMessage message : chatMessagePersistencePort.findTalkByAgitAndCreatedAtRange(
				event.agitUuid(),
				event.previousReadAt(),
				event.readAt(),
				event.readerUuid()
		)) {
			int unreadMemberCount = unreadMemberCountCalculator.compute(event.agitUuid(), message);
			chatReceiptPort.initUnreadMemberCount(event.agitUuid(), message.getId(), unreadMemberCount);
			chatReceiptBroadcastPort.publishReceiptUpdate(
					event.agitUuid(),
					message.getId(),
					unreadMemberCount
			);
		}
	}
}
