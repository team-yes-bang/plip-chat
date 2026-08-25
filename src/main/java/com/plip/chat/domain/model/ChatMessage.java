package com.plip.chat.domain.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {

	public static final String SYSTEM_SENDER = "SYSTEM";
	public static final int MAX_TALK_CONTENT_LENGTH = 2000;

	private UUID id;
	private UUID agitUuid;
	private UUID senderUuid;
	private MessageType type;
	private String content;
	private Map<String, Object> payload;
	private Instant createdAt;

	public static ChatMessage talk(UUID agitUuid, UUID senderUuid, String content) {
		if (senderUuid == null) {
			throw new IllegalArgumentException("senderUuid는 필수입니다.");
		}
		return create(agitUuid, senderUuid, MessageType.TALK, normalizeTalkContent(content), Map.of());
	}

	public static ChatMessage system(UUID agitUuid, String content, Map<String, Object> payload) {
		return create(agitUuid, null, MessageType.SYSTEM, content, payload);
	}

	public static ChatMessage reconstitute(
			UUID id,
			UUID agitUuid,
			UUID senderUuid,
			MessageType type,
			String content,
			Map<String, Object> payload,
			Instant createdAt
	) {
		if (id == null) {
			throw new IllegalArgumentException("id는 필수입니다.");
		}
		if (createdAt == null) {
			throw new IllegalArgumentException("createdAt은 필수입니다.");
		}
		return ChatMessage.builder()
				.id(id)
				.agitUuid(requireAgitUuid(agitUuid))
				.senderUuid(senderUuid)
				.type(requireType(type))
				.content(requireContent(content))
				.payload(copyPayload(payload))
				.createdAt(createdAt)
				.build();
	}

	private static ChatMessage create(
			UUID agitUuid,
			UUID senderUuid,
			MessageType type,
			String content,
			Map<String, Object> payload
	) {
		return ChatMessage.builder()
				.id(UuidV7.create())
				.agitUuid(requireAgitUuid(agitUuid))
				.senderUuid(senderUuid)
				.type(requireType(type))
				.content(requireContent(content))
				.payload(copyPayload(payload))
				.createdAt(Instant.now())
				.build();
	}

	private static UUID requireAgitUuid(UUID agitUuid) {
		if (agitUuid == null) {
			throw new IllegalArgumentException("agitUuid는 필수입니다.");
		}
		return agitUuid;
	}

	private static MessageType requireType(MessageType type) {
		if (type == null) {
			throw new IllegalArgumentException("type은 필수입니다.");
		}
		return type;
	}

	private static String requireContent(String content) {
		if (content == null || content.isBlank()) {
			throw new IllegalArgumentException("content는 필수입니다.");
		}
		return content;
	}

	static String normalizeTalkContent(String content) {
		if (content == null || content.isBlank()) {
			throw new IllegalArgumentException("content는 필수입니다.");
		}
		String trimmed = content.trim();
		if (trimmed.length() > MAX_TALK_CONTENT_LENGTH) {
			throw new IllegalArgumentException("content는 2000자 이하여야 합니다.");
		}
		return trimmed;
	}

	private static Map<String, Object> copyPayload(Map<String, Object> payload) {
		if (payload == null || payload.isEmpty()) {
			return Map.of();
		}
		return Map.copyOf(payload);
	}

	@Builder(access = AccessLevel.PRIVATE)
	private ChatMessage(
			UUID id,
			UUID agitUuid,
			UUID senderUuid,
			MessageType type,
			String content,
			Map<String, Object> payload,
			Instant createdAt
	) {
		this.id = id;
		this.agitUuid = agitUuid;
		this.senderUuid = senderUuid;
		this.type = type;
		this.content = content;
		this.payload = payload == null ? Map.of() : payload;
		this.createdAt = createdAt;
	}
}
