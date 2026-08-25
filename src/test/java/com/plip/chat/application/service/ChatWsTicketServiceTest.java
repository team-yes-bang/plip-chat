package com.plip.chat.application.service;

import com.plip.chat.application.port.in.dto.WsTicketResult;
import com.plip.chat.support.TestChatWsTicketConfig.InMemoryChatWsTicketPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChatWsTicketServiceTest {

	private InMemoryChatWsTicketPort ticketPort;
	private ChatWsTicketService chatWsTicketService;

	@BeforeEach
	void setUp() {
		ticketPort = new InMemoryChatWsTicketPort();
		chatWsTicketService = new ChatWsTicketService(ticketPort);
	}

	@Test
	void issue_returnsTicketAndTtl() {
		UUID userUuid = UUID.randomUUID();

		WsTicketResult result = chatWsTicketService.issue(userUuid);

		assertThat(result.ticket()).isNotBlank();
		assertThat(result.expiresInSeconds()).isEqualTo(ChatWsTicketService.TICKET_TTL.getSeconds());
		assertThat(ticketPort.consume(result.ticket())).contains(userUuid);
	}

	@Test
	void issue_rejectsNullUserUuid() {
		assertThatThrownBy(() -> chatWsTicketService.issue(null))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
