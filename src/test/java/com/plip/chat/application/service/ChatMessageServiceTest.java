package com.plip.chat.application.service;

import com.plip.chat.application.port.out.ChatMessagePersistencePort;
import com.plip.chat.domain.model.ChatMessage;
import com.plip.chat.domain.model.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChatMessageServiceTest {

	private ChatMessageService chatMessageService;

	@BeforeEach
	void setUp() {
		chatMessageService = new ChatMessageService(new InMemoryChatMessagePersistencePort());
	}

	@Test
	void saveAndListByAgitUuid_returnsNewestFirst() {
		UUID agitUuid = UUID.randomUUID();
		UUID otherAgitUuid = UUID.randomUUID();
		ChatMessage older = talkAt(agitUuid, Instant.parse("2026-08-18T01:00:00Z"), "이전 메시지");
		ChatMessage newer = talkAt(agitUuid, Instant.parse("2026-08-18T02:00:00Z"), "최근 메시지");
		ChatMessage otherRoom = talkAt(otherAgitUuid, Instant.parse("2026-08-18T03:00:00Z"), "다른 방");

		chatMessageService.save(older);
		chatMessageService.save(newer);
		chatMessageService.save(otherRoom);

		List<ChatMessage> messages = chatMessageService.listByAgitUuid(agitUuid);

		assertThat(messages).extracting(ChatMessage::getId)
				.containsExactly(newer.getId(), older.getId());
		assertThat(messages).extracting(ChatMessage::getContent)
				.containsExactly("최근 메시지", "이전 메시지");
	}

	@Test
	void save_persistsSystemMessage() {
		UUID agitUuid = UUID.randomUUID();
		ChatMessage system = ChatMessage.system(
				agitUuid,
				"멤버가 입장했습니다.",
				Map.of("eventType", "agit.member-joined")
		);

		ChatMessage saved = chatMessageService.save(system);

		assertThat(saved.getType()).isEqualTo(MessageType.SYSTEM);
		assertThat(saved.getSenderUuid()).isNull();
		assertThat(chatMessageService.listByAgitUuid(agitUuid)).containsExactly(saved);
	}

	@Test
	void save_requiresChatMessage() {
		assertThatThrownBy(() -> chatMessageService.save(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("chatMessage는 필수입니다.");
	}

	@Test
	void listByAgitUuid_requiresAgitUuid() {
		assertThatThrownBy(() -> chatMessageService.listByAgitUuid(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("agitUuid는 필수입니다.");
	}

	private static ChatMessage talkAt(UUID agitUuid, Instant createdAt, String content) {
		return ChatMessage.reconstitute(
				UUID.randomUUID(),
				agitUuid,
				UUID.randomUUID(),
				MessageType.TALK,
				content,
				Map.of(),
				createdAt
		);
	}

	private static final class InMemoryChatMessagePersistencePort implements ChatMessagePersistencePort {

		private final List<ChatMessage> store = new ArrayList<>();

		@Override
		public ChatMessage save(ChatMessage chatMessage) {
			store.add(chatMessage);
			return chatMessage;
		}

		@Override
		public List<ChatMessage> findByAgitUuidOrderByCreatedAtDesc(UUID agitUuid) {
			return store.stream()
					.filter(message -> message.getAgitUuid().equals(agitUuid))
					.sorted(Comparator.comparing(ChatMessage::getCreatedAt).reversed())
					.toList();
		}
	}
}
