package com.plip.chat.domain.model;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChatMessageTest {

	@Test
	void talk_assignsUuidV7AndTalkType() {
		UUID agitUuid = UUID.randomUUID();
		UUID senderUuid = UUID.randomUUID();

		ChatMessage message = ChatMessage.talk(agitUuid, senderUuid, "hello");

		assertThat(message.getId()).isNotNull();
		assertThat(message.getAgitUuid()).isEqualTo(agitUuid);
		assertThat(message.getSenderUuid()).isEqualTo(senderUuid);
		assertThat(message.getType()).isEqualTo(MessageType.TALK);
		assertThat(message.getCreatedAt()).isNotNull();
		assertThat(message.getPayload()).isEmpty();
	}

	@Test
	void system_keepsSenderNull() {
		ChatMessage message = ChatMessage.system(
				UUID.randomUUID(),
				"입장",
				Map.of("eventType", "agit.member-joined")
		);

		assertThat(message.getType()).isEqualTo(MessageType.SYSTEM);
		assertThat(message.getSenderUuid()).isNull();
		assertThat(message.getPayload()).containsEntry("eventType", "agit.member-joined");
	}

	@Test
	void talk_requiresSenderUuid() {
		assertThatThrownBy(() -> ChatMessage.talk(UUID.randomUUID(), null, "hello"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("senderUuid는 필수입니다.");
	}
}
