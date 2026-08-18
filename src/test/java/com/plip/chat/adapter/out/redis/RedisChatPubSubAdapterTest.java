package com.plip.chat.adapter.out.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.plip.chat.domain.model.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.DefaultMessage;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RedisChatPubSubAdapterTest {

	@Mock
	private StringRedisTemplate redisTemplate;

	@Mock
	private SimpMessagingTemplate simpMessagingTemplate;

	private ObjectMapper objectMapper;
	private RedisChatPubSubAdapter adapter;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		adapter = new RedisChatPubSubAdapter(redisTemplate, objectMapper, simpMessagingTemplate);
	}

	@Test
	void publish_sendsJsonToAgitChannel() throws Exception {
		ChatMessage talk = ChatMessage.talk(UUID.randomUUID(), UUID.randomUUID(), "안녕");

		adapter.publish(talk);

		ArgumentCaptor<String> payload = ArgumentCaptor.forClass(String.class);
		verify(redisTemplate).convertAndSend(
				eq(RedisChatPubSubAdapter.CHANNEL_PREFIX + talk.getAgitUuid()),
				payload.capture()
		);
		ChatBroadcastPayload parsed = objectMapper.readValue(payload.getValue(), ChatBroadcastPayload.class);
		assertThat(parsed.getContent()).isEqualTo("안녕");
		assertThat(parsed.getId()).isEqualTo(talk.getId());
		assertThat(parsed.getType()).isEqualTo("TALK");
	}

	@Test
	void onMessage_relaysToLocalStompBroker() throws Exception {
		ChatMessage talk = ChatMessage.talk(UUID.randomUUID(), UUID.randomUUID(), "안녕");
		String json = objectMapper.writeValueAsString(ChatBroadcastPayload.from(talk));
		byte[] channel = (RedisChatPubSubAdapter.CHANNEL_PREFIX + talk.getAgitUuid())
				.getBytes(StandardCharsets.UTF_8);

		adapter.onMessage(new DefaultMessage(channel, json.getBytes(StandardCharsets.UTF_8)), null);

		ArgumentCaptor<ChatBroadcastPayload> payload = ArgumentCaptor.forClass(ChatBroadcastPayload.class);
		verify(simpMessagingTemplate).convertAndSend(
				eq(RedisChatPubSubAdapter.SUBSCRIBE_DESTINATION_PREFIX + talk.getAgitUuid()),
				payload.capture()
		);
		assertThat(payload.getValue().getContent()).isEqualTo("안녕");
	}
}
