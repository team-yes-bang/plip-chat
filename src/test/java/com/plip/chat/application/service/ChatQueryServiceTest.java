package com.plip.chat.application.service;

import com.plip.chat.application.exception.ChatAccessDeniedException;
import com.plip.chat.application.port.in.dto.ChatHistoryResult;
import com.plip.chat.domain.model.AgitRoomReference;
import com.plip.chat.domain.model.ChatMessage;
import com.plip.chat.domain.model.MessageType;
import com.plip.chat.support.InMemoryAgitReferencePersistence;
import com.plip.chat.support.InMemoryChatMessagePersistence;
import com.plip.chat.support.TestChatStateConfig.InMemoryChatStatePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChatQueryServiceTest {

	private InMemoryAgitReferencePersistence agitStore;
	private InMemoryChatMessagePersistence messageStore;
	private InMemoryChatStatePort chatStatePort;
	private ChatQueryService chatQueryService;

	private UUID agitUuid;
	private UUID userUuid;

	@BeforeEach
	void setUp() {
		agitStore = new InMemoryAgitReferencePersistence();
		messageStore = new InMemoryChatMessagePersistence();
		chatStatePort = new InMemoryChatStatePort();
		chatQueryService = new ChatQueryService(agitStore, messageStore, chatStatePort);
		agitUuid = UUID.randomUUID();
		userUuid = UUID.randomUUID();
	}

	@Test
	void getHistory_returnsNewestFirstWithCursor() {
		seedActiveMember();
		ChatMessage first = talkAt("2026-08-18T01:00:00Z", "1");
		ChatMessage second = talkAt("2026-08-18T02:00:00Z", "2");
		ChatMessage third = talkAt("2026-08-18T03:00:00Z", "3");
		messageStore.save(first);
		messageStore.save(second);
		messageStore.save(third);

		ChatHistoryResult page = chatQueryService.getHistory(agitUuid, userUuid, null, null, 2);

		assertThat(page.getMessages()).extracting(ChatMessage::getContent).containsExactly("3", "2");
		assertThat(page.isHasNext()).isTrue();
		assertThat(page.getNextCursorId()).isEqualTo(second.getId());

		ChatHistoryResult next = chatQueryService.getHistory(
				agitUuid,
				userUuid,
				page.getNextCursorCreatedAt(),
				page.getNextCursorId(),
				2
		);
		assertThat(next.getMessages()).extracting(ChatMessage::getContent).containsExactly("1");
		assertThat(next.isHasNext()).isFalse();
	}

	@Test
	void getHistory_rejectsNonActiveMember() {
		assertThatThrownBy(() -> chatQueryService.getHistory(agitUuid, userUuid, null, null, 20))
				.isInstanceOf(ChatAccessDeniedException.class);
	}

	@Test
	void markRead_storesReadStateForActiveMember() {
		seedActiveMember();

		chatQueryService.markRead(agitUuid, userUuid);

		assertThat(chatStatePort.get(userUuid, agitUuid)).isNotNull();
	}

	@Test
	void markRead_rejectsNonActiveMember() {
		assertThatThrownBy(() -> chatQueryService.markRead(agitUuid, userUuid))
				.isInstanceOf(ChatAccessDeniedException.class);
		assertThat(chatStatePort.get(userUuid, agitUuid)).isNull();
	}

	private void seedActiveMember() {
		agitStore.save(AgitRoomReference.create(agitUuid, "아지트", "", 5, null, userUuid, "호스트"));
	}

	private ChatMessage talkAt(String createdAt, String content) {
		return ChatMessage.reconstitute(
				UUID.randomUUID(),
				agitUuid,
				userUuid,
				MessageType.TALK,
				content,
				Map.of(),
				Instant.parse(createdAt)
		);
	}
}
