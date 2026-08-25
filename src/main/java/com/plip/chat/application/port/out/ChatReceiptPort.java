package com.plip.chat.application.port.out;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

public interface ChatReceiptPort {

	void initUnreadMemberCount(UUID agitUuid, UUID messageId, int count);

	OptionalInt getUnreadMemberCount(UUID agitUuid, UUID messageId);

	Map<UUID, Integer> getUnreadMemberCounts(UUID agitUuid, Collection<UUID> messageIds);

	OptionalInt decrementUnreadMemberCount(UUID agitUuid, UUID messageId);
}
