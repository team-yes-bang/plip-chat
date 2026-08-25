package com.plip.chat.adapter.in.web;

import com.plip.chat.domain.model.AgitRoomReference;
import com.plip.chat.domain.model.ChatMessage;
import com.plip.chat.domain.model.MessageType;
import com.plip.chat.support.InMemoryAgitReferencePersistence;
import com.plip.chat.support.InMemoryChatMessagePersistence;
import com.plip.chat.support.TestChatStateConfig.InMemoryChatStatePort;
import com.plip.chat.support.TestMemberReadEventConfig.InMemoryMemberReadEventPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ChatControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private InMemoryAgitReferencePersistence agitStore;

	@Autowired
	private InMemoryChatMessagePersistence messageStore;

	@Autowired
	private InMemoryChatStatePort chatStatePort;

	@Autowired
	private InMemoryMemberReadEventPort memberReadEventPort;

	private UUID agitUuid;
	private UUID userUuid;

	@BeforeEach
	void setUp() {
		agitUuid = UUID.randomUUID();
		userUuid = UUID.randomUUID();
		agitStore.save(AgitRoomReference.create(agitUuid, "아지트", "", 5, null, userUuid, "호스트"));
		messageStore.save(talkAt("2026-08-18T01:00:00Z", "안녕"));
		memberReadEventPort.clear();
	}

	@Test
	void getMessages_unauthorizedWithoutHeader() throws Exception {
		mockMvc.perform(get("/api/v1/agits/{agitUuid}/messages", agitUuid))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
	}

	@Test
	void getMessages_forbiddenWhenNotActiveMember() throws Exception {
		mockMvc.perform(get("/api/v1/agits/{agitUuid}/messages", agitUuid)
						.header(ChatController.USER_UUID_HEADER, UUID.randomUUID().toString()))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("MEMBER_NOT_ACTIVE"));
	}

	@Test
	void getMessages_returnsHistoryForActiveMember() throws Exception {
		mockMvc.perform(get("/api/v1/agits/{agitUuid}/messages", agitUuid)
						.header(ChatController.USER_UUID_HEADER, userUuid.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.messages[0].content").value("안녕"))
				.andExpect(jsonPath("$.messages[0].type").value("TALK"))
				.andExpect(jsonPath("$.hasNext").value(false))
				.andExpect(jsonPath("$.nextCursor").doesNotExist());
	}

	@Test
	void getMessages_returnsSecondPageWithCursor() throws Exception {
		ChatMessage second = talkAt("2026-08-18T02:00:00Z", "2");
		ChatMessage third = talkAt("2026-08-18T03:00:00Z", "3");
		messageStore.save(second);
		messageStore.save(third);

		mockMvc.perform(get("/api/v1/agits/{agitUuid}/messages", agitUuid)
						.header(ChatController.USER_UUID_HEADER, userUuid.toString())
						.param("size", "2"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.messages.length()").value(2))
				.andExpect(jsonPath("$.messages[0].content").value("3"))
				.andExpect(jsonPath("$.messages[1].content").value("2"))
				.andExpect(jsonPath("$.hasNext").value(true))
				.andExpect(jsonPath("$.nextCursor.id").value(second.getId().toString()));

		mockMvc.perform(get("/api/v1/agits/{agitUuid}/messages", agitUuid)
						.header(ChatController.USER_UUID_HEADER, userUuid.toString())
						.param("size", "2")
						.param("cursorCreatedAt", second.getCreatedAt().toString())
						.param("cursorId", second.getId().toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.messages.length()").value(1))
				.andExpect(jsonPath("$.messages[0].content").value("안녕"))
				.andExpect(jsonPath("$.hasNext").value(false));
	}

	@Test
	void getMessages_clampsSizeToMax() throws Exception {
		Instant base = Instant.parse("2026-08-18T02:00:00Z");
		for (int index = 0; index < 105; index++) {
			messageStore.save(talkAt(base.plusSeconds(index).toString(), "m" + index));
		}

		mockMvc.perform(get("/api/v1/agits/{agitUuid}/messages", agitUuid)
						.header(ChatController.USER_UUID_HEADER, userUuid.toString())
						.param("size", "200"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.messages.length()").value(100))
				.andExpect(jsonPath("$.hasNext").value(true));
	}

	@Test
	void getMessages_rejectsPartialCursor() throws Exception {
		mockMvc.perform(get("/api/v1/agits/{agitUuid}/messages", agitUuid)
						.header(ChatController.USER_UUID_HEADER, userUuid.toString())
						.param("cursorCreatedAt", Instant.parse("2026-08-18T01:00:00Z").toString()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
	}

	@Test
	void markRead_acceptsOptionalReadAtBody() throws Exception {
		Instant readAt = Instant.parse("2026-08-18T04:00:00Z");

		mockMvc.perform(post("/api/v1/agits/{agitUuid}/read", agitUuid)
						.header(ChatController.USER_UUID_HEADER, userUuid.toString())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"readAt\":\"" + readAt + "\"}"))
				.andExpect(status().isNoContent());

		assertThat(chatStatePort.getReadAt(userUuid, agitUuid)).contains(readAt);
		assertThat(chatStatePort.getMemberReadAt(agitUuid, userUuid)).isEqualTo(readAt);
		assertThat(memberReadEventPort.getPublished()).hasSize(1);
	}

	@Test
	void markRead_isMonotonic() throws Exception {
		Instant first = Instant.parse("2026-08-18T04:00:00Z");
		Instant second = Instant.parse("2026-08-18T05:00:00Z");
		Instant older = Instant.parse("2026-08-18T03:00:00Z");

		mockMvc.perform(post("/api/v1/agits/{agitUuid}/read", agitUuid)
						.header(ChatController.USER_UUID_HEADER, userUuid.toString())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"readAt\":\"" + first + "\"}"))
				.andExpect(status().isNoContent());
		mockMvc.perform(post("/api/v1/agits/{agitUuid}/read", agitUuid)
						.header(ChatController.USER_UUID_HEADER, userUuid.toString())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"readAt\":\"" + second + "\"}"))
				.andExpect(status().isNoContent());
		mockMvc.perform(post("/api/v1/agits/{agitUuid}/read", agitUuid)
						.header(ChatController.USER_UUID_HEADER, userUuid.toString())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"readAt\":\"" + older + "\"}"))
				.andExpect(status().isNoContent());

		assertThat(chatStatePort.getReadAt(userUuid, agitUuid)).contains(second);
		assertThat(memberReadEventPort.getPublished()).hasSize(2);
	}

	@Test
	void markRead_noContentForActiveMember() throws Exception {
		mockMvc.perform(post("/api/v1/agits/{agitUuid}/read", agitUuid)
						.header(ChatController.USER_UUID_HEADER, userUuid.toString()))
				.andExpect(status().isNoContent());
	}

	@Test
	void markRead_unauthorizedWithoutHeader() throws Exception {
		mockMvc.perform(post("/api/v1/agits/{agitUuid}/read", agitUuid))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
	}

	@Test
	void markRead_forbiddenWhenNotActiveMember() throws Exception {
		mockMvc.perform(post("/api/v1/agits/{agitUuid}/read", agitUuid)
						.header(ChatController.USER_UUID_HEADER, UUID.randomUUID().toString()))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("MEMBER_NOT_ACTIVE"));
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
