package com.plip.chat.adapter.out.persistence;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "agit_documents")
@CompoundIndex(name = "idx_agit_documents_members_userUuid", def = "{'members.userUuid': 1}")
public class AgitRoomMongoDocument {

	@Id
	private String id;
	private String title;
	private String description;
	private int maxMembers;
	private String thumbnailPath;
	private String status;
	private List<AgitMemberMongoDocument> members = new ArrayList<>();
	private Instant updatedAt;

	public AgitRoomMongoDocument(
			String id,
			String title,
			String description,
			int maxMembers,
			String thumbnailPath,
			String status,
			List<AgitMemberMongoDocument> members,
			Instant updatedAt
	) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.maxMembers = maxMembers;
		this.thumbnailPath = thumbnailPath;
		this.status = status;
		this.members = members != null ? members : new ArrayList<>();
		this.updatedAt = updatedAt;
	}
}
