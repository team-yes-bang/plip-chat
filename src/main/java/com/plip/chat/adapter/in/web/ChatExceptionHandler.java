package com.plip.chat.adapter.in.web;

import com.plip.chat.adapter.in.web.dto.ErrorResponse;
import com.plip.chat.application.exception.ChatAccessDeniedException;
import com.plip.chat.application.exception.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ChatExceptionHandler {

	@ExceptionHandler(UnauthorizedException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ErrorResponse handleUnauthorized(UnauthorizedException ex) {
		return ErrorResponse.builder()
				.code("UNAUTHORIZED")
				.message(ex.getMessage())
				.build();
	}

	@ExceptionHandler(ChatAccessDeniedException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ErrorResponse handleAccessDenied(ChatAccessDeniedException ex) {
		return ErrorResponse.builder()
				.code("MEMBER_NOT_ACTIVE")
				.message(ex.getMessage())
				.build();
	}

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleIllegalArgument(IllegalArgumentException ex) {
		return ErrorResponse.builder()
				.code("INVALID_ARGUMENT")
				.message(ex.getMessage())
				.build();
	}
}
