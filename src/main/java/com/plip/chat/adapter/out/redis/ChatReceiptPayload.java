package com.plip.chat.adapter.out.redis;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatReceiptPayload {

	private UUID messageId;
	private int unreadMemberCount;
}
