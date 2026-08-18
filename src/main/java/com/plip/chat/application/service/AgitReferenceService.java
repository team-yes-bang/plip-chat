package com.plip.chat.application.service;

import com.plip.chat.application.port.in.ProjectAgitEventUseCase;
import com.plip.chat.application.port.out.AgitReferencePersistencePort;
import com.plip.chat.domain.model.AgitMemberReference;
import com.plip.chat.domain.model.AgitMemberRole;
import com.plip.chat.domain.model.AgitMemberStatus;
import com.plip.chat.domain.model.AgitRoomReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgitReferenceService implements ProjectAgitEventUseCase {

	private final AgitReferencePersistencePort agitReferencePersistencePort;

	@Override
	public void onCreated(
			UUID agitUuid,
			String title,
			String description,
			int maxMembers,
			String thumbnailPath,
			UUID hostUserUuid,
			String hostNickname
	) {
		AgitRoomReference created = AgitRoomReference.create(
				agitUuid,
				title,
				description,
				maxMembers,
				thumbnailPath,
				hostUserUuid,
				hostNickname
		);
		AgitRoomReference current = agitReferencePersistencePort.findByAgitUuid(agitUuid)
				.map(existing -> existing
						.updateMeta(title, description, maxMembers, thumbnailPath)
						.upsertMember(AgitMemberReference.of(
								hostUserUuid,
								hostNickname,
								existing.findMember(hostUserUuid).map(AgitMemberReference::getProfileImage).orElse(null),
								AgitMemberRole.HOST,
								AgitMemberStatus.ACTIVE
						)))
				.orElse(created);
		agitReferencePersistencePort.save(current);
	}

	@Override
	public void onUpdated(UUID agitUuid, String title, String description, int maxMembers, String thumbnailPath) {
		agitReferencePersistencePort.findByAgitUuid(agitUuid)
				.map(existing -> existing.updateMeta(title, description, maxMembers, thumbnailPath))
				.ifPresentOrElse(
						agitReferencePersistencePort::save,
						() -> log.warn("agit.updated skip: 문서 없음 agitUuid={}", agitUuid)
				);
	}

	@Override
	public void onMemberJoined(
			UUID agitUuid,
			UUID userUuid,
			String nickname,
			String profileImage,
			AgitMemberRole role
	) {
		AgitRoomReference current = loadOrEmpty(agitUuid).upsertMember(AgitMemberReference.of(
				userUuid,
				nickname,
				profileImage,
				role,
				AgitMemberStatus.ACTIVE
		));
		agitReferencePersistencePort.save(current);
	}

	@Override
	public void onMemberLeft(UUID agitUuid, UUID userUuid) {
		saveStatus(agitUuid, userUuid, AgitMemberStatus.LEFT, null);
	}

	@Override
	public void onMemberBanned(UUID agitUuid, UUID userUuid, String nickname) {
		saveStatus(agitUuid, userUuid, AgitMemberStatus.BANNED, nickname);
	}

	@Override
	public void onMemberUnbanned(UUID agitUuid, UUID userUuid) {
		saveStatus(agitUuid, userUuid, AgitMemberStatus.LEFT, null);
	}

	@Override
	public void onMemberProfileUpdated(UUID agitUuid, UUID userUuid, String nickname, String profileImage) {
		AgitRoomReference current = loadOrEmpty(agitUuid).updateMemberProfile(userUuid, nickname, profileImage);
		agitReferencePersistencePort.save(current);
	}

	@Override
	public void onHostTransferred(
			UUID agitUuid,
			UUID previousHostUserUuid,
			UUID newHostUserUuid,
			String newHostNickname
	) {
		AgitRoomReference current = loadOrEmpty(agitUuid)
				.transferHost(previousHostUserUuid, newHostUserUuid, newHostNickname);
		agitReferencePersistencePort.save(current);
	}

	@Override
	public void onDeleted(UUID agitUuid) {
		agitReferencePersistencePort.findByAgitUuid(agitUuid)
				.map(AgitRoomReference::markDeleted)
				.ifPresentOrElse(
						agitReferencePersistencePort::save,
						() -> log.warn("agit.deleted skip: 문서 없음 agitUuid={}", agitUuid)
				);
	}

	private void saveStatus(UUID agitUuid, UUID userUuid, AgitMemberStatus status, String nickname) {
		AgitRoomReference current = loadOrEmpty(agitUuid).updateMemberStatus(userUuid, status, nickname);
		agitReferencePersistencePort.save(current);
	}

	private AgitRoomReference loadOrEmpty(UUID agitUuid) {
		return agitReferencePersistencePort.findByAgitUuid(agitUuid)
				.orElseGet(() -> AgitRoomReference.empty(agitUuid));
	}
}
