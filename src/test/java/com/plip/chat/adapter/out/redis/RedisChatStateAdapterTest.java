package com.plip.chat.adapter.out.redis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RedisChatStateAdapterTest {

	@Mock
	private StringRedisTemplate redisTemplate;

	@Mock
	@SuppressWarnings("rawtypes")
	private HashOperations hashOperations;

	@InjectMocks
	private RedisChatStateAdapter redisChatStateAdapter;

	@Test
	void markRead_hsetsUserReadStateField() {
		given(redisTemplate.opsForHash()).willReturn(hashOperations);
		UUID userUuid = UUID.randomUUID();
		UUID agitUuid = UUID.randomUUID();
		Instant readAt = Instant.parse("2026-08-18T04:00:00Z");

		redisChatStateAdapter.markRead(userUuid, agitUuid, readAt);

		verify(hashOperations).put(
				RedisChatStateAdapter.READ_STATE_KEY_PREFIX + userUuid,
				agitUuid + RedisChatStateAdapter.CHAT_FIELD_SUFFIX,
				readAt.toString()
		);
	}
}
