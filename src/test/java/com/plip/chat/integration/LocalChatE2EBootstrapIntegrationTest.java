package com.plip.chat.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plip.chat.adapter.in.messaging.AgitEventKafkaConsumer;
import com.plip.chat.adapter.in.messaging.AgitEventTopics;
import com.plip.chat.application.port.in.SendMessageUseCase;
import com.plip.chat.application.port.out.AgitReferenceQueryPort;
import com.plip.chat.application.service.AgitReferenceService;
import com.plip.chat.domain.model.ChatMessage;
import com.plip.chat.support.InMemoryChatMessagePersistence;
import com.plip.chat.support.TestChatBroadcastConfig.InMemoryChatBroadcastPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class LocalChatE2EBootstrapIntegrationTest {

	@Autowired
	private AgitReferenceService agitReferenceService;

	@Autowired
	private AgitReferenceQueryPort agitReferenceQueryPort;

	@Autowired
	private SendMessageUseCase sendMessageUseCase;

	@Autowired
	private InMemoryChatMessagePersistence chatMessageStore;

	@Autowired
	private InMemoryChatBroadcastPort chatBroadcastPort;

	private AgitEventKafkaConsumer agitEventKafkaConsumer;

	private UUID agitUuid;
	private UUID hostUuid;
	private UUID guestUuid;

	@BeforeEach
	void setUp() {
		chatMessageStore.clear();
		chatBroadcastPort.clear();
		agitEventKafkaConsumer = new AgitEventKafkaConsumer(new ObjectMapper(), agitReferenceService);
		agitUuid = UUID.fromString("018f3f6e-8e2a-7b3c-9d4e-5f6a7b8c9d0e");
		hostUuid = UUID.fromString("01912345-6789-7abc-def0-123456789abc");
		guestUuid = UUID.fromString("01923456-789a-8bcd-ef01-234567890abc");
		bootstrapReadModel();
	}

	@Test
	void kafkaBootstrap_enablesBidirectionalTalkSend() {
		assertThat(agitReferenceQueryPort.isActiveMember(agitUuid, hostUuid)).isTrue();
		assertThat(agitReferenceQueryPort.isActiveMember(agitUuid, guestUuid)).isTrue();

		ChatMessage fromHost = sendMessageUseCase.sendTalk(agitUuid, hostUuid, "호스트 메시지");
		ChatMessage fromGuest = sendMessageUseCase.sendTalk(agitUuid, guestUuid, "게스트 메시지");

		List<ChatMessage> history = chatMessageStore.findByAgitUuidOrderByCreatedAtDesc(agitUuid);
		assertThat(history).hasSize(2);
		assertThat(history).extracting(ChatMessage::getContent)
				.containsExactlyInAnyOrder("호스트 메시지", "게스트 메시지");

		List<ChatMessage> published = chatBroadcastPort.getPublishedMessages();
		assertThat(published).hasSize(2);
		assertThat(published.get(0).getContent()).isEqualTo("호스트 메시지");
		assertThat(published.get(1).getContent()).isEqualTo("게스트 메시지");
		assertThat(fromHost.getContent()).isEqualTo("호스트 메시지");
		assertThat(fromGuest.getContent()).isEqualTo("게스트 메시지");
	}

	private void bootstrapReadModel() {
		String createdPayload = """
				{
				  "agitUuid": "%s",
				  "agitName": "주말 보드게임",
				  "description": "가볍게 즐겨요",
				  "maximumCapacity": 5,
				  "hostUserUuid": "%s",
				  "hostNickname": "보드왕"
				}
				""".formatted(agitUuid, hostUuid);
		agitEventKafkaConsumer.consume(createdPayload, AgitEventTopics.CREATED);

		String joinedPayload = """
				{
				  "agitUuid": "%s",
				  "userUuid": "%s",
				  "nickname": "게스트",
				  "profileImagePath": "profiles/g.png",
				  "role": "GUEST"
				}
				""".formatted(agitUuid, guestUuid);
		agitEventKafkaConsumer.consume(joinedPayload, AgitEventTopics.MEMBER_JOINED);
	}
}
