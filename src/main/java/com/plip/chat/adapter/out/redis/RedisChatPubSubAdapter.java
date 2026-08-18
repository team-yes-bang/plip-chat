package com.plip.chat.adapter.out.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plip.chat.application.port.out.ChatBroadcastPort;
import com.plip.chat.domain.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class RedisChatPubSubAdapter implements ChatBroadcastPort, MessageListener {

	static final String CHANNEL_PREFIX = "chat:agit:";
	static final String SUBSCRIBE_DESTINATION_PREFIX = "/sub/agits/";

	private final StringRedisTemplate redisTemplate;
	private final ObjectMapper objectMapper;
	private final SimpMessagingTemplate simpMessagingTemplate;

	@Override
	public void publish(ChatMessage message) {
		try {
			String json = objectMapper.writeValueAsString(ChatBroadcastPayload.from(message));
			redisTemplate.convertAndSend(CHANNEL_PREFIX + message.getAgitUuid(), json);
		} catch (Exception e) {
			log.warn("채팅 브로드캐스트 발행 실패 agitUuid={}: {}", message.getAgitUuid(), e.getMessage());
		}
	}

	@Override
	public void onMessage(Message message, byte[] pattern) {
		String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
		if (!channel.startsWith(CHANNEL_PREFIX)) {
			return;
		}
		String agitUuid = channel.substring(CHANNEL_PREFIX.length());
		String body = new String(message.getBody(), StandardCharsets.UTF_8);
		try {
			ChatBroadcastPayload payload = objectMapper.readValue(body, ChatBroadcastPayload.class);
			simpMessagingTemplate.convertAndSend(SUBSCRIBE_DESTINATION_PREFIX + agitUuid, payload);
		} catch (Exception e) {
			log.warn("채팅 브로드캐스트 중계 실패 channel={}: {}", channel, e.getMessage());
		}
	}
}
