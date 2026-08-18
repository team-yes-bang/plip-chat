package com.plip.chat.support;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class TestChatMessagePersistenceConfig {

	@Bean
	public InMemoryChatMessagePersistence chatMessageStore() {
		return new InMemoryChatMessagePersistence();
	}
}
