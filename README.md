# OpenGarage Lab (오픈개러지)

> 올드카 오너가 자기 정비·주유 기록을 올리면, **같은 차종 사람들이 그걸 보고 배우는** 차종 중심 정보공유 앱.

연식 오래된 일상차 오너를 위한 "공유 정비노트". 네이버 카페 클래식카코리아의 정보공유를 앱으로.

- **영문명**: OpenGarage Lab
- **한글명**: 오픈개러지

---

## 핵심 컨셉

| 구분 | 성격 | 공유 |
|------|------|------|
| **정비기록** | 공유 정비노트 (핵심 가치) | 공개 → 차종 피드 노출 |
| **주유기록** | 개인 차계부 (연비·기름값) | 비공개 → 나만 |

"남이 내 정비기록을 본다" = 앱의 존재 이유. 주유는 사적 기록.

---

## 기술 스택

- **앱**: Android 네이티브, Kotlin + Jetpack Compose
- **아키텍처**: MVVM (UI → ViewModel → Repository → Firebase SDK), 단일 모듈
- **백엔드**: Firebase
  - Auth (Google 로그인 1종)
  - Firestore (데이터)
  - Storage (사진)
- **OCR**: MVP 제외 (수동 입력) → 2차에서 ML Kit / CLOVA 검토

---

## 데이터 모델 (Firestore)

### `users/{uid}`
```
uid, nickname, photoUrl, createdAt
```

### `cars/{carId}` — 내 차고
```
carId, ownerUid,
make,      // 제조사 "현대"
model,     // 모델 "프라이드"
year,      // 연식 2005
modelKey,  // "현대_프라이드"  ← 차종 피드 쿼리용 정규화 키
nickname,  // 애칭 "은마"
photoUrl, createdAt
```

### `records/{recordId}` — 정비·주유 기록
```
recordId, carId, ownerUid, ownerNickname,  // 작성자 비정규화
modelKey,        // "현대_프라이드"  ← 차종 피드 쿼리 키 (비정규화)
type,            // MAINTENANCE | FUEL
date,            // 정비/주유 일자
mileageKm,       // 주행거리
title,           // "타이밍벨트 교체"
description,     // 내용/노하우
photoUrls[],     // Storage 사진 링크 배열
cost,            // 금액(원)

// type=FUEL 일 때만:
liters,          // 주유량 (수동입력, 추후 OCR)
fuelType,        // 휘발유/경유/LPG

isPublic,        // MAINTENANCE=true(기본) / FUEL=false(강제)
createdAt
```

### Storage
```
users/{uid}/records/{recordId}/{photo}.jpg
```

### 핵심 쿼리
- **내 차고**: `cars where ownerUid == 나`
- **차종 피드**: `records where modelKey == X and isPublic == true orderBy createdAt desc`
- **차 상세(정비+주유 전체)**: `records where carId == 내차 orderBy date desc`

### 설계 포인트
- Firestore는 join 없음 → `modelKey`, `ownerNickname` **비정규화**로 피드 1쿼리 처리.
- 닉네임 변경 시 과거 기록은 옛 닉 유지 (MVP 허용).
- 주유(FUEL)는 `isPublic=false` 강제 → 피드 절대 미노출.

---

## 화면 구성

하단 탭 3개 + 상세/작성 화면 (Compose Navigation).

```
[ 🏠 차종피드 ]  [ 🔧 내 차고 ]  [ 👤 프로필 ]
```

1. **로그인** — Google 버튼, 첫 진입만
2. **차종 피드(홈)** — 차종 검색/선택 → 공개 정비기록 최신순 카드
3. **기록 상세** — 사진 캐러셀, 제목, 작성자, 주행거리, 날짜, 내용, 금액
4. **내 차고** — 내 차 리스트, `+` 차 추가 / 차 상세(정비+주유 타임라인)
5. **차 추가/수정** — 제조사, 모델, 연식, 애칭, 사진
6. **기록 작성**(FAB) — 정비|주유 선택 → 차/날짜/주행거리/사진/금액/메모 (정비=공개토글, 주유=비공개)
7. **프로필** — 내 닉/사진, 내 공개 정비기록, 로그아웃

### 흐름
```
로그인 → 차종피드(홈)
  ├ 피드 → 차종검색 → 기록상세
  ├ 내차고 → 차상세(정비+주유) / 차추가
  ├ FAB → 기록작성(정비|주유)
  └ 프로필 → 내기록 / 로그아웃
```

---

## MVP 범위

### IN
- [ ] Google 로그인
- [ ] 내 차고 (차 추가/수정/삭제)
- [ ] 정비기록 작성·조회 (공개)
- [ ] 주유기록 작성·조회 (비공개)
- [ ] 차종 피드 (modelKey 공개 정비기록)
- [ ] 기록 상세 (사진 캐러셀)
- [ ] 사진 업로드 (Storage)
- [ ] 프로필 + 로그아웃

### OUT (다음 단계 — YAGNI)
- OCR 영수증 자동추출
- 연비 자동계산·통계 그래프
- 댓글 / 좋아요 / 팔로우 / DM
- iOS / 푸시알림 / 신고·차단 / 고급검색

---

## 진행 내역

- [x] 네이밍 확정 (OpenGarage Lab / 오픈개러지) — 영문명 충돌 회피("Lab" 접미)
- [x] 기술 스택 확정 (Android Compose + Firebase)
- [x] 데이터 모델 확정
- [x] 화면 구성 확정
- [x] MVP 범위 확정
- [x] 구현 계획(플랜) 작성 → [docs/superpowers/plans/2026-06-23-opengarage-lab-mvp.md](docs/superpowers/plans/2026-06-23-opengarage-lab-mvp.md) (Task 0~14)
- [x] 프로젝트 스캐폴딩 (Android Studio `OpenGarage/`, 패키지 `com.lab.opengarage`)
- [x] Firebase 연결 (프로젝트 `opengarage-5154b`, google-services.json + Auth/Firestore/Storage 의존성, 빌드 성공)
- [x] Google 로그인 SHA-1 등록 + web_client_id
- [x] 콘솔 서비스 활성화 (Auth Google / Firestore 서울 / Storage US-EAST1) — REST probe 403=활성 확인
- [x] Task 2: 도메인 모델
- [x] Task 3~5: Repository 인터페이스 / Firebase 구현체+Hilt / Fake
- [x] Task 6~13: UI 전체 + 네비게이션 (화면 10개, ViewModel 8 + 테스트 7)
- [x] Task 14: Firestore/Storage 보안 규칙 + 복합 인덱스 (`OpenGarage/firestore.rules`, `storage.rules`, `firestore.indexes.json`)
- [ ] 보안 규칙·인덱스 콘솔/CLI 배포 (수동)
- [ ] 실기기/에뮬 런타임 검증 (로그인→차추가→기록작성→피드)

### 빌드 상태
- `:app:assembleDebug` ✅ / `:app:testDebugUnitTest` ✅ (8 ViewModel 로직 검증)
- 브랜치 `feature/opengarage-mvp`

> **참고**: 실제 패키지는 `com.lab.opengarage` (플랜 문서의 `com.opengaragelab.app` 대신). 빌드 환경: AGP 9.2.1 / Kotlin 2.2.10 / Gradle 9.4.1 / compileSdk 37 / minSdk 28.
