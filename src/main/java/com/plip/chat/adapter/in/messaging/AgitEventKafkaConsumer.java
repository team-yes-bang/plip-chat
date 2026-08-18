package com.plip.chat.adapter.in.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plip.chat.application.port.in.ProjectAgitEventUseCase;
import com.plip.chat.domain.model.AgitMemberRole;
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
public class AgitEventKafkaConsumer {

	private final ObjectMapper objectMapper;
	private final ProjectAgitEventUseCase projectAgitEventUseCase;

	@KafkaListener(
			topics = {
					AgitEventTopics.CREATED,
					AgitEventTopics.UPDATED,
					AgitEventTopics.MEMBER_JOINED,
					AgitEventTopics.MEMBER_LEFT,
					AgitEventTopics.MEMBER_BANNED,
					AgitEventTopics.MEMBER_UNBANNED,
					AgitEventTopics.MEMBER_PROFILE_UPDATED,
					AgitEventTopics.HOST_TRANSFERRED,
					AgitEventTopics.DELETED
			},
			groupId = AgitEventTopics.CONSUMER_GROUP
	)
	public void consume(String payload, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
		try {
			JsonNode node = objectMapper.readTree(payload);
			UUID agitUuid = requiredUuid(node, "agitUuid");
			switch (topic) {
				case AgitEventTopics.CREATED -> projectAgitEventUseCase.onCreated(
						agitUuid,
						text(node, "agitName"),
						text(node, "description"),
						intValue(node, "maximumCapacity"),
						text(node, "thumbnailPath"),
						requiredUuid(node, "hostUserUuid"),
						text(node, "hostNickname")
				);
				case AgitEventTopics.UPDATED -> projectAgitEventUseCase.onUpdated(
						agitUuid,
						text(node, "agitName"),
						text(node, "description"),
						intValue(node, "maximumCapacity"),
						text(node, "thumbnailPath")
				);
				case AgitEventTopics.MEMBER_JOINED -> projectAgitEventUseCase.onMemberJoined(
						agitUuid,
						requiredUuid(node, "userUuid"),
						text(node, "nickname"),
						text(node, "profileImagePath"),
						parseRole(text(node, "role"))
				);
				case AgitEventTopics.MEMBER_LEFT -> projectAgitEventUseCase.onMemberLeft(
						agitUuid,
						requiredUuid(node, "userUuid")
				);
				case AgitEventTopics.MEMBER_BANNED -> projectAgitEventUseCase.onMemberBanned(
						agitUuid,
						requiredUuid(node, "userUuid"),
						text(node, "nickname")
				);
				case AgitEventTopics.MEMBER_UNBANNED -> projectAgitEventUseCase.onMemberUnbanned(
						agitUuid,
						requiredUuid(node, "userUuid")
				);
				case AgitEventTopics.MEMBER_PROFILE_UPDATED -> projectAgitEventUseCase.onMemberProfileUpdated(
						agitUuid,
						requiredUuid(node, "userUuid"),
						text(node, "nickname"),
						text(node, "profileImagePath")
				);
				case AgitEventTopics.HOST_TRANSFERRED -> projectAgitEventUseCase.onHostTransferred(
						agitUuid,
						requiredUuid(node, "previousHostUserUuid"),
						requiredUuid(node, "newHostUserUuid"),
						text(node, "newHostNickname")
				);
				case AgitEventTopics.DELETED -> projectAgitEventUseCase.onDeleted(agitUuid);
				default -> log.warn("아지트 이벤트 skip: 알 수 없는 토픽 {}", topic);
			}
		} catch (IllegalArgumentException e) {
			log.warn("아지트 읽기 모델 투영 skip: {}", e.getMessage());
		} catch (Exception e) {
			log.warn("아지트 읽기 모델 투영 실패: {}", e.getMessage());
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

	private static int intValue(JsonNode node, String field) {
		JsonNode value = node.get(field);
		if (value == null || value.isNull() || !value.isNumber()) {
			return 0;
		}
		return value.asInt();
	}

	private static AgitMemberRole parseRole(String role) {
		if (role == null || role.isBlank()) {
			return AgitMemberRole.GUEST;
		}
		try {
			return AgitMemberRole.valueOf(role);
		} catch (IllegalArgumentException e) {
			return AgitMemberRole.GUEST;
		}
	}
}
