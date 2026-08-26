package com.plip.chat.adapter.in.web;

import com.plip.chat.domain.model.AgitMemberReference;
import com.plip.chat.domain.model.AgitMemberRole;
import com.plip.chat.domain.model.AgitMemberStatus;
import com.plip.chat.domain.model.AgitRoomReference;
import com.plip.chat.domain.model.AgitRoomStatus;
import com.plip.chat.domain.model.ChatMessage;
import com.plip.chat.domain.model.MessageType;
import com.plip.chat.support.InMemoryAgitReferencePersistence;
import com.plip.chat.support.InMemoryChatMessagePersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MeChatControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private InMemoryAgitReferencePersistence agitStore;

	@Autowired
	private InMemoryChatMessagePersistence messageStore;

	private UUID agitUuid;
	private UUID userUuid;
	private UUID otherUuid;

	@BeforeEach
	void setUp() {
		agitUuid = UUID.randomUUID();
		userUuid = UUID.randomUUID();
		otherUuid = UUID.randomUUID();
		agitStore.save(AgitRoomReference.reconstitute(
				agitUuid,
				"아지트",
				"",
				5,
				null,
				AgitRoomStatus.ACTIVE,
				List.of(
						AgitMemberReference.of(userUuid, "me", null, AgitMemberRole.HOST, AgitMemberStatus.ACTIVE),
						AgitMemberReference.of(otherUuid, "other", null, AgitMemberRole.GUEST, AgitMemberStatus.ACTIVE)
				),
				Instant.parse("2026-08-18T00:00:00Z")
		));
		messageStore.save(talkAt(otherUuid, "2026-08-18T03:00:00Z", "new"));
	}

	@Test
	void getMyAgitsChatUnread_returnsBatchCounts() throws Exception {
		mockMvc.perform(get("/api/v1/me/agits/chat-unread")
						.header(MeChatController.USER_UUID_HEADER, userUuid.toString())
						.param("agitUuids", agitUuid.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items.length()").value(1))
				.andExpect(jsonPath("$.items[0].agitUuid").value(agitUuid.toString()))
				.andExpect(jsonPath("$.items[0].unreadMessageCount").value(1));
	}

	@Test
	void getMyAgitsChatUnread_unauthorizedWithoutHeader() throws Exception {
		mockMvc.perform(get("/api/v1/me/agits/chat-unread")
						.param("agitUuids", agitUuid.toString()))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
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
