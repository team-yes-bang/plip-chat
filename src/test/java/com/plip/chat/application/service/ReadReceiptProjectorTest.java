package com.plip.chat.application.service;

import com.plip.chat.domain.event.MemberReadUpdated;
import com.plip.chat.domain.model.AgitMemberReference;
import com.plip.chat.domain.model.AgitMemberRole;
import com.plip.chat.domain.model.AgitMemberStatus;
import com.plip.chat.domain.model.AgitRoomReference;
import com.plip.chat.domain.model.ChatMessage;
import com.plip.chat.domain.model.MessageType;
import com.plip.chat.support.InMemoryAgitReferencePersistence;
import com.plip.chat.support.InMemoryChatMessagePersistence;
import com.plip.chat.support.TestChatReceiptConfig;
import com.plip.chat.support.TestChatStateConfig.InMemoryChatStatePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReadReceiptProjectorTest {

	private InMemoryAgitReferencePersistence agitStore;
	private InMemoryChatMessagePersistence messageStore;
	private InMemoryChatStatePort chatStatePort;
	private TestChatReceiptConfig.InMemoryChatReceiptPort chatReceiptPort;
	private TestChatReceiptConfig.InMemoryChatReceiptBroadcastPort chatReceiptBroadcastPort;
	private ReadReceiptProjector readReceiptProjector;

	private UUID agitUuid;
	private UUID senderUuid;
	private UUID readerUuid;

	@BeforeEach
	void setUp() {
		agitStore = new InMemoryAgitReferencePersistence();
		messageStore = new InMemoryChatMessagePersistence();
		chatStatePort = new InMemoryChatStatePort();
		chatReceiptPort = new TestChatReceiptConfig.InMemoryChatReceiptPort();
		chatReceiptBroadcastPort = new TestChatReceiptConfig.InMemoryChatReceiptBroadcastPort();
		UnreadMemberCountCalculator unreadMemberCountCalculator = new UnreadMemberCountCalculator(
				agitStore,
				chatStatePort
		);
		readReceiptProjector = new ReadReceiptProjector(
				messageStore,
				chatReceiptPort,
				chatReceiptBroadcastPort,
				unreadMemberCountCalculator
		);
		agitUuid = UUID.randomUUID();
		senderUuid = UUID.randomUUID();
		readerUuid = UUID.randomUUID();
		seedRoom();
	}

	@Test
	void onMemberReadUpdated_recomputesReceiptAndBroadcasts() {
		ChatMessage message = messageStore.save(talkAt("2026-08-18T02:00:00Z", "hello"));
		chatReceiptPort.initUnreadMemberCount(agitUuid, message.getId(), 2);
		chatStatePort.markRead(readerUuid, agitUuid, Instant.parse("2026-08-18T03:00:00Z"));

		readReceiptProjector.onMemberReadUpdated(new MemberReadUpdated(
				agitUuid,
				readerUuid,
				Instant.parse("2026-08-18T03:00:00Z"),
				Instant.parse("2026-08-18T01:00:00Z")
		));

		assertThat(chatReceiptPort.getUnreadMemberCount(agitUuid, message.getId())).hasValue(1);
		assertThat(chatReceiptBroadcastPort.getPublished()).containsExactly(
				new TestChatReceiptConfig.InMemoryChatReceiptBroadcastPort.PublishedReceipt(agitUuid, message.getId(), 1)
		);
	}

	private void seedRoom() {
		AgitRoomReference room = AgitRoomReference.empty(agitUuid)
				.upsertMember(AgitMemberReference.of(
						senderUuid,
						"보낸이",
						null,
						AgitMemberRole.HOST,
						AgitMemberStatus.ACTIVE
				))
				.upsertMember(AgitMemberReference.of(
						readerUuid,
						"읽은이",
						null,
						AgitMemberRole.GUEST,
						AgitMemberStatus.ACTIVE
				))
				.upsertMember(AgitMemberReference.of(
						UUID.randomUUID(),
						"멤버",
						null,
						AgitMemberRole.GUEST,
						AgitMemberStatus.ACTIVE
				));
		agitStore.save(room);
	}

	private ChatMessage talkAt(String createdAt, String content) {
		return ChatMessage.reconstitute(
				UUID.randomUUID(),
				agitUuid,
				senderUuid,
				MessageType.TALK,
				content,
				Map.of(),
				Instant.parse(createdAt)
		);
	}
}
