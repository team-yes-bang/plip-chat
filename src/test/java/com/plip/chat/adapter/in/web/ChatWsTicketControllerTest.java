package com.plip.chat.adapter.in.web;

import com.plip.chat.support.TestChatWsTicketConfig.InMemoryChatWsTicketPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ChatWsTicketControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private InMemoryChatWsTicketPort chatWsTicketPort;

	private UUID userUuid;

	@BeforeEach
	void setUp() {
		userUuid = UUID.randomUUID();
	}

	@Test
	void issueTicket_returnsCreatedWithTicket() throws Exception {
		mockMvc.perform(post("/api/v1/ws/ticket")
						.header(ChatController.USER_UUID_HEADER, userUuid))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.ticket").isNotEmpty())
				.andExpect(jsonPath("$.expiresInSeconds").value(30));
	}

	@Test
	void issueTicket_returns401WithoutUserUuidHeader() throws Exception {
		mockMvc.perform(post("/api/v1/ws/ticket"))
				.andExpect(status().isUnauthorized());
	}
}
