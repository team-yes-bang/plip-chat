package com.plip.chat.application.service;

import com.plip.chat.application.exception.ChatAccessDeniedException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChatCommandServiceTest {

	private InMemoryAgitReferencePersistence agitStore;
	private InMemoryChatMessagePersistence messageStore;
	private InMemoryChatStatePort chatStatePort;
	private InMemoryChatBroadcastPort chatBroadcastPort;
	private ChatCommandService chatCommandService;

	private UUID agitUuid;
	private UUID userUuid;

	@BeforeEach
	void setUp() {
		agitStore = new InMemoryAgitReferencePersistence();
		messageStore = new InMemoryChatMessagePersistence();
		chatStatePort = new InMemoryChatStatePort();
		chatBroadcastPort = new InMemoryChatBroadcastPort();
		chatCommandService = new ChatCommandService(
				agitStore,
				messageStore,
				chatStatePort,
				chatBroadcastPort
		);
		agitUuid = UUID.randomUUID();
		userUuid = UUID.randomUUID();
	}

	@Test
	void sendTalk_savesBroadcastsAndUpdatesLastChatAt() {
		agitStore.save(AgitRoomReference.create(agitUuid, "아지트", "", 5, null, userUuid, "호스트"));

		ChatMessage saved = chatCommandService.sendTalk(agitUuid, userUuid, "안녕");

		assertThat(saved.getType()).isEqualTo(MessageType.TALK);
		assertThat(saved.getContent()).isEqualTo("안녕");
		assertThat(messageStore.findByAgitUuidOrderByCreatedAtDesc(agitUuid)).containsExactly(saved);
		assertThat(chatStatePort.getLastChatAt(agitUuid)).isEqualTo(saved.getCreatedAt());
		assertThat(chatBroadcastPort.getPublished()).containsExactly(saved);
	}

	@Test
	void sendTalk_rejectsNonActiveMember() {
		assertThatThrownBy(() -> chatCommandService.sendTalk(agitUuid, userUuid, "안녕"))
				.isInstanceOf(ChatAccessDeniedException.class);
		assertThat(messageStore.findByAgitUuidOrderByCreatedAtDesc(agitUuid)).isEmpty();
		assertThat(chatBroadcastPort.getPublished()).isEmpty();
		assertThat(chatStatePort.getLastChatAt(agitUuid)).isNull();
	}
}
