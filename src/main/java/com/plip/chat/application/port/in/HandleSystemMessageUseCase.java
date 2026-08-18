package com.plip.chat.application.port.in;

import java.util.UUID;

public interface HandleSystemMessageUseCase {

	void onMemberJoined(UUID agitUuid, UUID userUuid, String nickname);

	void onMemberBanned(UUID agitUuid, UUID userUuid, String nickname);

	void onTopicBound(UUID agitUuid, String topicId);

	void onTopicStarted(UUID agitUuid, String topicId);

	// TODO: onMemberLeft(agitUuid, userUuid, nickname) — agit.member-left
	// TODO: onAgitDeleted(agitUuid) — agit.deleted
	// TODO: onTopicUnbound(agitUuid, topicId) — topic.unbound
	// TODO: onVideoUploaded(...) — video.uploaded (스펙 확정 후)
}
