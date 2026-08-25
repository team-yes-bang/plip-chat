package com.plip.chat.support;

import com.plip.chat.application.port.out.ChatReceiptBroadcastPort;
import com.plip.chat.application.port.out.ChatReceiptPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@Profile("test")
public class TestChatReceiptConfig {

	@Bean
	public InMemoryChatReceiptPort chatReceiptPort() {
		return new InMemoryChatReceiptPort();
	}

	@Bean
	public InMemoryChatReceiptBroadcastPort chatReceiptBroadcastPort() {
		return new InMemoryChatReceiptBroadcastPort();
	}

	public static class InMemoryChatReceiptPort implements ChatReceiptPort {

		private final Map<String, Integer> store = new ConcurrentHashMap<>();

		@Override
		public void initUnreadMemberCount(UUID agitUuid, UUID messageId, int count) {
			store.put(receiptKey(agitUuid, messageId), count);
		}

		@Override
		public OptionalInt getUnreadMemberCount(UUID agitUuid, UUID messageId) {
			Integer count = store.get(receiptKey(agitUuid, messageId));
			return count == null ? OptionalInt.empty() : OptionalInt.of(count);
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
			store.put(receiptKey(agitUuid, messageId), next);
			return OptionalInt.of(next);
		}

		public void clear() {
			store.clear();
		}

		private static String receiptKey(UUID agitUuid, UUID messageId) {
			return agitUuid + ":" + messageId;
		}
	}

	public static class InMemoryChatReceiptBroadcastPort implements ChatReceiptBroadcastPort {

		private final List<PublishedReceipt> published = new ArrayList<>();

		@Override
		public void publishReceiptUpdate(UUID agitUuid, UUID messageId, int unreadMemberCount) {
			published.add(new PublishedReceipt(agitUuid, messageId, unreadMemberCount));
		}

		public List<PublishedReceipt> getPublished() {
			return List.copyOf(published);
		}

		public void clear() {
			published.clear();
		}

		public record PublishedReceipt(UUID agitUuid, UUID messageId, int unreadMemberCount) {
		}
	}
}
