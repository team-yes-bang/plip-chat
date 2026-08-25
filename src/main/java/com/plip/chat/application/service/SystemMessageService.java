package com.plip.chat.application.service;

import com.plip.chat.application.port.in.HandleSystemMessageUseCase;
import com.plip.chat.application.port.in.SystemMessageEvents;
import com.plip.chat.application.port.out.AgitReferenceQueryPort;
import com.plip.chat.application.port.out.ChatBroadcastPort;
import com.plip.chat.application.port.out.ChatMessagePersistencePort;
import com.plip.chat.domain.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SystemMessageService implements HandleSystemMessageUseCase {

	private final ChatMessagePersistencePort chatMessagePersistencePort;
	private final ChatBroadcastPort chatBroadcastPort;
	private final AgitReferenceQueryPort agitReferenceQueryPort;

	@Override
	public void onMemberJoined(UUID agitUuid, UUID userUuid, String nickname) {
		requireAgitUuid(agitUuid);
		requireUserUuid(userUuid);
		String displayName = requireNickname(nickname);
		saveAndBroadcast(
				agitUuid,
				displayName + "님이 입장했습니다.",
				payload(SystemMessageEvents.MEMBER_JOINED, userUuid, displayName, null)
		);
	}

	@Override
	public void onMemberBanned(UUID agitUuid, UUID userUuid, String nickname) {
		requireAgitUuid(agitUuid);
		requireUserUuid(userUuid);
		String displayName = requireNickname(nickname);
		saveAndBroadcast(
				agitUuid,
				displayName + "님이 내보내졌습니다.",
				payload(SystemMessageEvents.MEMBER_BANNED, userUuid, displayName, null)
		);
	}

	@Override
	public void onTopicBound(UUID agitUuid, String topicId) {
		requireAgitUuid(agitUuid);
		saveAndBroadcast(
				agitUuid,
				"토픽이 연결되었습니다.",
				payload(SystemMessageEvents.TOPIC_BOUND, null, null, requireTopicId(topicId))
		);
	}

	@Override
	public void onTopicStarted(UUID agitUuid, String topicId) {
		requireAgitUuid(agitUuid);
		saveAndBroadcast(
				agitUuid,
				"토픽이 시작되었습니다.",
				payload(SystemMessageEvents.TOPIC_STARTED, null, null, requireTopicId(topicId))
		);
	}

	@Override
	public void onMemberLeft(UUID agitUuid, UUID userUuid) {
		requireAgitUuid(agitUuid);
		requireUserUuid(userUuid);
		String displayName = requireNickname(resolveMemberNickname(agitUuid, userUuid));
		saveAndBroadcast(
				agitUuid,
				displayName + "님이 퇴장했습니다.",
				payload(SystemMessageEvents.MEMBER_LEFT, userUuid, displayName, null)
		);
	}

	private String resolveMemberNickname(UUID agitUuid, UUID userUuid) {
		return agitReferenceQueryPort.findByAgitUuid(agitUuid)
				.flatMap(room -> room.findMember(userUuid))
				.map(member -> member.getNickname())
				.filter(nickname -> nickname != null && !nickname.isBlank())
				.orElse(null);
	}

	private void saveAndBroadcast(UUID agitUuid, String content, Map<String, Object> payload) {
		ChatMessage saved = chatMessagePersistencePort.save(ChatMessage.system(agitUuid, content, payload));
		chatBroadcastPort.publish(saved);
		// last_chat_at은 TALK만 갱신한다. 시스템 메시지는 Red Dot을 만들지 않는다.
	}

	private static Map<String, Object> payload(String eventType, UUID userUuid, String nickname, String topicId) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("eventType", eventType);
		if (userUuid != null) {
			payload.put("userUuid", userUuid.toString());
		}
		if (nickname != null) {
			payload.put("nickname", nickname);
		}
		if (topicId != null) {
			payload.put("topicId", topicId);
		}
		return payload;
	}

	private static void requireAgitUuid(UUID agitUuid) {
		if (agitUuid == null) {
			throw new IllegalArgumentException("agitUuid는 필수입니다.");
		}
	}

	private static void requireUserUuid(UUID userUuid) {
		if (userUuid == null) {
			throw new IllegalArgumentException("userUuid는 필수입니다.");
		}
	}

	private static String requireNickname(String nickname) {
		if (nickname == null || nickname.isBlank()) {
			throw new IllegalArgumentException("nickname은 필수입니다.");
		}
		return nickname;
	}

	private static String requireTopicId(String topicId) {
		if (topicId == null || topicId.isBlank()) {
			throw new IllegalArgumentException("topicId는 필수입니다.");
		}
		return topicId;
	}
}
