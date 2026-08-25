package com.plip.chat.application.port.out;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public interface ChatWsTicketPort {

	String issue(UUID userUuid, Duration ttl);

	Optional<UUID> consume(String ticket);
}
