package com.plip.chat.support;

import com.plip.chat.application.port.out.MemberReadEventPort;
import com.plip.chat.domain.event.MemberReadUpdated;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Profile("test")
public class TestMemberReadEventConfig {

	@Bean
	public InMemoryMemberReadEventPort memberReadEventPort() {
		return new InMemoryMemberReadEventPort();
	}

	public static class InMemoryMemberReadEventPort implements MemberReadEventPort {

		private final List<MemberReadUpdated> published = new ArrayList<>();

		@Override
		public void publish(MemberReadUpdated event) {
			published.add(event);
		}

		public List<MemberReadUpdated> getPublished() {
			return List.copyOf(published);
		}

		public void clear() {
			published.clear();
		}
	}
}
