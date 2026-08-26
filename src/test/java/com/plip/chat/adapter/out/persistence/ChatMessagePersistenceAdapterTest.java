package com.plip.chat.adapter.out.persistence;

import com.plip.chat.domain.model.ChatMessage;
import com.plip.chat.domain.model.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChatMessagePersistenceAdapterTest {

	@Mock
	private ChatMessageMongoRepository chatMessageMongoRepository;

	@Mock
	private MongoTemplate mongoTemplate;

	private ChatMessagePersistenceAdapter chatMessagePersistenceAdapter;

	@BeforeEach
	void setUp() {
		chatMessagePersistenceAdapter = new ChatMessagePersistenceAdapter(
				chatMessageMongoRepository,
				new ChatMessagePersistenceMapper(),
				mongoTemplate
		);
	}

	@Test
	void save_mapsTalkMessageToDocument() {
		ChatMessage talk = ChatMessage.talk(UUID.randomUUID(), UUID.randomUUID(), "안녕하세요");
		given(chatMessageMongoRepository.save(any(ChatMessageMongoDocument.class)))
				.willAnswer(invocation -> invocation.getArgument(0));

		ChatMessage saved = chatMessagePersistenceAdapter.save(talk);

		assertThat(saved.getId()).isEqualTo(talk.getId());
		assertThat(saved.getAgitUuid()).isEqualTo(talk.getAgitUuid());
		assertThat(saved.getSenderUuid()).isEqualTo(talk.getSenderUuid());
		assertThat(saved.getType()).isEqualTo(MessageType.TALK);
		assertThat(saved.getContent()).isEqualTo("안녕하세요");
	}

	@Test
	void save_storesSystemSenderAsLiteral() {
		ChatMessage system = ChatMessage.system(
				UUID.randomUUID(),
				"멤버가 입장했습니다.",
				Map.of("eventType", "agit.member-joined")
		);
		given(chatMessageMongoRepository.save(any(ChatMessageMongoDocument.class)))
				.willAnswer(invocation -> invocation.getArgument(0));

		ChatMessage saved = chatMessagePersistenceAdapter.save(system);

		assertThat(saved.getSenderUuid()).isNull();
		assertThat(saved.getType()).isEqualTo(MessageType.SYSTEM);
		verify(chatMessageMongoRepository).save(argThat(document ->
				ChatMessage.SYSTEM_SENDER.equals(document.getSenderUuid())
						&& MessageType.SYSTEM.name().equals(document.getType())
		));
	}

	@Test
	void findByAgitUuidOrderByCreatedAtDesc_mapsDocuments() {
		UUID agitUuid = UUID.randomUUID();
		Instant newerAt = Instant.parse("2026-08-18T02:00:00Z");
		Instant olderAt = Instant.parse("2026-08-18T01:00:00Z");
		ChatMessageMongoDocument newer = new ChatMessageMongoDocument(
				UUID.randomUUID().toString(),
				agitUuid.toString(),
				UUID.randomUUID().toString(),
				MessageType.TALK.name(),
				"최근",
				Map.of(),
				newerAt
		);
		ChatMessageMongoDocument older = new ChatMessageMongoDocument(
				UUID.randomUUID().toString(),
				agitUuid.toString(),
				UUID.randomUUID().toString(),
				MessageType.TALK.name(),
				"이전",
				Map.of(),
				olderAt
		);
		given(chatMessageMongoRepository.findByAgitUuidOrderByCreatedAtDesc(agitUuid.toString()))
				.willReturn(List.of(newer, older));

		List<ChatMessage> messages = chatMessagePersistenceAdapter.findByAgitUuidOrderByCreatedAtDesc(agitUuid);

		assertThat(messages).extracting(ChatMessage::getContent).containsExactly("최근", "이전");
		assertThat(messages).extracting(ChatMessage::getCreatedAt).containsExactly(newerAt, olderAt);
	}

	@Test
	void findTalkByAgitAndCreatedAtRange_usesAndOperatorForCreatedAtRange() {
		UUID agitUuid = UUID.randomUUID();
		UUID readerUuid = UUID.randomUUID();
		Instant afterExclusive = Instant.parse("2026-08-26T00:47:53.131Z");
		Instant toInclusive = Instant.parse("2026-08-26T01:06:07.989Z");
		given(mongoTemplate.find(any(Query.class), eq(ChatMessageMongoDocument.class))).willReturn(List.of());

		chatMessagePersistenceAdapter.findTalkByAgitAndCreatedAtRange(
				agitUuid,
				afterExclusive,
				toInclusive,
				readerUuid
		);

		verify(mongoTemplate).find(argThat(query -> {
			String queryString = query.getQueryObject().toString();
			return queryString.contains("createdAt")
					&& queryString.contains("$gt")
					&& queryString.contains("$lte");
		}), eq(ChatMessageMongoDocument.class));
	}
}
