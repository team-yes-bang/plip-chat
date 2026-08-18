package com.plip.chat.domain.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AgitRoomReference {

	private UUID agitUuid;
	private String title;
	private String description;
	private int maxMembers;
	private String thumbnailPath;
	private AgitRoomStatus status;
	private List<AgitMemberReference> members;
	private Instant updatedAt;

	public static AgitRoomReference create(
			UUID agitUuid,
			String title,
			String description,
			int maxMembers,
			String thumbnailPath,
			UUID hostUserUuid,
			String hostNickname
	) {
		AgitMemberReference host = AgitMemberReference.of(
				hostUserUuid,
				hostNickname,
				null,
				AgitMemberRole.HOST,
				AgitMemberStatus.ACTIVE
		);
		return AgitRoomReference.builder()
				.agitUuid(requireAgitUuid(agitUuid))
				.title(nullToEmpty(title))
				.description(nullToEmpty(description))
				.maxMembers(maxMembers)
				.thumbnailPath(thumbnailPath)
				.status(AgitRoomStatus.ACTIVE)
				.members(List.of(host))
				.updatedAt(Instant.now())
				.build();
	}

	public static AgitRoomReference empty(UUID agitUuid) {
		return AgitRoomReference.builder()
				.agitUuid(requireAgitUuid(agitUuid))
				.title("")
				.description("")
				.maxMembers(0)
				.status(AgitRoomStatus.ACTIVE)
				.members(List.of())
				.updatedAt(Instant.now())
				.build();
	}

	public static AgitRoomReference reconstitute(
			UUID agitUuid,
			String title,
			String description,
			int maxMembers,
			String thumbnailPath,
			AgitRoomStatus status,
			List<AgitMemberReference> members,
			Instant updatedAt
	) {
		return AgitRoomReference.builder()
				.agitUuid(requireAgitUuid(agitUuid))
				.title(nullToEmpty(title))
				.description(nullToEmpty(description))
				.maxMembers(maxMembers)
				.thumbnailPath(thumbnailPath)
				.status(status == null ? AgitRoomStatus.ACTIVE : status)
				.members(members)
				.updatedAt(updatedAt == null ? Instant.now() : updatedAt)
				.build();
	}

	public AgitRoomReference updateMeta(String title, String description, int maxMembers, String thumbnailPath) {
		return copy()
				.title(nullToEmpty(title))
				.description(nullToEmpty(description))
				.maxMembers(maxMembers)
				.thumbnailPath(thumbnailPath)
				.updatedAt(Instant.now())
				.build();
	}

	public AgitRoomReference upsertMember(AgitMemberReference member) {
		List<AgitMemberReference> next = replaceMember(member);
		return copy().members(next).updatedAt(Instant.now()).build();
	}

	public AgitRoomReference updateMemberStatus(UUID userUuid, AgitMemberStatus status, String nickname) {
		AgitMemberReference current = findMember(userUuid)
				.orElseGet(() -> AgitMemberReference.of(
						userUuid,
						nickname,
						null,
						AgitMemberRole.GUEST,
						status
				));
		AgitMemberReference updated = current.withStatus(status);
		if (nickname != null && !nickname.isBlank()) {
			updated = updated.withProfile(nickname, current.getProfileImage());
		}
		return upsertMember(updated);
	}

	public AgitRoomReference updateMemberProfile(UUID userUuid, String nickname, String profileImage) {
		AgitMemberReference current = findMember(userUuid)
				.orElseGet(() -> AgitMemberReference.of(
						userUuid,
						nickname,
						profileImage,
						AgitMemberRole.GUEST,
						AgitMemberStatus.LEFT
				));
		return upsertMember(current.withProfile(nickname, profileImage));
	}

	public AgitRoomReference transferHost(UUID previousHostUserUuid, UUID newHostUserUuid, String newHostNickname) {
		AgitRoomReference demoted = findMember(previousHostUserUuid)
				.map(host -> upsertMember(host.withRole(AgitMemberRole.GUEST)))
				.orElse(this);
		AgitMemberReference newHost = demoted.findMember(newHostUserUuid)
				.map(member -> member.withRole(AgitMemberRole.HOST).withStatus(AgitMemberStatus.ACTIVE))
				.orElseGet(() -> AgitMemberReference.of(
						newHostUserUuid,
						newHostNickname,
						null,
						AgitMemberRole.HOST,
						AgitMemberStatus.ACTIVE
				));
		if (newHostNickname != null && !newHostNickname.isBlank()) {
			newHost = newHost.withProfile(newHostNickname, newHost.getProfileImage());
		}
		return demoted.upsertMember(newHost);
	}

	public AgitRoomReference markDeleted() {
		return copy().status(AgitRoomStatus.DELETED).updatedAt(Instant.now()).build();
	}

	public Optional<AgitMemberReference> findMember(UUID userUuid) {
		if (userUuid == null) {
			return Optional.empty();
		}
		return members.stream()
				.filter(member -> userUuid.equals(member.getUserUuid()))
				.findFirst();
	}

	public boolean isActiveMember(UUID userUuid) {
		if (status != AgitRoomStatus.ACTIVE) {
			return false;
		}
		return findMember(userUuid).map(AgitMemberReference::isActive).orElse(false);
	}

	private List<AgitMemberReference> replaceMember(AgitMemberReference member) {
		List<AgitMemberReference> next = new ArrayList<>();
		for (AgitMemberReference existing : members) {
			if (!existing.getUserUuid().equals(member.getUserUuid())) {
				next.add(existing);
			}
		}
		next.add(member);
		return List.copyOf(next);
	}

	private AgitRoomReference.AgitRoomReferenceBuilder copy() {
		return AgitRoomReference.builder()
				.agitUuid(agitUuid)
				.title(title)
				.description(description)
				.maxMembers(maxMembers)
				.thumbnailPath(thumbnailPath)
				.status(status)
				.members(members)
				.updatedAt(updatedAt);
	}

	private static UUID requireAgitUuid(UUID agitUuid) {
		if (agitUuid == null) {
			throw new IllegalArgumentException("agitUuid는 필수입니다.");
		}
		return agitUuid;
	}

	private static String nullToEmpty(String value) {
		return value == null ? "" : value;
	}

	@Builder(access = AccessLevel.PRIVATE)
	private AgitRoomReference(
			UUID agitUuid,
			String title,
			String description,
			int maxMembers,
			String thumbnailPath,
			AgitRoomStatus status,
			List<AgitMemberReference> members,
			Instant updatedAt
	) {
		this.agitUuid = agitUuid;
		this.title = title;
		this.description = description;
		this.maxMembers = maxMembers;
		this.thumbnailPath = thumbnailPath;
		this.status = status;
		this.members = members == null ? List.of() : List.copyOf(members);
		this.updatedAt = updatedAt;
	}
}
