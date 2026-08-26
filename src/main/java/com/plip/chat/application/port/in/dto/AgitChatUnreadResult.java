package com.plip.chat.application.port.in.dto;

import java.util.UUID;

public record AgitChatUnreadResult(UUID agitUuid, long unreadMessageCount) {
}
