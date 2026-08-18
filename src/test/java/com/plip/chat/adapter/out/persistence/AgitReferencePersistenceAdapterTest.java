package com.plip.chat.adapter.out.persistence;

import com.plip.chat.domain.model.AgitMemberReference;
import com.plip.chat.domain.model.AgitMemberRole;
import com.plip.chat.domain.model.AgitMemberStatus;
import com.plip.chat.domain.model.AgitRoomReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AgitReferencePersistenceAdapterTest {

	@Mock
	private AgitRoomMongoRepository agitRoomMongoRepository;

	private AgitReferencePersistenceAdapter adapter;

	@BeforeEach
	void setUp() {
		adapter = new AgitReferencePersistenceAdapter(
				agitRoomMongoRepository,
				new AgitReferencePersistenceMapper()
		);
	}

	@Test
	void isActiveMember_trueOnlyWhenRoomAndMemberAreActive() {
		UUID agitUuid = UUID.randomUUID();
		UUID hostUuid = UUID.randomUUID();
		UUID bannedUuid = UUID.randomUUID();
		AgitRoomReference reference = AgitRoomReference.create(
				agitUuid, "아지트", "", 5, null, hostUuid, "호스트"
		).upsertMember(AgitMemberReference.of(
				bannedUuid, "밴", null, AgitMemberRole.GUEST, AgitMemberStatus.BANNED
		));
		given(agitRoomMongoRepository.findById(agitUuid.toString()))
				.willReturn(Optional.of(new AgitReferencePersistenceMapper().toDocument(reference)));

		assertThat(adapter.isActiveMember(agitUuid, hostUuid)).isTrue();
		assertThat(adapter.isActiveMember(agitUuid, bannedUuid)).isFalse();
		assertThat(adapter.isActiveMember(agitUuid, UUID.randomUUID())).isFalse();
	}

	@Test
	void findByAgitUuid_emptyWhenMissing() {
		UUID agitUuid = UUID.randomUUID();
		given(agitRoomMongoRepository.findById(agitUuid.toString())).willReturn(Optional.empty());

		assertThat(adapter.findByAgitUuid(agitUuid)).isEmpty();
		assertThat(adapter.isActiveMember(agitUuid, UUID.randomUUID())).isFalse();
	}
}
