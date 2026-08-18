package com.plip.chat.adapter.in.web.mapper;

import com.plip.chat.adapter.in.web.dto.ChatCursorResponse;
import com.plip.chat.adapter.in.web.dto.ChatHistoryResponse;
import com.plip.chat.adapter.in.web.dto.ChatMessageResponse;
import com.plip.chat.application.port.in.dto.ChatHistoryResult;
import com.plip.chat.domain.model.ChatMessage;
import org.springframework.stereotype.Component;

@Component
public class ChatWebMapper {

	public ChatHistoryResponse toResponse(ChatHistoryResult result) {
		ChatCursorResponse nextCursor = null;
		if (result.isHasNext()) {
			nextCursor = ChatCursorResponse.builder()
					.createdAt(result.getNextCursorCreatedAt())
					.id(result.getNextCursorId())
					.build();
		}
		return ChatHistoryResponse.builder()
				.messages(result.getMessages().stream().map(this::toMessageResponse).toList())
				.nextCursor(nextCursor)
				.hasNext(result.isHasNext())
				.build();
	}

	public ChatMessageResponse toMessageResponse(ChatMessage message) {
		return ChatMessageResponse.builder()
				.id(message.getId())
				.agitUuid(message.getAgitUuid())
				.senderUuid(message.getSenderUuid())
				.type(message.getType().name())
				.content(message.getContent())
				.payload(message.getPayload())
				.createdAt(message.getCreatedAt())
				.build();
	}
}
