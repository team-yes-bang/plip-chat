# Chat WebSocket (Gateway 경유 · 일회용 ticket)

## 경로

| 구간 | URL |
| --- | --- |
| 티켓 발급 (REST) | `{gateway}/api/chat/api/v1/ws/ticket` |
| SockJS (WS) | `{gateway}/api/chat/ws/chat?ticket={ticket}` |
| Gateway → chat-service | StripPrefix=2 → `/ws/chat` |

REST·WS 모두 `/api/chat/**` 라우트를 사용합니다.

## 인증 흐름

1. 클라이언트가 **Bearer JWT**로 `POST /api/v1/ws/ticket` 호출 (Gateway → `X-User-UUID` 주입).
2. chat-service가 Redis에 **30초 TTL·1회용** ticket 저장 후 반환.
3. 클라이언트가 `{gateway}/api/chat/ws/chat?ticket=...` 로 SockJS 연결.
4. Gateway는 WS handshake를 **JWT 없이** 프록시할 수 있음 (ticket 검증은 chat-service).
5. HandshakeInterceptor가 ticket을 **조회 후 즉시 삭제**하고 세션에 userUuid 바인딩.
6. STOMP CONNECT는 handshake 세션 userUuid만 사용 (클라이언트 STOMP 헤더 무시).

JWT를 URL에 넣지 않으므로 access token 로그 노출 위험이 없습니다. ticket은 1회 소비·짧은 TTL이라 로그에 남아도 재사용이 어렵습니다.

## STOMP

- 엔드포인트: `/ws/chat`
- 발행: `/app/agits/{agitUuid}/send`
- 구독: `/sub/agits/{agitUuid}`

## Gateway 후속 (plip-gateway 별도 이슈)

- `/api/chat/ws/**` — JWT 없이 permit (ticket은 chat-service에서 검증)
- `/api/chat/api/v1/ws/ticket` — 기존과 동일 JWT 필수
