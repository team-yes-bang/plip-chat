package com.plip.chat.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMessageMongoRepository extends MongoRepository<ChatMessageMongoDocument, String> {

	List<ChatMessageMongoDocument> findByAgitUuidOrderByCreatedAtDesc(String agitUuid);
}
