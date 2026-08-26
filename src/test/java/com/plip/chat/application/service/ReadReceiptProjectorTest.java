package com.plip.chat.application.service;

import com.plip.chat.domain.event.MemberReadUpdated;
import com.plip.chat.domain.model.ChatMessage;
import com.plip.chat.domain.model.MessageType;
import com.plip.chat.support.InMemoryChatMessagePersistence;
import com.plip.chat.support.TestChatReceiptConfig.InMemoryChatReceiptBroadcastPort;
import com.plip.chat.support.TestChatReceiptConfig.InMemoryChatReceiptPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReadReceiptProjectorTest {

	private InMemoryChatMessagePersistence messageStore;
	private InMemoryChatReceiptPort chatReceiptPort;
	private InMemoryChatReceiptBroadcastPort chatReceiptBroadcastPort;
	private ReadReceiptProjector readReceiptProjector;

	private UUID agitUuid;
	private UUID senderUuid;
	private UUID readerUuid;

	@BeforeEach
	void setUp() {
		messageStore = new InMemoryChatMessagePersistence();
		chatReceiptPort = new InMemoryChatReceiptPort();
		chatReceiptBroadcastPort = new InMemoryChatReceiptBroadcastPort();
		readReceiptProjector = new ReadReceiptProjector(messageStore, chatReceiptPort, chatReceiptBroadcastPort);
		agitUuid = UUID.randomUUID();
		senderUuid = UUID.randomUUID();
		readerUuid = UUID.randomUUID();
	}

	@Test
	void onMemberReadUpdated_decrementsReceiptAndBroadcasts() {
		ChatMessage message = messageStore.save(talkAt("2026-08-18T02:00:00Z", "hello"));
		chatReceiptPort.initUnreadMemberCount(agitUuid, message.getId(), 2);

		readReceiptProjector.onMemberReadUpdated(new MemberReadUpdated(
				agitUuid,
				readerUuid,
				Instant.parse("2026-08-18T03:00:00Z"),
				Instant.parse("2026-08-18T01:00:00Z")
		));

		assertThat(chatReceiptPort.getUnreadMemberCount(agitUuid, message.getId())).hasValue(1);
		assertThat(chatReceiptBroadcastPort.getPublished()).containsExactly(
				new InMemoryChatReceiptBroadcastPort.PublishedReceipt(agitUuid, message.getId(), 1)
		);
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
