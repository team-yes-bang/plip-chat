package com.plip.chat.support;

import com.plip.chat.application.port.out.ChatWsTicketPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@Profile("test")
public class TestChatWsTicketConfig {

	@Bean
	public InMemoryChatWsTicketPort chatWsTicketPort() {
		return new InMemoryChatWsTicketPort();
	}

	public static class InMemoryChatWsTicketPort implements ChatWsTicketPort {

		private record Entry(UUID userUuid, Instant expiresAt) {
		}

		private final Map<String, Entry> store = new ConcurrentHashMap<>();

		@Override
		public String issue(UUID userUuid, Duration ttl) {
			String ticket = UUID.randomUUID().toString();
			store.put(ticket, new Entry(userUuid, Instant.now().plus(ttl)));
			return ticket;
		}

		@Override
		public Optional<UUID> consume(String ticket) {
			if (ticket == null || ticket.isBlank()) {
				return Optional.empty();
			}
			Entry entry = store.remove(ticket.trim());
			if (entry == null || Instant.now().isAfter(entry.expiresAt())) {
				return Optional.empty();
			}
			return Optional.of(entry.userUuid());
		}
	}
}
