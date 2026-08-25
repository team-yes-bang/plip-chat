package com.plip.chat.adapter.out.event;

import com.plip.chat.application.port.out.MemberReadEventPort;
import com.plip.chat.domain.event.MemberReadUpdated;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class SpringMemberReadEventPublisher implements MemberReadEventPort {

	private final ApplicationEventPublisher applicationEventPublisher;

	@Override
	public void publish(MemberReadUpdated event) {
		applicationEventPublisher.publishEvent(event);
	}
}
