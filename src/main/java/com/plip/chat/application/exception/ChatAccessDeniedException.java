package com.plip.chat.application.exception;

public class ChatAccessDeniedException extends RuntimeException {

	public ChatAccessDeniedException() {
		super("ACTIVE 멤버만 채팅을 사용할 수 있습니다.");
	}
}
