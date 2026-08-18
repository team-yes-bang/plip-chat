package com.plip.chat.application.exception;

public class UnauthorizedException extends RuntimeException {

	public UnauthorizedException() {
		super("인증 정보가 없습니다.");
	}
}
