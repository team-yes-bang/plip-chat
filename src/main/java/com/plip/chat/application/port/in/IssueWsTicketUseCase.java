package com.plip.chat.application.port.in;

import com.plip.chat.application.port.in.dto.WsTicketResult;

import java.util.UUID;

public interface IssueWsTicketUseCase {

	WsTicketResult issue(UUID userUuid);
}
