package com.plip.chat.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plip.chat.application.port.in.HandleSystemMessageUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SystemMessageKafkaConsumerTest {

	@Mock
	private HandleSystemMessageUseCase handleSystemMessageUseCase;

	private SystemMessageKafkaConsumer consumer;

	@BeforeEach
	void setUp() {
		consumer = new SystemMessageKafkaConsumer(new ObjectMapper(), handleSystemMessageUseCase);
	}

	@Test
	void consume_memberJoined_delegatesToUseCase() {
		UUID agitUuid = UUID.randomUUID();
		UUID userUuid = UUID.randomUUID();
		String payload = """
				{
				  "agitUuid": "%s",
				  "userUuid": "%s",
				  "nickname": "게스트"
				}
				""".formatted(agitUuid, userUuid);

		consumer.consume(payload, AgitEventTopics.MEMBER_JOINED);

		verify(handleSystemMessageUseCase).onMemberJoined(agitUuid, userUuid, "게스트");
	}

	@Test
	void consume_memberBanned_delegatesToUseCase() {
		UUID agitUuid = UUID.randomUUID();
		UUID userUuid = UUID.randomUUID();
		String payload = """
				{
				  "agitUuid": "%s",
				  "userUuid": "%s",
				  "nickname": "게스트"
				}
				""".formatted(agitUuid, userUuid);

		consumer.consume(payload, AgitEventTopics.MEMBER_BANNED);

		verify(handleSystemMessageUseCase).onMemberBanned(agitUuid, userUuid, "게스트");
	}

	@Test
	void consume_topicBound_delegatesToUseCase() {
		UUID agitUuid = UUID.randomUUID();
		String payload = """
				{
				  "agitUuid": "%s",
				  "topicId": "topic-1"
				}
				""".formatted(agitUuid);

		consumer.consume(payload, TopicEventTopics.BOUND);

		verify(handleSystemMessageUseCase).onTopicBound(agitUuid, "topic-1");
	}

	@Test
	void consume_topicStarted_delegatesToUseCase() {
		UUID agitUuid = UUID.randomUUID();
		String payload = """
				{
				  "agitUuid": "%s",
				  "topicId": "topic-1",
				  "startedAt": "2026-08-14T04:00:00Z"
				}
				""".formatted(agitUuid);

		consumer.consume(payload, TopicEventTopics.STARTED);

		verify(handleSystemMessageUseCase).onTopicStarted(agitUuid, "topic-1");
	}

	@Test
	void consume_skipsWhenAgitUuidMissing() {
		consumer.consume("{\"nickname\":\"게스트\"}", AgitEventTopics.MEMBER_JOINED);

		verifyNoInteractions(handleSystemMessageUseCase);
	}
}
