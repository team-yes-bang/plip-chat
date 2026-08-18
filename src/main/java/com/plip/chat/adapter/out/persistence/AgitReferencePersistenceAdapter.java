package com.plip.chat.adapter.out.persistence;

import com.plip.chat.application.port.out.AgitReferencePersistencePort;
import com.plip.chat.application.port.out.AgitReferenceQueryPort;
import com.plip.chat.domain.model.AgitRoomReference;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class AgitReferencePersistenceAdapter implements AgitReferencePersistencePort, AgitReferenceQueryPort {

	private final AgitRoomMongoRepository agitRoomMongoRepository;
	private final AgitReferencePersistenceMapper agitReferencePersistenceMapper;

	@Override
	public AgitRoomReference save(AgitRoomReference reference) {
		AgitRoomMongoDocument saved = agitRoomMongoRepository.save(
				agitReferencePersistenceMapper.toDocument(reference)
		);
		return agitReferencePersistenceMapper.toDomain(saved);
	}

	@Override
	public Optional<AgitRoomReference> findByAgitUuid(UUID agitUuid) {
		return agitRoomMongoRepository.findById(agitUuid.toString())
				.map(agitReferencePersistenceMapper::toDomain);
	}

	@Override
	public boolean isActiveMember(UUID agitUuid, UUID userUuid) {
		return findByAgitUuid(agitUuid)
				.map(reference -> reference.isActiveMember(userUuid))
				.orElse(false);
	}
}
