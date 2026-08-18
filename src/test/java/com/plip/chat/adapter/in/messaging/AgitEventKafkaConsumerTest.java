package com.plip.chat.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plip.chat.application.port.in.ProjectAgitEventUseCase;
import com.plip.chat.domain.model.AgitMemberRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AgitEventKafkaConsumerTest {

	@Mock
	private ProjectAgitEventUseCase projectAgitEventUseCase;

	private AgitEventKafkaConsumer consumer;

	@BeforeEach
	void setUp() {
		consumer = new AgitEventKafkaConsumer(new ObjectMapper(), projectAgitEventUseCase);
	}

	@Test
	void consume_created_projectsPayloadFields() {
		UUID agitUuid = UUID.fromString("018f3f6e-8e2a-7b3c-9d4e-5f6a7b8c9d0e");
		UUID hostUuid = UUID.fromString("01912345-6789-7abc-def0-123456789abc");
		String payload = """
				{
				  "agitUuid": "%s",
				  "agitName": "주말 보드게임",
				  "description": "가볍게 즐겨요",
				  "maximumCapacity": 5,
				  "hostUserUuid": "%s",
				  "hostNickname": "보드왕"
				}
				""".formatted(agitUuid, hostUuid);

		consumer.consume(payload, AgitEventTopics.CREATED);

		verify(projectAgitEventUseCase).onCreated(
				agitUuid,
				"주말 보드게임",
				"가볍게 즐겨요",
				5,
				null,
				hostUuid,
				"보드왕"
		);
	}

	@Test
	void consume_memberJoined_mapsProfileImagePath() {
		UUID agitUuid = UUID.randomUUID();
		UUID userUuid = UUID.randomUUID();
		String payload = """
				{
				  "agitUuid": "%s",
				  "userUuid": "%s",
				  "nickname": "게스트",
				  "profileImagePath": "profiles/a.png",
				  "role": "GUEST"
				}
				""".formatted(agitUuid, userUuid);

		consumer.consume(payload, AgitEventTopics.MEMBER_JOINED);

		verify(projectAgitEventUseCase).onMemberJoined(
				agitUuid,
				userUuid,
				"게스트",
				"profiles/a.png",
				AgitMemberRole.GUEST
		);
	}

	@Test
	void consume_memberUnbanned_projectsLeftConvergence() {
		UUID agitUuid = UUID.randomUUID();
		UUID userUuid = UUID.randomUUID();
		String payload = """
				{
				  "agitUuid": "%s",
				  "userUuid": "%s"
				}
				""".formatted(agitUuid, userUuid);

		consumer.consume(payload, AgitEventTopics.MEMBER_UNBANNED);

		verify(projectAgitEventUseCase).onMemberUnbanned(agitUuid, userUuid);
	}

	@Test
	void consume_skipsWhenAgitUuidMissing() {
		consumer.consume("{\"userUuid\":\"01912345-6789-7abc-def0-123456789abc\"}", AgitEventTopics.MEMBER_LEFT);

		verifyNoInteractions(projectAgitEventUseCase);
	}
}
