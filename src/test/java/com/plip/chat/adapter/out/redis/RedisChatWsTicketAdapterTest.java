package com.plip.chat.adapter.out.redis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RedisChatWsTicketAdapterTest {

	@Mock
	private StringRedisTemplate redisTemplate;

	@Mock
	private ValueOperations<String, String> valueOperations;

	@InjectMocks
	private RedisChatWsTicketAdapter adapter;

	@Test
	void issue_storesTicketWithTtl() {
		UUID userUuid = UUID.randomUUID();
		given(redisTemplate.opsForValue()).willReturn(valueOperations);

		String ticket = adapter.issue(userUuid, Duration.ofSeconds(30));

		assertThat(ticket).isNotBlank();
		verify(valueOperations).set(
				eq(RedisChatWsTicketAdapter.key(ticket)),
				eq(userUuid.toString()),
				eq(Duration.ofSeconds(30))
		);
	}

	@Test
	void consume_returnsUserUuidAndDeletesTicket() {
		UUID userUuid = UUID.randomUUID();
		String ticket = UUID.randomUUID().toString();
		given(redisTemplate.opsForValue()).willReturn(valueOperations);
		given(valueOperations.getAndDelete(RedisChatWsTicketAdapter.key(ticket))).willReturn(userUuid.toString());

		assertThat(adapter.consume(ticket)).contains(userUuid);
	}

	@Test
	void consume_returnsEmptyWhenMissing() {
		String ticket = "missing";
		given(redisTemplate.opsForValue()).willReturn(valueOperations);
		given(valueOperations.getAndDelete(RedisChatWsTicketAdapter.key(ticket))).willReturn(null);

		assertThat(adapter.consume(ticket)).isEmpty();
	}
}
