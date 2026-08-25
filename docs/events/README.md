# Chat 구독 이벤트

원본 스펙은 agit / topic 레포 `docs/events/`에 있습니다. 이 문서는 Chat이 **구독**하는 토픽만 정리합니다.

## 읽기 모델 투영 (group: `chat-agit-read-model`)

| Topic | 용도 |
| --- | --- |
| `agit.created` | 아지트 문서 생성 |
| `agit.updated` | 메타 갱신 |
| `agit.member-joined` | 멤버 ACTIVE |
| `agit.member-left` | 멤버 LEFT |
| `agit.member-banned` | 멤버 BANNED |
| `agit.member-unbanned` | 멤버 LEFT (Agit 스펙 수렴) |
| `agit.member-profile-updated` | 닉네임/이미지 |
| `agit.host-transferred` | 호스트 역할 교체 |
| `agit.deleted` | 문서 DELETED |

## 시스템 메시지 (group: `chat-system-message`)

Mongo `type=SYSTEM` 적재 후 Redis `chat:agit:{agitUuid}`로 브로드캐스트합니다. **`last_chat_at`은 갱신하지 않습니다.**

| Topic | 채팅 본문 |
| --- | --- |
| `agit.member-joined` | `{nickname}님이 입장했습니다.` |
| `agit.member-banned` | `{nickname}님이 내보내졌습니다.` |
| `agit.member-left` | `{nickname}님이 퇴장했습니다.` (읽기 모델 nickname) |
| `topic.bound` | `토픽이 연결되었습니다.` |
| `topic.started` | `토픽이 시작되었습니다.` |

payload 예: `eventType`, `userUuid`/`nickname` 또는 `topicId`.

## 미구독 (의도적)

| Topic | 사유 |
| --- | --- |
| `agit.deleted` | 삭제 시 read model `DELETED` → 채팅 REST/WS 403. SYSTEM을 볼 ACTIVE 멤버 없음 |
| `topic.unbound` | bound/started와 달리 채팅 UX상 SYSTEM 불필요 |
| `video.uploaded` | 이벤트 스펙 확정 후 |
