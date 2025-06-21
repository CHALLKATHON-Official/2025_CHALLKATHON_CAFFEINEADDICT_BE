# GitHub 라벨 정의 및 가이드

## 📋 라벨 체계 개요

이 문서는 CAFFEINE 프로젝트에서 사용하는 GitHub 라벨의 정의와 사용 가이드입니다.

---

## 🏷️ 라벨 카테고리

### 1. 유형 (Type) - 파란색 계열

| 라벨 | 색상 | 설명 | 사용 예시 |
|------|------|------|-----------|
| `bug` | `#d73a49` | 버그 및 오류 | 로그인 시 JWT 토큰 생성 실패 |
| `enhancement` | `#a2eeef` | 새로운 기능 및 개선사항 | 프로필 이미지 업로드 기능 |
| `task` | `#1d76db` | 일반적인 개발 작업 | UserService 리팩터링 |
| `documentation` | `#0075ca` | 문서화 관련 | API 문서 업데이트 |
| `refactor` | `#5319e7` | 코드 리팩터링 | 중복 코드 제거 |
| `test` | `#0e8a16` | 테스트 관련 | 단위 테스트 추가 |
| `security` | `#b60205` | 보안 관련 | 입력 검증 강화 |
| `performance` | `#fbca04` | 성능 최적화 | 데이터베이스 쿼리 최적화 |

### 2. 우선순위 (Priority) - 빨간색 계열

| 라벨 | 색상 | 설명 | 처리 기한 |
|------|------|------|-----------|
| `priority: critical` | `#b60205` | 즉시 처리 필요 (운영 장애) | 24시간 이내 |
| `priority: high` | `#d73a49` | 높은 우선순위 | 1주일 이내 |
| `priority: medium` | `#fbca04` | 보통 우선순위 | 2-3주 이내 |
| `priority: low` | `#0e8a16` | 낮은 우선순위 | 시간 날 때 |

### 3. 상태 (Status) - 회색/보라색 계열

| 라벨 | 색상 | 설명 | 담당자 |
|------|------|------|--------|
| `needs-triage` | `#ededed` | 분류 및 검토 필요 | 팀 리드 |
| `needs-reproduction` | `#fef2c0` | 재현 단계 확인 필요 | 이슈 작성자 |
| `needs-design` | `#c2e0c6` | 설계 검토 필요 | 아키텍트 |
| `ready-for-dev` | `#c5def5` | 개발 준비 완료 | 개발자 |
| `in-progress` | `#f9d0c4` | 개발 진행 중 | 담당 개발자 |
| `needs-review` | `#e1a7f2` | 코드 리뷰 필요 | 리뷰어 |
| `blocked` | `#d73a49` | 차단된 상태 | 관련 담당자 |

### 4. 영향 범위 (Scope) - 초록색 계열

| 라벨 | 색상 | 설명 | 예시 |
|------|------|------|------|
| `scope: auth` | `#0e8a16` | 인증/인가 모듈 | JWT, OAuth2 관련 |
| `scope: domain` | `#5fbf5f` | 도메인 계층 | 비즈니스 로직, 엔티티 |
| `scope: global` | `#7fb069` | 전역 설정 및 공통 | 예외 처리, 설정 |
| `scope: infrastructure` | `#99d492` | 인프라 계층 | 데이터베이스, 외부 API |
| `scope: build` | `#b3e6b3` | 빌드 및 배포 | Gradle, Docker |
| `scope: ci/cd` | `#ccf2cc` | CI/CD 파이프라인 | GitHub Actions |

### 5. 플랫폼 (Platform) - 노란색 계열

| 라벨 | 색상 | 설명 |
|------|------|------|
| `platform: backend` | `#fbca04` | 백엔드 관련 |
| `platform: database` | `#f9d71c` | 데이터베이스 관련 |
| `platform: api` | `#dfd874` | API 관련 |

### 6. 특수 라벨 (Special) - 보라색/핑크 계열

