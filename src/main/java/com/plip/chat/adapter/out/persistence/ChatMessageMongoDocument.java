package com.plip.chat.adapter.out.persistence;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "chat_messages")
@CompoundIndex(name = "idx_chat_messages_agit_created", def = "{'agitUuid': 1, 'createdAt': -1}")
public class ChatMessageMongoDocument {

	@Id
	private String id;
	private String agitUuid;
	private String senderUuid;
	private String type;
	private String content;
	private Map<String, Object> payload;
	private Instant createdAt;

	public ChatMessageMongoDocument(
			String id,
			String agitUuid,
			String senderUuid,
			String type,
			String content,
			Map<String, Object> payload,
			Instant createdAt
	) {
		this.id = id;
		this.agitUuid = agitUuid;
		this.senderUuid = senderUuid;
		this.type = type;
		this.content = content;
		this.payload = payload;
		this.createdAt = createdAt;
	}
}
