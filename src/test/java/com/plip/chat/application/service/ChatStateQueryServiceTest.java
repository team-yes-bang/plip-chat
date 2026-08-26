package com.plip.chat.application.service;

import com.plip.chat.application.exception.ChatAccessDeniedException;
import com.plip.chat.application.port.in.dto.AgitChatUnreadResult;
import com.plip.chat.application.port.in.dto.ChatStateResult;
import com.plip.chat.domain.model.AgitMemberReference;
import com.plip.chat.domain.model.AgitMemberRole;
import com.plip.chat.domain.model.AgitMemberStatus;
import com.plip.chat.domain.model.AgitRoomReference;
import com.plip.chat.domain.model.ChatMessage;
import com.plip.chat.domain.model.MessageType;
import com.plip.chat.support.InMemoryAgitReferencePersistence;
import com.plip.chat.support.InMemoryChatMessagePersistence;
import com.plip.chat.support.TestChatStateConfig.InMemoryChatStatePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChatStateQueryServiceTest {

	private InMemoryAgitReferencePersistence agitStore;
	private InMemoryChatMessagePersistence messageStore;
	private InMemoryChatStatePort chatStatePort;
	private ChatStateQueryService chatStateQueryService;

	private UUID agitUuid;
	private UUID userUuid;
	private UUID otherUuid;

	@BeforeEach
	void setUp() {
		agitStore = new InMemoryAgitReferencePersistence();
		messageStore = new InMemoryChatMessagePersistence();
		chatStatePort = new InMemoryChatStatePort();
		chatStateQueryService = new ChatStateQueryService(agitStore, messageStore, chatStatePort);
		agitUuid = UUID.randomUUID();
		userUuid = UUID.randomUUID();
		otherUuid = UUID.randomUUID();
		seedRoom();
	}

	@Test
	void getChatState_returnsUnreadCountAndTimestamps() {
		Instant readAt = Instant.parse("2026-08-18T02:00:00Z");
		chatStatePort.markRead(userUuid, agitUuid, readAt);
		messageStore.save(talkAt(otherUuid, "2026-08-18T01:00:00Z", "old"));
		ChatMessage unread = messageStore.save(talkAt(otherUuid, "2026-08-18T03:00:00Z", "new"));
		chatStatePort.updateLastChatAt(agitUuid, unread.getCreatedAt());

		ChatStateResult result = chatStateQueryService.getChatState(agitUuid, userUuid);

		assertThat(result.getReadAt()).isEqualTo(readAt);
		assertThat(result.getLastChatAt()).isEqualTo(unread.getCreatedAt());
		assertThat(result.getUnreadMessageCount()).isEqualTo(1);
	}

	@Test
	void getMyAgitsChatUnread_returnsOnlyActiveMemberships() {
		messageStore.save(talkAt(otherUuid, "2026-08-18T03:00:00Z", "new"));
		UUID foreignAgit = UUID.randomUUID();

		List<AgitChatUnreadResult> results = chatStateQueryService.getMyAgitsChatUnread(
				userUuid,
				List.of(agitUuid, foreignAgit)
		);

		assertThat(results).containsExactly(new AgitChatUnreadResult(agitUuid, 1));
	}

	@Test
	void getChatState_rejectsNonActiveMember() {
		assertThatThrownBy(() -> chatStateQueryService.getChatState(agitUuid, UUID.randomUUID()))
				.isInstanceOf(ChatAccessDeniedException.class);
	}

	private void seedRoom() {
		AgitRoomReference room = AgitRoomReference.reconstitute(
				agitUuid,
				"아지트",
				"",
				5,
				null,
				com.plip.chat.domain.model.AgitRoomStatus.ACTIVE,
				List.of(
						AgitMemberReference.of(userUuid, "me", null, AgitMemberRole.HOST, AgitMemberStatus.ACTIVE),
						AgitMemberReference.of(otherUuid, "other", null, AgitMemberRole.GUEST, AgitMemberStatus.ACTIVE)
				),
				Instant.parse("2026-08-18T00:00:00Z")
		);
		agitStore.save(room);
	}

	private ChatMessage talkAt(UUID senderUuid, String createdAt, String content) {
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
