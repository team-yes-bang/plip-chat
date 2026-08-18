# 서비스 템플릿 설정 및 초기화 가이드 (Quick Start Guide)

`plip-template`을 복사한 뒤 **서비스 repo 초기 설정**을 다룹니다.  
Develop PC 자동 배포(CI/CD) **흐름·인프라 등록**은 [plip-develop-env/DEVELOP_PC_DEPLOY.md](../plip-develop-env/DEVELOP_PC_DEPLOY.md)를 참고하세요.

| 문서 | 역할 |
| --- | --- |
| **START.md** (본 문서) | 템플릿 적용 · YML/env · **서비스 repo** CI/CD 준비 |
| [DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md) | 팀 개발 규칙 (헥사고날, DB, Springdoc) |
| [AI_CODING_GUIDELINES.md](AI_CODING_GUIDELINES.md) | AI 에이전트용 코드 생성 제약 |
| [GIT_CONVENTION.md](GIT_CONVENTION.md) | Issue / 브랜치 / 커밋 / PR / CI |

### AI 가이드 문서 사용

[AI_CODING_GUIDELINES.md](AI_CODING_GUIDELINES.md)는 Cursor 등 AI 에이전트가 따를 규칙입니다.  
에이전트·팀 정책에 맞게 **파일명을 바꾸거나** (예: `.cursor/rules/…`, `AGENTS.md`, `copilot-instructions.md`) **필요한 섹션만 복사**해 사용해도 됩니다.  
단, 헥사고날 레이어·명명(`adapter`, `dto`) 등 **코드 규칙의 내용**은 [DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md)와 맞추세요.

초기 설정 후 일상 개발은 DEVELOPMENT_GUIDE를 따릅니다. `src/test/java/com/sample`은 빌드 제외 참고용입니다.

## Check List

- [ ] 1. **프로젝트 기본 정보 변경**: `settings.gradle` · 패키지 · `{Service}Application` · `{Service}ApplicationTests`(클래스명 포함) · `OpenApiGeneratorTest`
- [ ] 2. **Application YML · Env**: `application.yaml` · `application-local.yml` · `application-docker.yml` · `application-test.yml` · `.env.example`
- [ ] 3. **Docker Compose placeholder 치환**: `docker-compose.app.yml` · `docker-compose.yml` (`{service}` / `plip_{service}` · 필요 시 로컬 App override)
- [ ] 4. **로컬 기동 확인**: MySQL compose + IDE bootRun (또는 선택: Docker App)
- [ ] 5. **CI/CD 준비**: 서비스 repo workflow `"service"` 값 변경 (env/Gateway 연동은 **인프라 담당**)

---

## 샘플 코드 (`com.sample`) — 참고용 (빌드 제외)

`src/test/java/com/sample/**`는 `./gradlew build` / 테스트 대상에서 **제외**됩니다 (`build.gradle` `sourceSets`).  
서비스 코드·테스트는 `com.plip.{service}`에 작성하세요. 신규 코드 명명은 DEVELOPMENT_GUIDE 표준(`adapter`, `dto`)을 따릅니다.

---

## 1단계: 프로젝트 기본 정보

`plip-template`을 복사해 `plip-{service}`를 만들 때 아래를 **서비스 id**에 맞게 바꿉니다. (예: `user`, `payment`)
> `-service` 접미사는 쓰지 않습니다. Gateway `lb://`, manifest `id`와 동일하게 유지합니다.

### 1) `settings.gradle`
```groovy
// plip-template -> {service} 로 변경
rootProject.name = '{service}'
```

### 2) 패키지 · 메인 클래스 · 테스트 (IDE Refactor)

1. `src/main/java/com/plip/template` → Rename (Shift+F6) → `com.plip.{service}`
2. `src/test/java/com/plip/template` 동일 (`OpenApiGeneratorTest`, `*ApplicationTests` 포함)
3. 메인 클래스: `TemplateApplication.java` → `{Service}Application.java`
4. ApplicationTests: `TemplateApplicationTests.java` → `{Service}ApplicationTests.java` — **파일명과 클래스 선언** 모두 Rename

