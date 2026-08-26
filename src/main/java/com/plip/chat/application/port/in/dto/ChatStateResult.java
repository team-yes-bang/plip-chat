package com.plip.chat.application.port.in.dto;

import lombok.Getter;

import java.time.Instant;

@Getter
public class ChatStateResult {

	private final Instant readAt;
	private final Instant lastChatAt;
	private final long unreadMessageCount;

	public ChatStateResult(Instant readAt, Instant lastChatAt, long unreadMessageCount) {
		this.readAt = readAt;
		this.lastChatAt = lastChatAt;
		this.unreadMessageCount = unreadMessageCount;
	}
}
