package com.plip.chat.application.service;

import com.plip.chat.domain.model.AgitMemberReference;
import com.plip.chat.domain.model.AgitMemberRole;
import com.plip.chat.domain.model.AgitMemberStatus;
import com.plip.chat.domain.model.AgitRoomReference;
import com.plip.chat.domain.model.AgitRoomStatus;
import com.plip.chat.support.InMemoryAgitReferencePersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AgitReferenceServiceTest {

	private InMemoryAgitReferencePersistence store;
	private AgitReferenceService agitReferenceService;

	private UUID agitUuid;
	private UUID hostUuid;
	private UUID guestUuid;

	@BeforeEach
	void setUp() {
		store = new InMemoryAgitReferencePersistence();
		agitReferenceService = new AgitReferenceService(store);
		agitUuid = UUID.randomUUID();
		hostUuid = UUID.randomUUID();
		guestUuid = UUID.randomUUID();
	}

	@Test
	void onCreated_projectsHostAsActiveHost() {
		agitReferenceService.onCreated(agitUuid, "주말 보드게임", "소개", 5, null, hostUuid, "보드왕");

		AgitRoomReference saved = store.findByAgitUuid(agitUuid).orElseThrow();
		assertThat(saved.getTitle()).isEqualTo("주말 보드게임");
		assertThat(saved.getMaxMembers()).isEqualTo(5);
		assertThat(saved.getStatus()).isEqualTo(AgitRoomStatus.ACTIVE);
		assertThat(store.isActiveMember(agitUuid, hostUuid)).isTrue();
		AgitMemberReference host = saved.findMember(hostUuid).orElseThrow();
		assertThat(host.getRole()).isEqualTo(AgitMemberRole.HOST);
		assertThat(host.getStatus()).isEqualTo(AgitMemberStatus.ACTIVE);
	}

	@Test
	void onUpdated_changesTitleAndCapacity() {
		agitReferenceService.onCreated(agitUuid, "이전", "소개", 5, null, hostUuid, "보드왕");

		agitReferenceService.onUpdated(agitUuid, "주말 보드게임", "새 소개", 8, "thumb.png");

		AgitRoomReference saved = store.findByAgitUuid(agitUuid).orElseThrow();
		assertThat(saved.getTitle()).isEqualTo("주말 보드게임");
		assertThat(saved.getDescription()).isEqualTo("새 소개");
		assertThat(saved.getMaxMembers()).isEqualTo(8);
		assertThat(saved.getThumbnailPath()).isEqualTo("thumb.png");
	}

	@Test
	void bannedAndLeftMembersAreNotActive() {
		agitReferenceService.onCreated(agitUuid, "아지트", "", 5, null, hostUuid, "호스트");
		agitReferenceService.onMemberJoined(agitUuid, guestUuid, "게스트", "profiles/a.png", AgitMemberRole.GUEST);
		assertThat(store.isActiveMember(agitUuid, guestUuid)).isTrue();

		agitReferenceService.onMemberBanned(agitUuid, guestUuid, "게스트");
		assertThat(store.isActiveMember(agitUuid, guestUuid)).isFalse();
		assertThat(store.findByAgitUuid(agitUuid).orElseThrow().findMember(guestUuid).orElseThrow().getStatus())
				.isEqualTo(AgitMemberStatus.BANNED);

		UUID leftUuid = UUID.randomUUID();
		agitReferenceService.onMemberJoined(agitUuid, leftUuid, "나갈사람", null, AgitMemberRole.GUEST);
		agitReferenceService.onMemberLeft(agitUuid, leftUuid);
		assertThat(store.isActiveMember(agitUuid, leftUuid)).isFalse();
		assertThat(store.findByAgitUuid(agitUuid).orElseThrow().findMember(leftUuid).orElseThrow().getStatus())
				.isEqualTo(AgitMemberStatus.LEFT);
	}

	@Test
	void onMemberUnbanned_convergesToLeftNotActive() {
		agitReferenceService.onCreated(agitUuid, "아지트", "", 5, null, hostUuid, "호스트");
		agitReferenceService.onMemberJoined(agitUuid, guestUuid, "게스트", null, AgitMemberRole.GUEST);
		agitReferenceService.onMemberBanned(agitUuid, guestUuid, "게스트");

		agitReferenceService.onMemberUnbanned(agitUuid, guestUuid);

		assertThat(store.isActiveMember(agitUuid, guestUuid)).isFalse();
		assertThat(store.findByAgitUuid(agitUuid).orElseThrow().findMember(guestUuid).orElseThrow().getStatus())
				.isEqualTo(AgitMemberStatus.LEFT);
	}

	@Test
	void onMemberJoined_reactivatesLeftMember() {
		agitReferenceService.onCreated(agitUuid, "아지트", "", 5, null, hostUuid, "호스트");
		agitReferenceService.onMemberJoined(agitUuid, guestUuid, "게스트", null, AgitMemberRole.GUEST);
		agitReferenceService.onMemberLeft(agitUuid, guestUuid);

		agitReferenceService.onMemberJoined(agitUuid, guestUuid, "게스트", "profiles/b.png", AgitMemberRole.GUEST);

		assertThat(store.isActiveMember(agitUuid, guestUuid)).isTrue();
	}

	@Test
	void onHostTransferred_swapsRoles() {
		agitReferenceService.onCreated(agitUuid, "아지트", "", 5, null, hostUuid, "호스트");
		agitReferenceService.onMemberJoined(agitUuid, guestUuid, "게스트", null, AgitMemberRole.GUEST);

		agitReferenceService.onHostTransferred(agitUuid, hostUuid, guestUuid, "게스트");

		AgitRoomReference saved = store.findByAgitUuid(agitUuid).orElseThrow();
		assertThat(saved.findMember(hostUuid).orElseThrow().getRole()).isEqualTo(AgitMemberRole.GUEST);
		assertThat(saved.findMember(guestUuid).orElseThrow().getRole()).isEqualTo(AgitMemberRole.HOST);
		assertThat(store.isActiveMember(agitUuid, guestUuid)).isTrue();
	}

	@Test
	void onDeleted_marksRoomDeletedAndMembersInactive() {
		agitReferenceService.onCreated(agitUuid, "아지트", "", 5, null, hostUuid, "호스트");

		agitReferenceService.onDeleted(agitUuid);

		assertThat(store.findByAgitUuid(agitUuid).orElseThrow().getStatus()).isEqualTo(AgitRoomStatus.DELETED);
		assertThat(store.isActiveMember(agitUuid, hostUuid)).isFalse();
	}

	@Test
	void onMemberProfileUpdated_changesNicknameAndImage() {
		agitReferenceService.onCreated(agitUuid, "아지트", "", 5, null, hostUuid, "호스트");

		agitReferenceService.onMemberProfileUpdated(agitUuid, hostUuid, "새닉", "profiles/new.png");

		AgitMemberReference host = store.findByAgitUuid(agitUuid).orElseThrow().findMember(hostUuid).orElseThrow();
		assertThat(host.getNickname()).isEqualTo("새닉");
		assertThat(host.getProfileImage()).isEqualTo("profiles/new.png");
	}
}
