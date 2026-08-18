package com.plip.chat.adapter.out.persistence;

import com.plip.chat.domain.model.AgitMemberReference;
import com.plip.chat.domain.model.AgitMemberRole;
import com.plip.chat.domain.model.AgitMemberStatus;
import com.plip.chat.domain.model.AgitRoomReference;
import com.plip.chat.domain.model.AgitRoomStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class AgitReferencePersistenceMapper {

	public AgitRoomMongoDocument toDocument(AgitRoomReference reference) {
		List<AgitMemberMongoDocument> members = reference.getMembers().stream()
				.map(this::toMemberDocument)
				.toList();
		return new AgitRoomMongoDocument(
				reference.getAgitUuid().toString(),
				reference.getTitle(),
				reference.getDescription(),
				reference.getMaxMembers(),
				reference.getThumbnailPath(),
				reference.getStatus().name(),
				members,
				reference.getUpdatedAt()
		);
	}

	public AgitRoomReference toDomain(AgitRoomMongoDocument document) {
		List<AgitMemberReference> members = (document.getMembers() == null
				? List.<AgitMemberMongoDocument>of()
				: document.getMembers())
				.stream()
				.map(this::toMemberDomain)
				.toList();
		return AgitRoomReference.reconstitute(
				UUID.fromString(document.getId()),
				document.getTitle(),
				document.getDescription(),
				document.getMaxMembers(),
				document.getThumbnailPath(),
				AgitRoomStatus.valueOf(document.getStatus()),
				members,
				document.getUpdatedAt()
		);
	}

	private AgitMemberMongoDocument toMemberDocument(AgitMemberReference member) {
		return new AgitMemberMongoDocument(
				member.getUserUuid().toString(),
				member.getNickname(),
				member.getProfileImage(),
				member.getRole().name(),
				member.getStatus().name()
		);
	}

	private AgitMemberReference toMemberDomain(AgitMemberMongoDocument document) {
		return AgitMemberReference.of(
				UUID.fromString(document.getUserUuid()),
				document.getNickname(),
				document.getProfileImage(),
				AgitMemberRole.valueOf(document.getRole()),
				AgitMemberStatus.valueOf(document.getStatus())
		);
	}
}
