package com.plip.chat.support;

import com.plip.chat.application.port.out.ChatMessageEventPort;
import com.plip.chat.domain.model.ChatMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class TestChatMessageEventConfig {

	@Bean
	public ChatMessageEventPort chatMessageEventPort() {
		return new NoOpChatMessageEventPort();
	}

	public static class NoOpChatMessageEventPort implements ChatMessageEventPort {

		@Override
		public void publishMessageSent(ChatMessage message) {
			// test: Kafka 없이 채팅 커맨드만 검증
		}
	}
}
