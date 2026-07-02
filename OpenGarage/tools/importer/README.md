# 오픈개러지 일괄 임포터

동의받아 확보한 정비/복원 글과 사진을 오픈개러지 Firebase(`records`)에 등록하는 도구.

> ⚠️ 저작권 동의를 받은 콘텐츠만 넣으세요. 네이버 카페 등 자동 스크래핑 결과 투입 용도가 아닙니다.
> 각 글의 작성자 동의(옵트인)가 전제입니다.

## 준비

1. **서비스 계정 키**: Firebase 콘솔 → 프로젝트 설정 → 서비스 계정 → "새 비공개 키 생성" → 받은 파일을 이 폴더에 `serviceAccountKey.json` 으로 저장. (gitignore됨)
2. **아카이브 계정 uid**: 앱에서 한 번 로그인한 계정의 uid를 `import.mjs` 의 `ARCHIVE_UID` 에 입력. (Firestore `users` 컬렉션 문서 ID = uid)
3. **데이터**: `posts.sample.json` 을 참고해 `posts.json` 작성. 사진은 `images/` 폴더에 두고 `photos` 에 상대경로.

## 실행

```bash
cd OpenGarage/tools/importer
npm install
npm run import -- --dry   # 미리보기(DB 미반영)
npm run import            # 실제 등록
```

## posts.json 필드

| 필드 | 필수 | 설명 |
|------|------|------|
| make | ✓ | 제조사 (앱 브랜드 목록과 동일 표기 권장: 현대/기아/BMW…) |
| model | ✓ | 모델명 (대소문자/공백 무시되어 매칭됨) |
| title | ✓ | 정비 제목 |
| author | | 원작성자 표기(동의 닉). 비우면 "비회원" |
| description | | 본문 |
| date | | "YYYY-MM-DD" (없으면 현재) |
| mileageKm | | 주행거리(숫자) |
| cost | | 금액(원, 숫자) |
| photos | | `["images/a.jpg", ...]` 상대경로 배열 |

등록된 기록은 `type=MAINTENANCE`, `shared=true` 로 차종 피드에 노출됩니다.
