package com.plip.chat.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AgitRoomMongoRepository extends MongoRepository<AgitRoomMongoDocument, String> {
}
