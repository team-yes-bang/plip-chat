package com.plip.chat.support;

import com.plip.chat.application.port.out.ChatWsTicketPort;
import com.plip.chat.global.config.UserUuidChannelInterceptor;
import com.plip.chat.global.config.UserUuidHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@Profile("test")
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class TestWebSocketMessageBrokerConfig implements WebSocketMessageBrokerConfigurer {

	private final ChatWsTicketPort chatWsTicketPort;

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/topic", "/sub");
		registry.setApplicationDestinationPrefixes("/app");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		UserUuidHandshakeInterceptor handshakeInterceptor = new UserUuidHandshakeInterceptor(chatWsTicketPort);
		registry.addEndpoint("/ws/chat")
				.addInterceptors(handshakeInterceptor)
				.setAllowedOriginPatterns("*");
		registry.addEndpoint("/ws/chat")
				.addInterceptors(handshakeInterceptor)
				.setAllowedOriginPatterns("*")
				.withSockJS();
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(new UserUuidChannelInterceptor());
	}
}