| 라벨 | 색상 | 설명 | 사용 조건 |
|------|------|------|-----------|
| `good first issue` | `#7057ff` | 새로운 기여자에게 적합 | 난이도 낮음, 명확한 요구사항 |
| `help wanted` | `#008672` | 도움이 필요한 이슈 | 추가 인력 또는 전문성 필요 |
| `breaking change` | `#d4c5f9` | 기존 API를 변경하는 작업 | 버전 업그레이드 시 고려 필요 |
| `duplicate` | `#cfd3d7` | 중복된 이슈 | 기존 이슈와 동일 |
| `wontfix` | `#ffffff` | 수정하지 않을 이슈 | 의도된 동작 또는 범위 밖 |

---

## 🔧 라벨 사용 가이드

### 이슈 생성 시 라벨링

1. **자동 라벨**: 이슈 템플릿 사용 시 자동으로 기본 라벨 적용
2. **수동 라벨**: 이슈 내용에 따라 추가 라벨 적용

#### 권장 라벨 조합 예시:

```
버그 리포트:
- bug (유형)
- priority: high (우선순위)
- scope: auth (영향 범위)
- needs-triage (상태)

기능 요청:
- enhancement (유형)
- priority: medium (우선순위)
- scope: domain (영향 범위)
- needs-design (상태)

일반 작업:
- task (유형)
- priority: low (우선순위)
- scope: global (영향 범위)
- ready-for-dev (상태)
```

### Pull Request 라벨링

1. **자동 라벨**: PR 제목의 prefix에 따라 자동 적용
2. **수동 라벨**: 변경 범위와 우선순위에 따라 추가

#### PR 라벨 예시:

```
버그 수정 PR:
- bug
- scope: auth
- priority: high

새 기능 PR:
- enhancement
- scope: domain
- breaking change (필요한 경우)

리팩터링 PR:
- refactor
- scope: global
- priority: low
```

---

## 🔄 라벨 워크플로우

### 이슈 생명주기

```
생성 → needs-triage → (우선순위 설정) → ready-for-dev → in-progress → needs-review → 완료
```

### 담당자별 역할

#### 팀 리드 (Team Lead)
- `needs-triage` 라벨 이슈 검토 및 분류
- 우선순위 라벨 할당
- 영향 범위 라벨 검증

#### 개발자 (Developer)
- `ready-for-dev` 이슈 선택 및 작업 시작
- 작업 시작 시 `in-progress` 라벨 적용
- PR 생성 시 `needs-review` 라벨 적용

#### 리뷰어 (Reviewer)
- `needs-review` PR 우선 검토
- 리뷰 완료 후 적절한 라벨로 변경

---

## 📊 라벨 관리 도구

### GitHub CLI를 이용한 라벨 일괄 생성

```bash
# 라벨 생성 스크립트 예시
gh label create "scope: auth" --color "0e8a16" --description "인증/인가 모듈 관련"
gh label create "priority: critical" --color "b60205" --description "즉시 처리 필요"
```

### 라벨 정리 주기
- **매주 금요일**: 오래된 `needs-triage` 라벨 정리
- **매월 말**: 완료된 이슈의 라벨 정리
- **분기별**: 라벨 사용 현황 검토 및 개선

---

## 🎯 라벨 사용 팁

### DO ✅
- 이슈/PR 생성 시 최소 2-3개의 라벨 적용
- 상태 라벨은 실시간으로 업데이트
- 우선순위가 변경되면 라벨도 함께 수정

### DON'T ❌
- 너무 많은 라벨을 한 번에 적용하지 않기
- 모호하거나 중복되는 라벨 조합 피하기
- 라벨 없이 이슈/PR 생성하지 않기

---

## 📋 라벨 리스트 (복사용)

### 빠른 라벨 적용을 위한 참고

**버그 관련:**
`bug`, `priority: high`, `needs-triage`

**새 기능 관련:**
`enhancement`, `priority: medium`, `needs-design`

**작업 관련:**
`task`, `refactor`, `ready-for-dev`

**긴급 상황:**
`priority: critical`, `bug`, `needs-reproduction`

---

*이 라벨 가이드는 팀의 워크플로우 개선에 따라 지속적으로 업데이트됩니다.*