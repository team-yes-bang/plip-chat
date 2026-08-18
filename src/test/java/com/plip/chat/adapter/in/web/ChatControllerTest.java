package com.plip.chat.adapter.in.web;

import com.plip.chat.domain.model.AgitRoomReference;
import com.plip.chat.domain.model.ChatMessage;
import com.plip.chat.support.InMemoryAgitReferencePersistence;
import com.plip.chat.support.InMemoryChatMessagePersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

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

	private UUID agitUuid;
	private UUID userUuid;

	@BeforeEach
	void setUp() {
		agitUuid = UUID.randomUUID();
		userUuid = UUID.randomUUID();
		agitStore.save(AgitRoomReference.create(agitUuid, "아지트", "", 5, null, userUuid, "호스트"));
		messageStore.save(ChatMessage.talk(agitUuid, userUuid, "안녕"));
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
				.andExpect(jsonPath("$.hasNext").value(false));
	}

	@Test
	void markRead_noContentForActiveMember() throws Exception {
		mockMvc.perform(post("/api/v1/agits/{agitUuid}/read", agitUuid)
						.header(ChatController.USER_UUID_HEADER, userUuid.toString()))
				.andExpect(status().isNoContent());
	}
}
