package com.plip.chat.adapter.out.persistence;

import com.plip.chat.application.port.out.ChatMessagePersistencePort;
import com.plip.chat.domain.model.ChatMessage;
import com.plip.chat.domain.model.MessageType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class ChatMessagePersistenceAdapter implements ChatMessagePersistencePort {

	private final ChatMessageMongoRepository chatMessageMongoRepository;
	private final ChatMessagePersistenceMapper chatMessagePersistenceMapper;
	private final MongoTemplate mongoTemplate;

	@Override
	public ChatMessage save(ChatMessage chatMessage) {
		ChatMessageMongoDocument saved = chatMessageMongoRepository.save(
				chatMessagePersistenceMapper.toDocument(chatMessage)
		);
		return chatMessagePersistenceMapper.toDomain(saved);
	}

	@Override
	public List<ChatMessage> findByAgitUuidOrderByCreatedAtDesc(UUID agitUuid) {
		return chatMessageMongoRepository.findByAgitUuidOrderByCreatedAtDesc(agitUuid.toString())
				.stream()
				.map(chatMessagePersistenceMapper::toDomain)
				.toList();
	}

	@Override
	public List<ChatMessage> findHistory(UUID agitUuid, Instant cursorCreatedAt, UUID cursorId, int limit) {
		Query query = new Query(Criteria.where("agitUuid").is(agitUuid.toString()));
		if (cursorCreatedAt != null && cursorId != null) {
			query.addCriteria(new Criteria().orOperator(
					Criteria.where("createdAt").lt(cursorCreatedAt),
					new Criteria().andOperator(
							Criteria.where("createdAt").is(cursorCreatedAt),
							Criteria.where("_id").lt(cursorId.toString())
					)
			));
		}
		query.with(Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("_id")));
		query.limit(limit);
		return mongoTemplate.find(query, ChatMessageMongoDocument.class).stream()
				.map(chatMessagePersistenceMapper::toDomain)
				.toList();
	}

	@Override
	public long countUnread(UUID agitUuid, UUID userUuid, Instant readAt) {
		Criteria criteria = Criteria.where("agitUuid").is(agitUuid.toString())
				.and("type").is(MessageType.TALK.name())
				.and("senderUuid").ne(userUuid.toString());
		if (readAt != null) {
			criteria = criteria.and("createdAt").gt(readAt);
		}
		return mongoTemplate.count(new Query(criteria), ChatMessageMongoDocument.class);
	}

	@Override
	public List<ChatMessage> findTalkByAgitAndCreatedAtRange(
			UUID agitUuid,
			Instant afterExclusive,
			Instant toInclusive,
			UUID excludeSenderUuid
	) {
		Criteria criteria = Criteria.where("agitUuid").is(agitUuid.toString())
				.and("type").is(MessageType.TALK.name())
				.and("senderUuid").ne(excludeSenderUuid.toString())
				.and("createdAt").lte(toInclusive);
		if (afterExclusive != null) {
			criteria = criteria.and("createdAt").gt(afterExclusive);
		}
		Query query = new Query(criteria);
		return mongoTemplate.find(query, ChatMessageMongoDocument.class).stream()
				.map(chatMessagePersistenceMapper::toDomain)
				.toList();
	}
}
