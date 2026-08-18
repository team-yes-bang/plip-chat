package com.plip.chat.support;

import com.plip.chat.application.port.out.AgitReferencePersistencePort;
import com.plip.chat.application.port.out.AgitReferenceQueryPort;
import com.plip.chat.domain.model.AgitRoomReference;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryAgitReferencePersistence implements AgitReferencePersistencePort, AgitReferenceQueryPort {

	private final Map<UUID, AgitRoomReference> store = new ConcurrentHashMap<>();

	@Override
	public AgitRoomReference save(AgitRoomReference reference) {
		store.put(reference.getAgitUuid(), reference);
		return reference;
	}

	@Override
	public Optional<AgitRoomReference> findByAgitUuid(UUID agitUuid) {
		return Optional.ofNullable(store.get(agitUuid));
	}

	@Override
	public boolean isActiveMember(UUID agitUuid, UUID userUuid) {
		return findByAgitUuid(agitUuid)
				.map(reference -> reference.isActiveMember(userUuid))
				.orElse(false);
	}
}
