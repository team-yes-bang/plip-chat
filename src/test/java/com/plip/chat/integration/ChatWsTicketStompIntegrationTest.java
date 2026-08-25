package com.plip.chat.integration;

import com.plip.chat.support.TestChatWsTicketConfig.InMemoryChatWsTicketPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ChatWsTicketStompIntegrationTest {

	@LocalServerPort
	private int port;

	@Autowired
	private InMemoryChatWsTicketPort chatWsTicketPort;

	private UUID userUuid;

	@BeforeEach
	void setUp() {
		userUuid = UUID.randomUUID();
	}

	@Test
	void stompConnect_succeedsWithValidTicket() throws Exception {
		String ticket = chatWsTicketPort.issue(userUuid, Duration.ofSeconds(30));

		StompSession session = connect(ticket);

		assertThat(session.isConnected()).isTrue();
		session.disconnect();
	}

	@Test
	void stompConnect_rejectsMissingTicket() {
		assertThatThrownBy(() -> connectWithoutTicket())
				.isInstanceOf(Exception.class);
	}

	@Test
	void stompConnect_rejectsReusedTicket() throws Exception {
		String ticket = chatWsTicketPort.issue(userUuid, Duration.ofSeconds(30));
		StompSession first = connect(ticket);
		first.disconnect();

		assertThatThrownBy(() -> connect(ticket))
				.isInstanceOf(Exception.class);
	}

	private StompSession connect(String ticket) throws Exception {
		return connectTo("ws://localhost:" + port + "/ws/chat?ticket=" + ticket);
	}

	private StompSession connectWithoutTicket() throws Exception {
		return connectTo("ws://localhost:" + port + "/ws/chat");
	}

	private StompSession connectTo(String url) throws Exception {
		WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
		stompClient.setMessageConverter(new MappingJackson2MessageConverter());

		CompletableFuture<StompSession> connected = new CompletableFuture<>();

		stompClient.connectAsync(url, new StompSessionHandlerAdapter() {
			@Override
			public void afterConnected(StompSession session, org.springframework.messaging.simp.stomp.StompHeaders connectedHeaders) {
				connected.complete(session);
			}

			@Override
			public void handleTransportError(StompSession session, Throwable exception) {
				connected.completeExceptionally(exception);
			}
		});

		return connected.get(5, TimeUnit.SECONDS);
	}
}
