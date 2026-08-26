package com.plip.chat.application.service;

import com.plip.chat.application.exception.ChatAccessDeniedException;
import com.plip.chat.application.port.in.dto.ChatHistoryResult;
import com.plip.chat.domain.event.MemberReadUpdated;
import com.plip.chat.domain.model.AgitRoomReference;
import com.plip.chat.domain.model.ChatMessage;
import com.plip.chat.domain.model.MessageType;
import com.plip.chat.support.InMemoryAgitReferencePersistence;
import com.plip.chat.support.InMemoryChatMessagePersistence;
import com.plip.chat.support.TestChatReceiptConfig;
import com.plip.chat.support.TestChatStateConfig.InMemoryChatStatePort;
import com.plip.chat.support.TestMemberReadEventConfig.InMemoryMemberReadEventPort;
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
	private InMemoryMemberReadEventPort memberReadEventPort;
	private TestChatReceiptConfig.InMemoryChatReceiptPort chatReceiptPort;
	private ReadReceiptProjector readReceiptProjector;
	private UnreadMemberCountCalculator unreadMemberCountCalculator;
	private ChatQueryService chatQueryService;

	private UUID agitUuid;
	private UUID userUuid;

	@BeforeEach
	void setUp() {
		agitStore = new InMemoryAgitReferencePersistence();
		messageStore = new InMemoryChatMessagePersistence();
		chatStatePort = new InMemoryChatStatePort();
		memberReadEventPort = new InMemoryMemberReadEventPort();
		chatReceiptPort = new TestChatReceiptConfig.InMemoryChatReceiptPort();
		unreadMemberCountCalculator = new UnreadMemberCountCalculator(agitStore, chatStatePort);
		readReceiptProjector = new ReadReceiptProjector(
				messageStore,
				chatReceiptPort,
				new TestChatReceiptConfig.InMemoryChatReceiptBroadcastPort(),
				unreadMemberCountCalculator
		);
		chatQueryService = new ChatQueryService(
				agitStore,
				messageStore,
				chatStatePort,
				memberReadEventPort,
				readReceiptProjector,
				unreadMemberCountCalculator
		);
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

		chatQueryService.markRead(agitUuid, userUuid, null);

		assertThat(chatStatePort.getReadAt(userUuid, agitUuid)).isPresent();
		assertThat(chatStatePort.getMemberReadAt(agitUuid, userUuid)).isNotEmpty();
		assertThat(memberReadEventPort.getPublished()).hasSize(1);
	}

	@Test
	void markRead_usesProvidedReadAt() {
		seedActiveMember();
		Instant readAt = Instant.parse("2026-08-18T04:00:00Z");

		chatQueryService.markRead(agitUuid, userUuid, readAt);

		assertThat(chatStatePort.getReadAt(userUuid, agitUuid)).contains(readAt);
		assertThat(memberReadEventPort.getPublished())
				.containsExactly(new MemberReadUpdated(agitUuid, userUuid, readAt, null));
	}

	@Test
	void markRead_isMonotonicAndIdempotent() {
		seedActiveMember();
		Instant first = Instant.parse("2026-08-18T04:00:00Z");
		Instant second = Instant.parse("2026-08-18T05:00:00Z");
		Instant older = Instant.parse("2026-08-18T03:00:00Z");

		chatQueryService.markRead(agitUuid, userUuid, first);
		chatQueryService.markRead(agitUuid, userUuid, second);
		chatQueryService.markRead(agitUuid, userUuid, older);
		chatQueryService.markRead(agitUuid, userUuid, second);

		assertThat(chatStatePort.getReadAt(userUuid, agitUuid)).contains(second);
		assertThat(memberReadEventPort.getPublished()).hasSize(2);
	}

	@Test
	void markRead_decrementsUnreadMemberCountForNewlyReadMessages() {
		UUID senderUuid = UUID.randomUUID();
		UUID readerUuid = UUID.randomUUID();
		seedActiveMember();
		agitStore.save(AgitRoomReference.reconstitute(
				agitUuid,
				"아지트",
				"",
				5,
				null,
				com.plip.chat.domain.model.AgitRoomStatus.ACTIVE,
				java.util.List.of(
						com.plip.chat.domain.model.AgitMemberReference.of(
								senderUuid,
								"보낸이",
								null,
								com.plip.chat.domain.model.AgitMemberRole.HOST,
								com.plip.chat.domain.model.AgitMemberStatus.ACTIVE
						),
						com.plip.chat.domain.model.AgitMemberReference.of(
								readerUuid,
								"읽은이",
								null,
								com.plip.chat.domain.model.AgitMemberRole.GUEST,
								com.plip.chat.domain.model.AgitMemberStatus.ACTIVE
						),
						com.plip.chat.domain.model.AgitMemberReference.of(
								UUID.randomUUID(),
								"멤버1",
								null,
								com.plip.chat.domain.model.AgitMemberRole.GUEST,
								com.plip.chat.domain.model.AgitMemberStatus.ACTIVE
						),
						com.plip.chat.domain.model.AgitMemberReference.of(
								UUID.randomUUID(),
								"멤버2",
								null,
								com.plip.chat.domain.model.AgitMemberRole.GUEST,
								com.plip.chat.domain.model.AgitMemberStatus.ACTIVE
						)
				),
				Instant.parse("2026-08-18T00:00:00Z")
		));
		ChatMessage message = messageStore.save(ChatMessage.reconstitute(
				UUID.randomUUID(),
				agitUuid,
				senderUuid,
				MessageType.TALK,
				"hello",
				Map.of(),
				Instant.parse("2026-08-18T02:00:00Z")
		));
		chatReceiptPort.initUnreadMemberCount(agitUuid, message.getId(), 3);

		chatQueryService.markRead(agitUuid, readerUuid, Instant.parse("2026-08-18T03:00:00Z"));

		assertThat(chatReceiptPort.getUnreadMemberCount(agitUuid, message.getId())).hasValue(2);
	}

	@Test
	void markRead_projectsReceiptsEvenWhenReadAtDoesNotAdvance() {
		UUID senderUuid = UUID.randomUUID();
		UUID readerUuid = UUID.randomUUID();
		agitStore.save(AgitRoomReference.reconstitute(
				agitUuid,
				"아지트",
				"",
				5,
				null,
				com.plip.chat.domain.model.AgitRoomStatus.ACTIVE,
				java.util.List.of(
						com.plip.chat.domain.model.AgitMemberReference.of(
								senderUuid,
								"보낸이",
								null,
								com.plip.chat.domain.model.AgitMemberRole.HOST,
								com.plip.chat.domain.model.AgitMemberStatus.ACTIVE
						),
						com.plip.chat.domain.model.AgitMemberReference.of(
								readerUuid,
								"읽은이",
								null,
								com.plip.chat.domain.model.AgitMemberRole.GUEST,
								com.plip.chat.domain.model.AgitMemberStatus.ACTIVE
						)
				),
				Instant.parse("2026-08-18T00:00:00Z")
		));
		ChatMessage message = messageStore.save(ChatMessage.reconstitute(
				UUID.randomUUID(),
				agitUuid,
				senderUuid,
				MessageType.TALK,
				"hello",
				Map.of(),
				Instant.parse("2026-08-18T02:00:00Z")
		));
		Instant readAt = Instant.parse("2026-08-18T03:00:00Z");
		chatStatePort.markRead(readerUuid, agitUuid, readAt);
		chatReceiptPort.initUnreadMemberCount(agitUuid, message.getId(), 1);

		chatQueryService.markRead(agitUuid, readerUuid, Instant.parse("2026-08-18T01:00:00Z"));

		assertThat(chatReceiptPort.getUnreadMemberCount(agitUuid, message.getId())).hasValue(0);
		assertThat(memberReadEventPort.getPublished()).hasSize(1);
	}

	@Test
	void markRead_rejectsNonActiveMember() {
		assertThatThrownBy(() -> chatQueryService.markRead(agitUuid, userUuid, null))
				.isInstanceOf(ChatAccessDeniedException.class);
		assertThat(chatStatePort.getReadAt(userUuid, agitUuid)).isEmpty();
		assertThat(memberReadEventPort.getPublished()).isEmpty();
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
