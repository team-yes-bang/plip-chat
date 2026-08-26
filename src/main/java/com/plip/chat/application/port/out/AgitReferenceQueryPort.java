package com.plip.chat.application.port.out;

import com.plip.chat.domain.model.AgitMemberReference;
import com.plip.chat.domain.model.AgitRoomReference;

import java.util.Optional;
import java.util.UUID;

public interface AgitReferenceQueryPort {

	Optional<AgitRoomReference> findByAgitUuid(UUID agitUuid);

	boolean isActiveMember(UUID agitUuid, UUID userUuid);

	default int countActiveMembers(UUID agitUuid) {
		return findByAgitUuid(agitUuid)
				.map(room -> (int) room.getMembers().stream().filter(AgitMemberReference::isActive).count())
				.orElse(0);
	}
}
