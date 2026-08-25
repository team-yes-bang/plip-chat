package com.plip.chat.support;

import com.plip.chat.application.port.out.ChatBroadcastPort;
import com.plip.chat.domain.model.ChatMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Profile("test")
public class TestChatBroadcastConfig {

	@Bean
	public InMemoryChatBroadcastPort chatBroadcastPort() {
		return new InMemoryChatBroadcastPort();
	}

	public static class InMemoryChatBroadcastPort implements ChatBroadcastPort {

		private final List<ChatMessage> published = new ArrayList<>();

		@Override
		public void publish(ChatMessage message) {
			published.add(message);
		}

		public List<ChatMessage> getPublished() {
			return List.copyOf(published);
		}

		public void clear() {
			published.clear();
		}
	}
}
