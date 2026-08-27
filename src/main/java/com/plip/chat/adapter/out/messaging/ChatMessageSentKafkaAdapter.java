package com.plip.chat.adapter.out.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plip.chat.adapter.in.messaging.ChatEventTopics;
import com.plip.chat.application.port.out.ChatMessageEventPort;
import com.plip.chat.domain.model.ChatMessage;
import com.plip.chat.domain.model.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class ChatMessageSentKafkaAdapter implements ChatMessageEventPort {

	private static final int PREVIEW_LIMIT = 80;

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;

	@Override
	public void publishMessageSent(ChatMessage message) {
		if (message == null || message.getType() != MessageType.TALK || message.getSenderUuid() == null) {
			return;
		}
		try {
			Map<String, Object> payload = new LinkedHashMap<>();
			payload.put("agitUuid", message.getAgitUuid().toString());
			payload.put("messageId", message.getId() == null ? null : message.getId().toString());
			payload.put("senderUserUuid", message.getSenderUuid().toString());
			payload.put("contentPreview", preview(message.getContent()));
			payload.put("occurredAt", message.getCreatedAt() == null ? null : message.getCreatedAt().toString());
			kafkaTemplate.send(
					ChatEventTopics.MESSAGE_SENT,
					message.getAgitUuid().toString(),
					objectMapper.writeValueAsString(payload)
			);
		} catch (Exception exception) {
			log.warn("chat.message-sent publish skipped: {}", exception.getMessage());
		}
	}

	private static String preview(String content) {
		if (content == null) {
			return "";
		}
		String trimmed = content.trim();
		return trimmed.length() <= PREVIEW_LIMIT ? trimmed : trimmed.substring(0, PREVIEW_LIMIT);
	}
}
