package com.plip.chat.adapter.out.redis;

import com.plip.chat.application.port.out.ChatReceiptPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.UUID;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class RedisChatReceiptAdapter implements ChatReceiptPort {

	static final String MESSAGE_RECEIPTS_KEY_PREFIX = "agit:";
	static final String MESSAGE_RECEIPTS_KEY_SUFFIX = ":message_receipts";

	private final StringRedisTemplate redisTemplate;

	@Override
	public void initUnreadMemberCount(UUID agitUuid, UUID messageId, int count) {
		redisTemplate.opsForHash().put(receiptsKey(agitUuid), messageId.toString(), Integer.toString(count));
	}

	@Override
	public OptionalInt getUnreadMemberCount(UUID agitUuid, UUID messageId) {
		Object value = redisTemplate.opsForHash().get(receiptsKey(agitUuid), messageId.toString());
		if (value == null) {
			return OptionalInt.empty();
		}
		return OptionalInt.of(Integer.parseInt(value.toString()));
	}

	@Override
	public Map<UUID, Integer> getUnreadMemberCounts(UUID agitUuid, Collection<UUID> messageIds) {
		if (messageIds == null || messageIds.isEmpty()) {
			return Map.of();
		}
		Map<UUID, Integer> result = new HashMap<>();
		for (UUID messageId : messageIds) {
			getUnreadMemberCount(agitUuid, messageId).ifPresent(count -> result.put(messageId, count));
		}
		return Map.copyOf(result);
	}

	@Override
	public OptionalInt decrementUnreadMemberCount(UUID agitUuid, UUID messageId) {
		OptionalInt current = getUnreadMemberCount(agitUuid, messageId);
		if (current.isEmpty()) {
			return OptionalInt.empty();
		}
		int next = Math.max(0, current.getAsInt() - 1);
		redisTemplate.opsForHash().put(receiptsKey(agitUuid), messageId.toString(), Integer.toString(next));
		return OptionalInt.of(next);
	}

	static String receiptsKey(UUID agitUuid) {
		return MESSAGE_RECEIPTS_KEY_PREFIX + agitUuid + MESSAGE_RECEIPTS_KEY_SUFFIX;
	}
}
