package com.plip.chat.application.port.out;

import com.plip.chat.domain.model.AgitRoomReference;

import java.util.Optional;
import java.util.UUID;

public interface AgitReferencePersistencePort {

	AgitRoomReference save(AgitRoomReference reference);

	Optional<AgitRoomReference> findByAgitUuid(UUID agitUuid);
}
