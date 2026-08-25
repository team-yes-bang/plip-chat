package com.plip.chat.support;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChatGatewayPathContractTest {

	@Test
	void toGatewayPath_mapsRestAndWsTicket() {
		assertThat(ChatGatewayPathContract.toGatewayPath(ChatGatewayPathContract.WS_TICKET))
				.isEqualTo("/api/chat/api/v1/ws/ticket");
	}

	@Test
	void toGatewayPath_mapsWsChatWithTicketQuery() {
		assertThat(ChatGatewayPathContract.toGatewayPath(ChatGatewayPathContract.WS_CHAT) + "?ticket=abc")
				.isEqualTo("/api/chat/ws/chat?ticket=abc");
	}

	@Test
	void toGatewayPath_mapsAgitMessages() {
		UUID agitUuid = UUID.fromString("018f3f6e-8e2a-7b3c-9d4e-5f6a7b8c9d0e");
		assertThat(ChatGatewayPathContract.toGatewayPath("/api/v1/agits/" + agitUuid + "/messages"))
				.isEqualTo("/api/chat/api/v1/agits/" + agitUuid + "/messages");
	}

	@Test
	void toServicePath_stripsGatewayPrefix() {
		assertThat(ChatGatewayPathContract.toServicePath("/api/chat/api/v1/ws/ticket"))
				.isEqualTo("/api/v1/ws/ticket");
		assertThat(ChatGatewayPathContract.toServicePath("/api/chat/ws/chat"))
				.isEqualTo("/ws/chat");
	}

	@Test
	void toServicePath_rejectsInvalidPrefix() {
		assertThatThrownBy(() -> ChatGatewayPathContract.toServicePath("/api/agit/foo"))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
