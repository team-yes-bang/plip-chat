package com.plip.chat.support;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class TestAgitReferencePersistenceConfig {

	@Bean
	public InMemoryAgitReferencePersistence agitReferenceStore() {
		return new InMemoryAgitReferencePersistence();
	}
}
