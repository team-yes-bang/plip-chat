package com.plip.chat.support;

import com.plip.chat.application.port.out.ChatStatePort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@Profile("test")
public class TestChatStateConfig {

	@Bean
	public InMemoryChatStatePort chatStatePort() {
		return new InMemoryChatStatePort();
	}

	public static class InMemoryChatStatePort implements ChatStatePort {

		private final Map<String, Instant> store = new ConcurrentHashMap<>();
		private final Map<UUID, Instant> lastChatAtStore = new ConcurrentHashMap<>();

		@Override
		public void markRead(UUID userUuid, UUID agitUuid, Instant readAt) {
			store.put(key(userUuid, agitUuid), readAt);
		}

		@Override
		public void updateLastChatAt(UUID agitUuid, Instant lastChatAt) {
			lastChatAtStore.put(agitUuid, lastChatAt);
		}

		public Instant get(UUID userUuid, UUID agitUuid) {
			return store.get(key(userUuid, agitUuid));
		}

		public Instant getLastChatAt(UUID agitUuid) {
			return lastChatAtStore.get(agitUuid);
		}

		private static String key(UUID userUuid, UUID agitUuid) {
			return userUuid + ":" + agitUuid;
		}
	}
}
