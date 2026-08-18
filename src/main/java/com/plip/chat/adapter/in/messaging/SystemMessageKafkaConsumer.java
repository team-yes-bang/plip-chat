package com.plip.chat.adapter.in.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plip.chat.application.port.in.HandleSystemMessageUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class SystemMessageKafkaConsumer {

	public static final String CONSUMER_GROUP = "chat-system-message";

	private final ObjectMapper objectMapper;
	private final HandleSystemMessageUseCase handleSystemMessageUseCase;

	@KafkaListener(
			topics = {
					AgitEventTopics.MEMBER_JOINED,
					AgitEventTopics.MEMBER_BANNED,
					TopicEventTopics.BOUND,
					TopicEventTopics.STARTED
					// TODO: AgitEventTopics.MEMBER_LEFT — 자진 퇴장 시스템 메시지
					// TODO: AgitEventTopics.DELETED — 아지트 삭제 시스템 메시지
					// TODO: TopicEventTopics.UNBOUND — topic.unbound (상수·핸들러 추가 후)
					// TODO: video.uploaded — 이벤트 스펙 확정 후
			},
			groupId = CONSUMER_GROUP
	)
	public void consume(String payload, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
		try {
			JsonNode node = objectMapper.readTree(payload);
			UUID agitUuid = requiredUuid(node, "agitUuid");
			switch (topic) {
				case AgitEventTopics.MEMBER_JOINED -> handleSystemMessageUseCase.onMemberJoined(
						agitUuid,
						requiredUuid(node, "userUuid"),
						text(node, "nickname")
				);
				case AgitEventTopics.MEMBER_BANNED -> handleSystemMessageUseCase.onMemberBanned(
						agitUuid,
						requiredUuid(node, "userUuid"),
						text(node, "nickname")
				);
				case TopicEventTopics.BOUND -> handleSystemMessageUseCase.onTopicBound(
						agitUuid,
						text(node, "topicId")
				);
				case TopicEventTopics.STARTED -> handleSystemMessageUseCase.onTopicStarted(
						agitUuid,
						text(node, "topicId")
				);
				// TODO: MEMBER_LEFT / DELETED / topic.unbound / video.uploaded 분기
				default -> log.warn("시스템 메시지 skip: 알 수 없는 토픽 {}", topic);
			}
		} catch (IllegalArgumentException e) {
			log.warn("시스템 메시지 skip: {}", e.getMessage());
		} catch (Exception e) {
			log.warn("시스템 메시지 처리 실패: {}", e.getMessage());
		}
	}

	private static UUID requiredUuid(JsonNode node, String field) {
		String value = text(node, field);
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(field + "는 필수입니다.");
		}
		return UUID.fromString(value);
	}

	private static String text(JsonNode node, String field) {
		JsonNode value = node.get(field);
		if (value == null || value.isNull()) {
			return null;
		}
		return value.asText();
	}
}
