package com.plip.chat.application.port.in;

import com.plip.chat.domain.model.AgitMemberRole;

import java.util.UUID;

public interface ProjectAgitEventUseCase {

	void onCreated(
			UUID agitUuid,
			String title,
			String description,
			int maxMembers,
			String thumbnailPath,
			UUID hostUserUuid,
			String hostNickname
	);

	void onUpdated(UUID agitUuid, String title, String description, int maxMembers, String thumbnailPath);

	void onMemberJoined(
			UUID agitUuid,
			UUID userUuid,
			String nickname,
			String profileImage,
			AgitMemberRole role
	);

	void onMemberLeft(UUID agitUuid, UUID userUuid);

	void onMemberBanned(UUID agitUuid, UUID userUuid, String nickname);

	void onMemberUnbanned(UUID agitUuid, UUID userUuid);

	void onMemberProfileUpdated(UUID agitUuid, UUID userUuid, String nickname, String profileImage);

	void onHostTransferred(
			UUID agitUuid,
			UUID previousHostUserUuid,
			UUID newHostUserUuid,
			String newHostNickname
	);

	void onDeleted(UUID agitUuid);
}
