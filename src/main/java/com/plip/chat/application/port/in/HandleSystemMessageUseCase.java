package com.plip.chat.application.port.in;

import java.util.UUID;

public interface HandleSystemMessageUseCase {

	void onMemberJoined(UUID agitUuid, UUID userUuid, String nickname);

	void onMemberBanned(UUID agitUuid, UUID userUuid, String nickname);

	void onTopicBound(UUID agitUuid, String topicId);

	void onTopicStarted(UUID agitUuid, String topicId);

	void onMemberLeft(UUID agitUuid, UUID userUuid);
}
