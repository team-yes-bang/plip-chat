package com.plip.chat.adapter.out.persistence;

import com.plip.chat.domain.model.ChatMessage;
import com.plip.chat.domain.model.MessageType;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ChatMessagePersistenceMapper {

	public ChatMessageMongoDocument toDocument(ChatMessage chatMessage) {
		return new ChatMessageMongoDocument(
				chatMessage.getId().toString(),
				chatMessage.getAgitUuid().toString(),
				toSenderUuid(chatMessage),
				chatMessage.getType().name(),
				chatMessage.getContent(),
				chatMessage.getPayload(),
				chatMessage.getCreatedAt()
		);
	}

	public ChatMessage toDomain(ChatMessageMongoDocument document) {
		return ChatMessage.reconstitute(
				UUID.fromString(document.getId()),
				UUID.fromString(document.getAgitUuid()),
				toSenderUuid(document.getSenderUuid()),
				MessageType.valueOf(document.getType()),
				document.getContent(),
				document.getPayload(),
				document.getCreatedAt()
		);
	}

	private static String toSenderUuid(ChatMessage chatMessage) {
		if (chatMessage.getSenderUuid() == null) {
			return ChatMessage.SYSTEM_SENDER;
		}
		return chatMessage.getSenderUuid().toString();
	}

	private static UUID toSenderUuid(String senderUuid) {
		if (senderUuid == null || ChatMessage.SYSTEM_SENDER.equals(senderUuid)) {
			return null;
		}
		return UUID.fromString(senderUuid);
	}
}
