package com.plip.chat.application.service;

import com.plip.chat.application.port.in.IssueWsTicketUseCase;
import com.plip.chat.application.port.in.dto.WsTicketResult;
import com.plip.chat.application.port.out.ChatWsTicketPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatWsTicketService implements IssueWsTicketUseCase {

	static final Duration TICKET_TTL = Duration.ofSeconds(30);

	private final ChatWsTicketPort chatWsTicketPort;

	@Override
	public WsTicketResult issue(UUID userUuid) {
		if (userUuid == null) {
			throw new IllegalArgumentException("userUuid는 필수입니다.");
		}
		String ticket = chatWsTicketPort.issue(userUuid, TICKET_TTL);
		return new WsTicketResult(ticket, TICKET_TTL.getSeconds());
	}
}
