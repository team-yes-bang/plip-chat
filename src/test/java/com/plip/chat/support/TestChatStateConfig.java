package com.plip.chat.support;

import com.plip.chat.application.port.out.ChatStatePort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Instant;
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
		public void markRead(UUID userUuid, UUID agitUuid, Instant readAt) {
			readStateStore.put(readStateKey(userUuid, agitUuid), readAt);
			memberReadsStore.put(memberReadKey(agitUuid, userUuid), readAt);
		}

		@Override
		public void updateLastChatAt(UUID agitUuid, Instant lastChatAt) {
			lastChatAtStore.put(agitUuid, lastChatAt);
		}

		public Instant getLastChatAt(UUID agitUuid) {
			return lastChatAtStore.get(agitUuid);
		}

		public Instant getMemberReadAt(UUID agitUuid, UUID userUuid) {
			return memberReadsStore.get(memberReadKey(agitUuid, userUuid));
		}

		private static String readStateKey(UUID userUuid, UUID agitUuid) {
			return userUuid + ":" + agitUuid;
		}

		private static String memberReadKey(UUID agitUuid, UUID userUuid) {
			return agitUuid + ":" + userUuid;
		}
	}
}
