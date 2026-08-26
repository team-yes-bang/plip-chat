package com.plip.chat.support;

import com.plip.chat.application.port.out.ChatStatePort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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

		private final Map<String, Instant> readStateStore = new ConcurrentHashMap<>();
		private final Map<String, Instant> memberReadsStore = new ConcurrentHashMap<>();
		private final Map<UUID, Instant> lastChatAtStore = new ConcurrentHashMap<>();

		@Override
		public Optional<Instant> getReadAt(UUID userUuid, UUID agitUuid) {
			return Optional.ofNullable(readStateStore.get(readStateKey(userUuid, agitUuid)));
		}

		@Override
		public Optional<Instant> getLastChatAt(UUID agitUuid) {
			return Optional.ofNullable(lastChatAtStore.get(agitUuid));
		}

		@Override
		public Optional<Instant> getMemberReadAt(UUID agitUuid, UUID userUuid) {
			return Optional.ofNullable(memberReadsStore.get(memberReadKey(agitUuid, userUuid)));
		}

		@Override
		public Map<UUID, Instant> getMemberReadAtMap(UUID agitUuid) {
			Map<UUID, Instant> result = new HashMap<>();
			String prefix = agitUuid + ":";
			for (Map.Entry<String, Instant> entry : memberReadsStore.entrySet()) {
				if (entry.getKey().startsWith(prefix)) {
					result.put(UUID.fromString(entry.getKey().substring(prefix.length())), entry.getValue());
				}
			}
			return Map.copyOf(result);
		}

		@Override
		public void markRead(UUID userUuid, UUID agitUuid, Instant readAt) {
			readStateStore.put(readStateKey(userUuid, agitUuid), readAt);
			memberReadsStore.put(memberReadKey(agitUuid, userUuid), readAt);
		}

		@Override
		public void updateLastChatAt(UUID agitUuid, Instant lastChatAt) {
			lastChatAtStore.put(agitUuid, lastChatAt);
		}

		private static String readStateKey(UUID userUuid, UUID agitUuid) {
			return userUuid + ":" + agitUuid;
		}

		private static String memberReadKey(UUID agitUuid, UUID userUuid) {
			return agitUuid + ":" + userUuid;
		}
	}
}
