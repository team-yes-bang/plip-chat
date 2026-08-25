package com.plip.chat.domain.event;

import java.time.Instant;
import java.util.UUID;

public record MemberReadUpdated(UUID agitUuid, UUID readerUuid, Instant readAt) {
}
