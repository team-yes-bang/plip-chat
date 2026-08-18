package com.plip.chat.adapter.out.persistence;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AgitMemberMongoDocument {

	private String userUuid;
	private String nickname;
	private String profileImage;
	private String role;
	private String status;

	public AgitMemberMongoDocument(
			String userUuid,
			String nickname,
			String profileImage,
			String role,
			String status
	) {
		this.userUuid = userUuid;
		this.nickname = nickname;
		this.profileImage = profileImage;
		this.role = role;
		this.status = status;
	}
}