| 항목 | plip-template | 복사 후 (예: `{service}`) |
| --- | --- | --- |
| 패키지 | `com.plip.template` | `com.plip.{service}` |
| 메인 클래스 | `TemplateApplication.java` | `{Service}Application.java` |
| ApplicationTests | `TemplateApplicationTests` (클래스명) | `{Service}ApplicationTests` |
| OpenAPI 테스트 | `OpenApiGeneratorTest.java` | 동일 클래스명, 패키지 `{service}` |

---

## 2단계: Application YML · Env

| 파일 | 역할 |
| --- | --- |
| `application.yaml` | 공통 — `spring.application.name`, datasource env 키, Eureka 기본 |
| `application-local.yml` | 개발 PC IDE/`bootRun` — 포트, `localhost:3308` DB |
| `application-docker.yml` | Develop PC compose — `mysql:3306`, Eureka hostname = **서비스 id** |
| `application-test.yml` | `./gradlew test` — H2 in-memory DB명 |
| `.env.example` | 환경변수 참고용 (**.env` Git 금지**) |

### 서비스 id 통일 (아래 표의 id·스키마를 모든 위치에서 동일하게)

| 항목 | plip-template | 복사 후 (`{service}`) | 적용 위치 |
| --- | --- | --- | --- |
| 서비스 id | `template` | `{service}` | `settings.gradle`, `spring.application.name`, workflow `"service"`, Eureka docker hostname |
| MySQL 스키마 | `plip_template` | `plip_{service}` | `application-local.yml`, `application-docker.yml`, `.env.example`, compose env |
| H2 테스트 DB | `plip_template_test` | `plip_{service}_test` | `application-test.yml` |
| Gateway path | `/template/**` | `/{service}/**` | 인프라 등록 (서비스 repo 직접 수정 아님) |
| 로컬 포트 | `8081` | 팀 할당 포트 | `application-local.yml` `server.port` (template 기본값 `8081`) |

### 파일별 변경 항목

1. **`application.yaml`** — `spring.application.name`: `template` → `{service}`
2. **`application-local.yml`** — `server.port` (팀 할당), datasource URL `plip_template` → `plip_{service}`
3. **`application-docker.yml`** — datasource `${MYSQL_DATABASE:plip_template}` → `plip_{service}`, `eureka.instance.hostname`: `template` → `{service}` (Develop PC `docker-compose.services.yml`과 일치)
4. **`application-test.yml`** — H2 URL `plip_template_test` → `plip_{service}_test`
5. **`.env.example`** — `MYSQL_DATABASE=plip_template` → `plip_{service}`

---

## 3단계: Docker Compose placeholder 치환 (★)

통합 Develop PC 환경과의 설정 중복을 막기 위해, App·로컬 설정을 두 파일로 분리해 둡니다.

| 파일 | 역할 | Develop PC |
| --- | --- | --- |
| `docker-compose.app.yml` | App 컨테이너 정의 SSOT (Eureka on, `depends_on: eureka-server`) | `plip-develop-env`가 `include` |
| `docker-compose.yml` | 로컬 MySQL + (선택) App **로컬 override** | 사용하지 않음 |
| `application-docker.yml` | `SPRING_PROFILES_ACTIVE=docker` 시 Spring 설정 | JAR에 포함, compose env로 일부 override |

repo 루트에 `{service}` placeholder 파일이 이미 있으므로 **새로 만들지 않고** 치환하세요.

### 1) `docker-compose.app.yml` (App 전용 — develop-env SSOT)

Develop 통합 환경에서 `include`로 불러 씁니다. **로컬 Eureka 미연동은 이 파일을 수정하지 않고** `docker-compose.yml`에서 override 합니다.

| placeholder | 예 (`agit`) |
| --- | --- |
| 서비스 키 `{service}:` | `agit:` |
| `plip-service-{service}` | `plip-service-agit` |
| `plip_{service}` | `plip_agit` |
| `EUREKA_INSTANCE_HOSTNAME: {service}` | `EUREKA_INSTANCE_HOSTNAME: agit` |

### 2) `docker-compose.yml` (로컬 개발용)

`docker-compose.app.yml`을 `include`하고 로컬 MySQL을 선언합니다.

**placeholder 치환**

| placeholder | 예 (`agit`) |
| --- | --- |
| `plip-{service}-mysql` | `plip-agit-mysql` |
| `plip_{service}` (DB·volume명) | `plip_agit`, `plip_agit_mysql_data` |

**로컬 MySQL ↔ App 통신** — `mysql`을 `plip-net`에 연결합니다 (`app.yml`과 동일 network). 파일에 `networks.plip-net` 정의가 포함되어 있습니다.

**(선택) 로컬 Docker로 App까지 실행** — `docker-compose.yml` 하단 주석 블록을 해제하고 `{service}`를 치환하세요.

| override | 목적 |
| --- | --- |
| `depends_on: !reset` + `mysql` only | `app.yml`의 `eureka-server` 의존 제거 |
| `EUREKA_CLIENT_ENABLED: "false"` | `application-docker.yml`의 Eureka on을 로컬에서만 off |
| `ports` | 호스트 접근 (`app.yml`에는 ports 없음 — Develop PC는 Gateway 경유) |

---

## 4단계: 로컬 실행 확인 (개발용)

### A. 기본 — MySQL compose + IDE bootRun (권장)

1. MySQL만 기동: `docker compose up -d mysql`
2. 환경변수: Spring Boot는 **`.env`를 자동 로드하지 않습니다.** IDE Run Configuration 또는 터미널 `export`로 `DB_USERNAME`, `DB_PASSWORD` 주입
3. IDE에서 `{Service}Application` 실행 (profile: `local`)
4. 호출 테스트: `http://localhost:{포트}/api/test` (template 기본 포트 `8081`, `application-local.yml` 참고)

> `docker compose up -d`(서비스 전체)는 3단계 **로컬 App override 주석을 해제하기 전**에는 `eureka-server` 의존으로 실패할 수 있습니다.

### B. (선택) 로컬 Docker로 App까지 실행

1. 3단계에서 `docker-compose.yml`의 `{service}` override 주석 해제 · 치환
2. `docker compose up -d --build`
3. 호출 테스트: `http://localhost:${SERVER_PORT:-8080}/api/test`

### C. 마무리 검증

```bash
rg -i "template|plip_template" --glob '!build/**'
./gradlew test
```

`com.sample`의 `ReactiveMongoTemplate`, `pull_request_template.md` 등은 false positive입니다.

---

## 5단계: Develop PC CI/CD — 서비스 repo에서 할 일

`develop` push 시 자동으로 Develop PC에 배포(Deploy)가 되도록 워크플로우를 수정합니다.
> **참고:** 통합 레포(`plip-develop-env`)에 새 서비스를 등록(`docker-compose.services.yml`에 include 추가)하고 Gateway 라우팅을 추가하는 작업은 **인프라 담당**이 수행합니다.

### 서비스 담당자 체크리스트

| 파일 | 수정해야 할 내용 |
| --- | --- |
| `.github/workflows/deploy-develop-pc.yml` | `service: "template"` 부분을 `service: "{service}"`로 변경 |
| `.github/workflows/test.yml` | 수정 불필요 (PR/push 시 `./gradlew test`) |

수정 후 `develop` 브랜치에 코드를 푸시하면 CI가 동작하며, 인프라 등록이 완료된 시점부터 통합 API 문서([Gateway Swagger UI](http://192.168.10.144:8000/swagger-ui/index.html)) 및 Develop 서버에서 서비스 확인이 가능해집니다.

---

## FAQ

- **Q. 통합 Develop 환경(`plip-develop-env`) 연동은 어떻게 하나요?**  
  서비스 개발자는 `docker-compose.app.yml`의 placeholder를 치환해 두면 됩니다. 통합 레포 관리자가 해당 파일을 `include`하여 연동하므로 추가적인 복사-붙여넣기가 필요 없습니다.
- **Q. 로컬 DB 포트가 3306이 아닌 3308인 이유**  
  통합 DB나 다른 서비스와의 포트 충돌을 막기 위해 로컬 단독 MySQL은 호스트 `3308` 포트에 노출하도록 규정되어 있습니다.
- **Q. Could not resolve placeholder 'DB_PASSWORD' 오류 발생**  
  Spring 실행 환경(IDE Run Config 등)에 `DB_PASSWORD` 환경 변수가 누락되었습니다. `.env` 파일만 만들고 IDE env를 설정하지 않은 경우에도 동일합니다. 주입 후 다시 실행하세요.
- **Q. `docker compose up -d` 시 eureka-server not found**  
  로컬 기본 흐름은 `docker compose up -d mysql` + IDE(`local` profile)입니다. Docker로 App까지 띄우려면 3단계의 로컬 App override 주석을 해제하세요.