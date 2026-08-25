package com.plip.chat.adapter.out.redis;

import com.plip.chat.application.port.out.ChatWsTicketPort;
import com.plip.chat.domain.model.UuidV7;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class RedisChatWsTicketAdapter implements ChatWsTicketPort {

	static final String KEY_PREFIX = "chat:ws:ticket:";

	private final StringRedisTemplate redisTemplate;

	@Override
	public String issue(UUID userUuid, Duration ttl) {
		String ticket = UuidV7.create().toString();
		redisTemplate.opsForValue().set(key(ticket), userUuid.toString(), ttl);
		return ticket;
	}

	@Override
	public Optional<UUID> consume(String ticket) {
		if (ticket == null || ticket.isBlank()) {
			return Optional.empty();
		}
		String userUuid = redisTemplate.opsForValue().getAndDelete(key(ticket.trim()));
		if (userUuid == null || userUuid.isBlank()) {
			return Optional.empty();
		}
		try {
			return Optional.of(UUID.fromString(userUuid.trim()));
		} catch (IllegalArgumentException e) {
			return Optional.empty();
		}
	}

	static String key(String ticket) {
		return KEY_PREFIX + ticket;
	}
}
