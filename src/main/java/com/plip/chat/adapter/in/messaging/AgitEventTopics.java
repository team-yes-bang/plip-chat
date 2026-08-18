package com.plip.chat.adapter.in.messaging;

public final class AgitEventTopics {

	public static final String CREATED = "agit.created";
	public static final String UPDATED = "agit.updated";
	public static final String MEMBER_JOINED = "agit.member-joined";
	public static final String MEMBER_LEFT = "agit.member-left";
	public static final String MEMBER_BANNED = "agit.member-banned";
	public static final String MEMBER_UNBANNED = "agit.member-unbanned";
	public static final String MEMBER_PROFILE_UPDATED = "agit.member-profile-updated";
	public static final String HOST_TRANSFERRED = "agit.host-transferred";
	public static final String DELETED = "agit.deleted";

	public static final String CONSUMER_GROUP = "chat-agit-read-model";

	private AgitEventTopics() {
	}
}
