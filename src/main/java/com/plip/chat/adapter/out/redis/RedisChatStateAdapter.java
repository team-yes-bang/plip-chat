package com.plip.chat.adapter.out.redis;

import com.plip.chat.application.port.out.ChatStatePort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class RedisChatStateAdapter implements ChatStatePort {

	static final String READ_STATE_KEY_PREFIX = "user:read_state:";
	static final String CHAT_FIELD_SUFFIX = ":chat";
	static final String WRITE_STATE_KEY_PREFIX = "agit:write_state:";
	static final String MEMBER_READS_KEY_PREFIX = "agit:";
	static final String MEMBER_READS_KEY_SUFFIX = ":member_reads";
	static final String LAST_CHAT_AT_FIELD = "last_chat_at";

	private final StringRedisTemplate redisTemplate;

	@Override
	public Optional<Instant> getReadAt(UUID userUuid, UUID agitUuid) {
		Object value = redisTemplate.opsForHash().get(
				READ_STATE_KEY_PREFIX + userUuid,
				agitUuid + CHAT_FIELD_SUFFIX
		);
		if (value == null) {
			return Optional.empty();
		}
		return Optional.of(Instant.parse(value.toString()));
	}

	@Override
	public void markRead(UUID userUuid, UUID agitUuid, Instant readAt) {
		String readAtValue = readAt.toString();
		redisTemplate.opsForHash().put(
				READ_STATE_KEY_PREFIX + userUuid,
				agitUuid + CHAT_FIELD_SUFFIX,
				readAtValue
		);
		redisTemplate.opsForHash().put(
				memberReadsKey(agitUuid),
				userUuid.toString(),
				readAtValue
		);
	}

	@Override
	public void updateLastChatAt(UUID agitUuid, Instant lastChatAt) {
		redisTemplate.opsForHash().put(
				WRITE_STATE_KEY_PREFIX + agitUuid,
				LAST_CHAT_AT_FIELD,
				lastChatAt.toString()
		);
	}

	static String memberReadsKey(UUID agitUuid) {
		return MEMBER_READS_KEY_PREFIX + agitUuid + MEMBER_READS_KEY_SUFFIX;
	}
}
