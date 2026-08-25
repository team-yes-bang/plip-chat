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

		private final List<PublishedMessage> published = new ArrayList<>();

		@Override
		public void publish(ChatMessage message, Integer unreadMemberCount) {
			published.add(new PublishedMessage(message, unreadMemberCount));
		}

		public List<PublishedMessage> getPublished() {
			return List.copyOf(published);
		}

		public List<ChatMessage> getPublishedMessages() {
			return published.stream().map(PublishedMessage::message).toList();
		}

		public void clear() {
			published.clear();
		}

		public record PublishedMessage(ChatMessage message, Integer unreadMemberCount) {
		}
	}
}
