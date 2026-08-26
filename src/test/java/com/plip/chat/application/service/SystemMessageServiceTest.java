package com.plip.chat.application.service;

import com.plip.chat.application.port.in.SystemMessageEvents;
import com.plip.chat.domain.model.AgitMemberReference;
import com.plip.chat.domain.model.AgitMemberRole;
import com.plip.chat.domain.model.AgitMemberStatus;
import com.plip.chat.domain.model.AgitRoomReference;
import com.plip.chat.domain.model.ChatMessage;
import com.plip.chat.domain.model.MessageType;
import com.plip.chat.support.InMemoryAgitReferencePersistence;
import com.plip.chat.support.InMemoryChatMessagePersistence;
import com.plip.chat.support.TestChatBroadcastConfig.InMemoryChatBroadcastPort;
import com.plip.chat.support.TestChatStateConfig.InMemoryChatStatePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SystemMessageServiceTest {

	private InMemoryChatMessagePersistence messageStore;
	private InMemoryChatBroadcastPort chatBroadcastPort;
	private InMemoryChatStatePort chatStatePort;
	private InMemoryAgitReferencePersistence agitStore;
	private SystemMessageService systemMessageService;

	private UUID agitUuid;
	private UUID userUuid;

	@BeforeEach
	void setUp() {
		messageStore = new InMemoryChatMessagePersistence();
		chatBroadcastPort = new InMemoryChatBroadcastPort();
		chatStatePort = new InMemoryChatStatePort();
		agitStore = new InMemoryAgitReferencePersistence();
		systemMessageService = new SystemMessageService(messageStore, chatBroadcastPort, agitStore);
		agitUuid = UUID.randomUUID();
		userUuid = UUID.randomUUID();
	}

	@Test
	void onMemberJoined_savesSystemMessageAndBroadcastsWithoutLastChatAt() {
		systemMessageService.onMemberJoined(agitUuid, userUuid, "게스트");

		ChatMessage saved = messageStore.findByAgitUuidOrderByCreatedAtDesc(agitUuid).get(0);
		assertThat(saved.getType()).isEqualTo(MessageType.SYSTEM);
		assertThat(saved.getSenderUuid()).isNull();
		assertThat(saved.getContent()).isEqualTo("게스트님이 입장했습니다.");
		assertThat(saved.getPayload())
				.containsEntry("eventType", SystemMessageEvents.MEMBER_JOINED)
				.containsEntry("userUuid", userUuid.toString())
				.containsEntry("nickname", "게스트");
		assertThat(chatBroadcastPort.getPublishedMessages()).containsExactly(saved);
		assertThat(chatStatePort.getLastChatAt(agitUuid)).isEmpty();
	}

	@Test
	void onMemberBanned_savesSystemMessage() {
		systemMessageService.onMemberBanned(agitUuid, userUuid, "게스트");

		ChatMessage saved = messageStore.findByAgitUuidOrderByCreatedAtDesc(agitUuid).get(0);
		assertThat(saved.getContent()).isEqualTo("게스트님이 내보내졌습니다.");
		assertThat(saved.getPayload()).containsEntry("eventType", SystemMessageEvents.MEMBER_BANNED);
		assertThat(chatStatePort.getLastChatAt(agitUuid)).isEmpty();
	}

	@Test
	void onTopicBound_savesSystemMessage() {
		systemMessageService.onTopicBound(agitUuid, "topic-1");

		ChatMessage saved = messageStore.findByAgitUuidOrderByCreatedAtDesc(agitUuid).get(0);
		assertThat(saved.getContent()).isEqualTo("토픽이 연결되었습니다.");
		assertThat(saved.getPayload())
				.containsEntry("eventType", SystemMessageEvents.TOPIC_BOUND)
				.containsEntry("topicId", "topic-1");
		assertThat(chatBroadcastPort.getPublishedMessages()).containsExactly(saved);
		assertThat(chatStatePort.getLastChatAt(agitUuid)).isEmpty();
	}

	@Test
	void onTopicStarted_savesSystemMessage() {
		systemMessageService.onTopicStarted(agitUuid, "topic-1");

		ChatMessage saved = messageStore.findByAgitUuidOrderByCreatedAtDesc(agitUuid).get(0);
		assertThat(saved.getContent()).isEqualTo("토픽이 시작되었습니다.");
		assertThat(saved.getPayload()).containsEntry("eventType", SystemMessageEvents.TOPIC_STARTED);
		assertThat(chatStatePort.getLastChatAt(agitUuid)).isEmpty();
	}

	@Test
	void onMemberLeft_savesSystemMessageUsingReadModelNickname() {
		agitStore.save(AgitRoomReference.empty(agitUuid).upsertMember(AgitMemberReference.of(
				userUuid,
				"게스트",
				null,
				AgitMemberRole.GUEST,
				AgitMemberStatus.ACTIVE
		)));

		systemMessageService.onMemberLeft(agitUuid, userUuid);

		ChatMessage saved = messageStore.findByAgitUuidOrderByCreatedAtDesc(agitUuid).get(0);
		assertThat(saved.getContent()).isEqualTo("게스트님이 퇴장했습니다.");
		assertThat(saved.getPayload()).containsEntry("eventType", SystemMessageEvents.MEMBER_LEFT);
		assertThat(chatStatePort.getLastChatAt(agitUuid)).isEmpty();
	}
}
