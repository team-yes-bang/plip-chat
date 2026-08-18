package com.plip.chat.adapter.out.redis;

import com.plip.chat.domain.model.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatBroadcastPayload {

	private UUID id;
	private UUID agitUuid;
	private UUID senderUuid;
	private String type;
	private String content;
	private Map<String, Object> payload;
	private Instant createdAt;

	public static ChatBroadcastPayload from(ChatMessage message) {
		return new ChatBroadcastPayload(
				message.getId(),
				message.getAgitUuid(),
				message.getSenderUuid(),
				message.getType().name(),
				message.getContent(),
				message.getPayload(),
				message.getCreatedAt()
		);
	}
}
