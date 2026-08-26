package com.plip.chat.application.service;

import com.plip.chat.application.port.out.AgitReferenceQueryPort;
import com.plip.chat.application.port.out.ChatStatePort;
import com.plip.chat.domain.model.AgitMemberReference;
import com.plip.chat.domain.model.AgitRoomReference;
import com.plip.chat.domain.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UnreadMemberCountCalculator {

	private final AgitReferenceQueryPort agitReferenceQueryPort;
	private final ChatStatePort chatStatePort;

	public int compute(UUID agitUuid, ChatMessage message) {
		if (message == null || message.getSenderUuid() == null || message.getCreatedAt() == null) {
			return 0;
		}
		AgitRoomReference room = agitReferenceQueryPort.findByAgitUuid(agitUuid).orElse(null);
		if (room == null) {
			return 0;
		}

		Map<UUID, Instant> memberReads = chatStatePort.getMemberReadAtMap(agitUuid);
		Instant messageCreatedAt = message.getCreatedAt();
		UUID senderUuid = message.getSenderUuid();

		int unread = 0;
		for (AgitMemberReference member : room.getMembers()) {
			if (!member.isActive() || senderUuid.equals(member.getUserUuid())) {
				continue;
			}
			Instant readAt = memberReads.get(member.getUserUuid());
			if (readAt == null || readAt.isBefore(messageCreatedAt)) {
				unread++;
			}
		}
		return unread;
	}
}
