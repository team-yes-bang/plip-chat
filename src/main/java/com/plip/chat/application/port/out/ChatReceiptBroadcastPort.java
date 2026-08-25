package com.plip.chat.application.port.out;

import java.util.UUID;

public interface ChatReceiptBroadcastPort {

	void publishReceiptUpdate(UUID agitUuid, UUID messageId, int unreadMemberCount);
}
