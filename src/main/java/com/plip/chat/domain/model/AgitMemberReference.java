package com.plip.chat.domain.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AgitMemberReference {

	private UUID userUuid;
	private String nickname;
	private String profileImage;
	private AgitMemberRole role;
	private AgitMemberStatus status;

	public static AgitMemberReference of(
			UUID userUuid,
			String nickname,
			String profileImage,
			AgitMemberRole role,
			AgitMemberStatus status
	) {
		if (userUuid == null) {
			throw new IllegalArgumentException("userUuid는 필수입니다.");
		}
		return AgitMemberReference.builder()
				.userUuid(userUuid)
				.nickname(nickname == null ? "" : nickname)
				.profileImage(profileImage)
				.role(role == null ? AgitMemberRole.GUEST : role)
				.status(status == null ? AgitMemberStatus.ACTIVE : status)
				.build();
	}

	public AgitMemberReference withStatus(AgitMemberStatus status) {
		return of(userUuid, nickname, profileImage, role, status);
	}

	public AgitMemberReference withRole(AgitMemberRole role) {
		return of(userUuid, nickname, profileImage, role, status);
	}

	public AgitMemberReference withProfile(String nickname, String profileImage) {
		return of(userUuid, nickname, profileImage, role, status);
	}

	public boolean isActive() {
		return status == AgitMemberStatus.ACTIVE;
	}

	@Builder(access = AccessLevel.PRIVATE)
	private AgitMemberReference(
			UUID userUuid,
			String nickname,
			String profileImage,
			AgitMemberRole role,
			AgitMemberStatus status
	) {
		this.userUuid = userUuid;
		this.nickname = nickname;
		this.profileImage = profileImage;
		this.role = role;
		this.status = status;
	}
}
