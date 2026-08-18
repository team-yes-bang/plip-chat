package com.plip.chat.application.port.in;

public final class SystemMessageEvents {

	public static final String MEMBER_JOINED = "agit.member-joined";
	public static final String MEMBER_BANNED = "agit.member-banned";
	public static final String TOPIC_BOUND = "topic.bound";
	public static final String TOPIC_STARTED = "topic.started";

	// TODO: agit.member-left — 자진 퇴장 시스템 메시지
	// TODO: agit.deleted — 아지트 삭제 시스템 메시지
	// TODO: topic.unbound — 토픽 해제 시스템 메시지
	// TODO: video.uploaded — 이벤트 스펙 확정 후 시스템 메시지

	private SystemMessageEvents() {
	}
}
