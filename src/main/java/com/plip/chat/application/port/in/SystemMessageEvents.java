package com.plip.chat.application.port.in;

public final class SystemMessageEvents {

	public static final String MEMBER_JOINED = "agit.member-joined";
	public static final String MEMBER_BANNED = "agit.member-banned";
	public static final String MEMBER_LEFT = "agit.member-left";
	public static final String TOPIC_BOUND = "topic.bound";
	public static final String TOPIC_STARTED = "topic.started";

	// agit.deleted — SYSTEM 미생성: 삭제 시 read model DELETED → 채팅 REST/WS 403, 수신자 없음
	// topic.unbound — SYSTEM 미생성: bound/started와 달리 채팅 UX상 불필요
	// TODO: video.uploaded — 이벤트 스펙 확정 후 시스템 메시지

	private SystemMessageEvents() {
	}
}
