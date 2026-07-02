// 수입 인기 클래식(E30/W123/Mustang)별 세부 정비기록 100개씩 추가(append).
// 범용 세분항목 + 모델 고유이슈 조합, 항목별 도해 자동 배정(공유 URL).
// 사용: node add_import_deep.mjs [--dry]

import { readFileSync, existsSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";
import { initializeApp, cert } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";
import { getStorage } from "firebase-admin/storage";

const HERE = dirname(fileURLToPath(import.meta.url));
const DRY = process.argv.includes("--dry");
const ARCHIVE_UID = "qJmvCaSr8PTZH9i4HJ0uACcEGjI3";
const STORAGE_BUCKET = "opengarage-5154b.firebasestorage.app";
const AUTHOR = "오픈개러지 가이드";
const PER_MODEL = 100;

const norm = (m) => m.trim().toLowerCase().replace(/[\s\-_]+/g, "");
const modelKeyOf = (make, model) => `${make.trim()}_${norm(model)}`;
const T = (t, d, v) => ({ t, d, v });

// 범용 세분항목 74
const UNIV = [
  // 냉각 6
  T("워터펌프 교체", "cooling_flow", "임펠러·베어링·누수 점검 후 교체."),
  T("라디에이터 리코어/교체", "cooling_flow", "코어 막힘 시 리코어 또는 교체."),
  T("서모스탯 교체", "cooling_flow", "고착 시 과열/난방불량, 규격 개도품 사용."),
  T("히터코어 교체", "cooling_flow", "막힘/누수 시 플러싱 또는 교체."),
  T("냉각팬/팬클러치 점검", "cooling_flow", "전동팬 모터·릴레이 또는 클러치 유격 점검."),
  T("냉각 호스 일체 교체", "cooling_flow", "상하부·히터·바이패스 호스 경화 교체."),
  // 타이밍 5
  T("타이밍벨트/체인 교체", "timing_marks", "주기 교체, 마크 정렬·장력 규격 준수."),
  T("타이밍 텐셔너 교체", "timing_marks", "베어링 소음·장력 저하 시 교체."),
  T("타이밍 가이드레일 점검", "timing_marks", "체인식 가이드 플라스틱 노후 점검."),
  T("캠샤프트 오일실 교체", "timing_marks", "타이밍커버 누유 시 캠실 교체."),
  T("크랭크 프론트 오일실 교체", "timing_marks", "풀리 뒤 누유 시 프론트 실 교체."),
  // 밸브 3
  T("밸브 간극 조정", "valve_clearance", "냉간/온간 규격으로 심/나사 조정."),
  T("밸브 스템실 교체", "valve_clearance", "시동초 백연 시 스템실 교체."),
  T("밸브커버 개스킷 교체", "valve_clearance", "상부 누유 시 개스킷·그로밋 교체."),
  // 헤드/개스킷 5
  T("헤드개스킷 교체", "head_torque_sequence", "혼입·백연·과열 시 교체, 평면도 점검."),
  T("실린더 헤드 평면 연마", "head_torque_sequence", "변형 시 규격 내 연마."),
  T("흡기 매니폴드 개스킷 교체", "head_torque_sequence", "진공 누기 시 공회전 불안정."),
  T("배기 매니폴드 개스킷 교체", "head_torque_sequence", "누기 시 배기음·성능저하."),
  T("오일팬 개스킷 교체", "head_torque_sequence", "하부 누유 시 팬 탈거·교체."),
  // 브레이크 7
  T("브레이크 패드 교체", "brake_bleed_order", "마모 한계·디스크 두께 점검."),
  T("브레이크 디스크 교체", "brake_bleed_order", "편마모·크랙·최소두께 이하 교체."),
  T("캘리퍼 오버홀", "brake_bleed_order", "슬라이드핀·피스톤 실 키트 교체."),
  T("마스터실린더 교체", "brake_bleed_order", "내부 누유로 페달 꺼짐 시 교체."),
  T("브레이크 부스터 점검", "brake_bleed_order", "진공 누기·배력 저하 점검."),
  T("브레이크 호스/라인 교체", "brake_bleed_order", "부풀음·부식 교체 후 블리딩."),
  T("주차브레이크 조정", "brake_bleed_order", "케이블 유격 조정, 고착 교체."),
  // 현가 10
  T("프론트 쇽업소버 교체", "suspension_bushings", "누유·리바운드 저하 시 교체."),
  T("리어 쇽업소버 교체", "suspension_bushings", "좌우 세트 교체 권장."),
  T("프론트 스프링 교체", "suspension_bushings", "새그·파손 시 교체."),
  T("리어 스프링 교체", "suspension_bushings", "새그·부싱 점검."),
  T("로어암 부싱 교체", "suspension_bushings", "유격 시 소음·정렬 틀어짐."),
  T("볼조인트 교체", "suspension_bushings", "조향 유격·소음 원인, 교체 후 얼라인."),
  T("스태빌라이저 링크 교체", "suspension_bushings", "요철 통통 소음 원인."),
  T("스태빌라이저 부싱 교체", "suspension_bushings", "롤 증가·소음 시 교체."),
  T("프론트 휠베어링 교체", "suspension_bushings", "웅~ 소음·유격 시 교체."),
  T("리어 휠베어링 교체", "suspension_bushings", "허브 프리로드 규격 준수."),
  // 조향 4
  T("타이로드엔드 교체", "suspension_bushings", "유격 시 조향 정확도 저하."),
  T("스티어링 링키지 점검", "suspension_bushings", "센터링크·아이들러암 유격 점검."),
  T("파워스티어링 펌프/오일", "suspension_bushings", "소음·무거움·누유 점검."),
  T("스티어링 커플러/부싱", "suspension_bushings", "유격 시 조향 흔들림."),
  // 전장 9
  T("알터네이터 교체", "charging_circuit", "충전 불량·다이오드 고장 시 교체."),
  T("스타터모터 오버홀", "charging_circuit", "크랭킹 약함 시 브러시·솔레노이드 점검."),
  T("배터리·접지 점검", "charging_circuit", "단자 부식·접지 스트랩 노후 점검."),
  T("등화 릴레이/배선 점검", "charging_circuit", "헤드라이트 릴레이·플래셔 점검."),
  T("퓨즈박스 접점 정비", "charging_circuit", "접점 산화 세척·보강."),
  T("계기판/센서 점검", "charging_circuit", "수온·유압·연료 게이지 배선 점검."),
  T("파워윈도우 레귤레이터", "charging_circuit", "모터·레귤레이터 유격, 스위치 접점."),
  T("중앙잠금장치 점검", "charging_circuit", "액추에이터·배선 점검."),
  T("와이퍼 모터/링키지", "charging_circuit", "모터·링키지 유격·간헐작동 점검."),
  // HVAC 3
  T("에어컨 컴프레서 점검", "charging_circuit", "클러치·소음·냉방 성능 점검."),
  T("에어컨 가스 충전", "charging_circuit", "누기 지점 확인 후 충전."),
  T("블로워 모터/레지스터", "charging_circuit", "풍량 단수 불량 시 점검."),
  // 배기 3
  T("머플러 교체", "rust_points", "부식 천공·소음 시 교체."),
  T("중간 파이프 교체", "rust_points", "부식 구간 교체, 개스킷 병행."),
  T("배기 행거/마운트 점검", "rust_points", "처짐·진동·간섭 점검."),
  // 연료 5
  T("연료펌프 교체", "fuel_injection", "압력 부족·소음 시 교체."),
  T("연료필터 교체", "fuel_injection", "막힘 시 교체, 주기 관리."),
  T("연료탱크 세척/방청", "fuel_injection", "장기보관 변질·녹 세척."),
  T("연료라인 교체", "fuel_injection", "고무라인 경화·누유 교체."),
  T("연료압 레귤레이터 점검", "fuel_injection", "압력 규격·리턴라인 점검."),
  // 점화 4
  T("점화플러그 교체", "ignition_timing", "갭·열가 규격, 실화 개선."),
  T("배전기 오버홀", "ignition_timing", "캡·로터·어드밴스 점검, 타이밍 조정."),
  T("점화코일 교체", "ignition_timing", "누전·실화 시 코일 점검."),
  T("하이텐션 케이블 교체", "ignition_timing", "저항·크랙 점검."),
  // 구동 4
  T("클러치 세트 교체", "auto_trans", "디스크·커버·베어링 세트 교체."),
  T("클러치 유압 실린더", "auto_trans", "마스터/릴리즈 누유 점검."),
  T("변속기 오일 교환", "auto_trans", "규격·주기 교환, 자동은 필터 병행."),
  T("디퍼렌셜 오일 교환", "auto_trans", "규격 교환, 출력축 시일 점검."),
  // 바디 6
  T("하부 방청/언더코팅", "rust_points", "표면녹 제거·방청 도포."),
  T("판금 복원", "rust_points", "관통 부식 판금 보강."),
  T("부분 도색", "rust_points", "색상 매칭·클리어 마감."),
  T("글라스 실링/웨더스트립", "rust_points", "노후 실링 교체로 누수 방지."),
  T("도어 체크/힌지 점검", "rust_points", "처짐·유격 점검."),
  T("실내 트림 복원", "rust_points", "크랙·탈색 트림 복원."),
];

// 모델 고유이슈 26
const ISSUES = {
  E30: [
    T("프론트 쇽타워 부식/보강", "rust_points", "구조 안전 직결, 보강판 시공."),
    T("리어 스프링퍼치 부식", "rust_points", "리어 서브프레임 주변 부식 점검."),
    T("배터리 트레이 부식(트렁크)", "rust_points", "누수·부식 점검·보수."),
    T("리어 서브프레임 부싱 교체", "suspension_bushings", "노후 시 클렁크 소음."),
    T("RTAB(트레일링암 부싱) 교체", "suspension_bushings", "후륜 안정감 회복."),
    T("디퍼렌셜 부싱 교체", "auto_trans", "가속/감속 클렁크 저감."),
    T("M20 타이밍벨트 교체", "timing_marks", "간섭형, 6만km 주기·펌프 세트."),
    T("M10 밸브간극 조정", "valve_clearance", "기계식 태핏 주기 조정."),
    T("금속 임펠러 워터펌프 교체", "cooling_flow", "플라스틱 임펠러 대체."),
    T("서모스탯 하우징 교체", "cooling_flow", "플라스틱 경화 누수 대응."),
    T("익스팬션탱크 교체", "cooling_flow", "균열 누수 예방 교체."),
    T("라디에이터 넥 균열 점검", "cooling_flow", "플라스틱 탱크 균열."),
    T("ICV 공회전 밸브 세척", "fuel_injection", "카본 세척으로 공회전 안정."),
    T("AFM(에어플로우미터) 점검", "fuel_injection", "포텐셔미터 마모 점검."),
    T("연료펌프(인탱크) 교체", "fuel_injection", "압력·소음 점검."),
    T("컨트롤암 부싱(리프레시)", "suspension_bushings", "프론트 유격 개선."),
    T("가이드로드 부싱 교체", "suspension_bushings", "브레이크시 흔들림 저감."),
    T("스티어링 박스 유격 조정", "suspension_bushings", "센터 유격 조정."),
    T("셀렉터/시프터 부싱 교체", "auto_trans", "변속 헐거움 개선."),
    T("클러치 릴리즈 베어링", "auto_trans", "소음·유격 점검."),
    T("헤드개스킷(M20) 점검", "head_torque_sequence", "혼입·오버히트 이력 점검."),
    T("스로틀바디 부싱 점검", "fuel_injection", "축 마모로 흡기 누기."),
    T("선루프 배수구 청소", "rust_points", "막힘 시 실내 누수."),
    T("도어 핸들 메커니즘 정비", "charging_circuit", "노후 링키지 정비."),
    T("헤드라이너 처짐 복원", "rust_points", "접착 노후 복원."),
    T("테일라이트 실링 점검", "rust_points", "누수로 트렁크 부식."),
  ],
  W123: [
    T("진공 중앙잠금 점검", "charging_circuit", "진공 액추에이터 누기 점검."),
    T("진공 시동정지(디젤) 점검", "glow_plug", "정지 솔레노이드/진공 점검."),
    T("진공펌프 점검", "glow_plug", "유지압 부족 시 펌프 점검."),
    T("HVAC 진공 플랩 점검", "charging_circuit", "플랩 미작동은 진공 누기."),
    T("모노밸브(히터) 교체", "cooling_flow", "누수·난방불량 시 교체."),
    T("OM617 밸브간극 조정", "valve_clearance", "기계식 주기 조정."),
    T("타이밍체인/텐셔너(가솔린)", "timing_marks", "체인 스트레치 점검."),
    T("서브프레임 마운트 부식", "rust_points", "안전 직결, 보강."),
    T("앞펜더 하단 부식", "rust_points", "헤드라이트 주변 점검."),
    T("젝킹포인트 부식", "rust_points", "리프트 지점 보강."),
    T("글로우 플러그 교체", "glow_plug", "개별 저항 점검."),
    T("리프트펌프/연료계통", "glow_plug", "연료 공급·수분 점검."),
    T("인젝터 노즐 정비", "glow_plug", "분사압·분무 점검."),
    T("인젝션펌프 타이밍/누유", "glow_plug", "전문 조정 필요."),
    T("스티어링 박스/댐퍼", "suspension_bushings", "유격·스티어링 댐퍼 점검."),
    T("아이들러암 교체", "suspension_bushings", "조향 유격 원인."),
    T("컨트롤암/볼조인트", "suspension_bushings", "유격 점검·교체."),
    T("셀프레벨(왜건) 유압", "suspension_bushings", "펌프·구체 점검."),
    T("클라이밋(온도조절) 점검", "charging_circuit", "서보/센서 점검."),
    T("블로워 레지스터 점검", "charging_circuit", "풍량 단수 불량."),
    T("도어 체크스트랩", "rust_points", "처짐·유격 정비."),
    T("스피도미터 케이블", "charging_circuit", "노이즈·미작동 점검."),
    T("엔진마운트 교체", "auto_trans", "진동·처짐 시 교체."),
    T("자동변속기 오일/모듈레이터", "auto_trans", "진공 모듈레이터 점검."),
    T("배큠락(도어) 조정", "charging_circuit", "잠금 속도·누기 점검."),
    T("크롬/트림 복원", "rust_points", "부식·탈색 복원."),
  ],
  Mustang: [
    T("토크박스 부식 복원", "rust_points", "구조 강성 직결, 패널 교체."),
    T("프론트 프레임레일 점검", "rust_points", "정렬·부식 점검."),
    T("카울 부식 복원", "rust_points", "누수·냄새 원인, 판금."),
    T("플로어팬 교체", "rust_points", "관통 부식 리프로덕션 교체."),
    T("트렁크 플로어 교체", "rust_points", "부식 시 패널 용접."),
    T("쿼터패널 하단 복원", "rust_points", "부식 판금·도색."),
    T("프론트 쇽타워 점검", "rust_points", "크랙·부식 보강."),
    T("V8 점화순서 배선", "firing_order_v8", "1-5-4-2-6-3-7-8 배열 확인."),
    T("카뷰레터(홀리/오토라이트) 정비", "carburetor_adjust", "동조·초크·유면 점검."),
    T("기계식 연료펌프 교체", "fuel_injection", "압력·베이퍼락 점검."),
    T("3열 라디에이터 업그레이드", "cooling_flow", "오버히트 대책 용량 확대."),
    T("팬클러치/전동팬", "cooling_flow", "냉각효율 점검."),
    T("제너레이터→알터네이터 전환", "charging_circuit", "충전 안정화 개조."),
    T("접지 스트랩 보강", "charging_circuit", "다발성 전기증상 개선."),
    T("C4 밴드 조정", "auto_trans", "변속 품질 위해 밴드 조정."),
    T("진공 모듈레이터 교체", "auto_trans", "다이어프램 파손 시 변속 이상."),
    T("토플로더 클러치 정비", "auto_trans", "유격·싱크로 점검."),
    T("9인치 리어액슬 점검", "auto_trans", "픽업 프리로드·시일 점검."),
    T("드럼→디스크 전환(프론트)", "brake_bleed_order", "제동력 개선 키트."),
    T("싱글→듀얼 마스터 개조", "brake_bleed_order", "안전 회로 분리."),
    T("어퍼 컨트롤암 부싱", "suspension_bushings", "부싱·스트럿로드 교체."),
    T("리프스프링 부싱/U볼트", "suspension_bushings", "후륜 유격 점검."),
    T("아이들러암/피트먼암", "suspension_bushings", "조향 유격 교체."),
    T("파워스티어링 전환/정비", "suspension_bushings", "컨트롤밸브·램 점검."),
    T("듀얼 배기 정비", "rust_points", "부식·행거 점검."),
    T("프론트엔드 리빌드", "suspension_bushings", "볼조인트·부싱·타이로드 일괄."),
  ],
};

const MODELS = [
  { make: "BMW", model: "E30", issues: "E30" },
  { make: "Mercedes-Benz", model: "W123", issues: "W123" },
  { make: "Ford", model: "Mustang", issues: "Mustang" },
];

function build() {
  const base = Date.parse("2017-01-01T00:00:00Z");
  const out = [];
  let idx = 0;
  for (const m of MODELS) {
    const topics = [...ISSUES[m.issues], ...UNIV].slice(0, PER_MODEL);
    for (const t of topics) {
      out.push({
        make: m.make, model: m.model, title: `${m.model} ${t.t}`, description: t.v,
        dateMs: base + idx * 24 * 3600 * 1000, mileageKm: 80000 + ((idx * 617) % 200000),
        cost: 150000, diagram: t.d,
      });
      idx++;
    }
  }
  return out;
}

async function main() {
  const keyPath = join(HERE, "serviceAccountKey.json");
  if (!existsSync(keyPath)) { console.error("✗ serviceAccountKey.json 없음"); process.exit(1); }
  initializeApp({ credential: cert(JSON.parse(readFileSync(keyPath, "utf8"))), storageBucket: STORAGE_BUCKET });
  const db = getFirestore();
  const bucket = getStorage().bucket();

  const records = build();
  console.log(`생성: ${records.length}건 (모델 ${MODELS.length} × ${PER_MODEL})${DRY ? " (dry-run)" : ""}`);

  const diaNames = [...new Set(records.map((r) => r.diagram))];
  const diaUrl = {};
  for (const name of diaNames) {
    const local = join(HERE, "diagrams", `${name}.svg`);
    if (!existsSync(local)) { console.warn(`도해 없음: ${name}`); continue; }
    const dest = `shared/diagrams/${name}.svg`;
    if (!DRY) {
      await bucket.upload(local, { destination: dest, metadata: { contentType: "image/svg+xml" } });
      const [url] = await bucket.file(dest).getSignedUrl({ action: "read", expires: "2099-12-31" });
      diaUrl[name] = url;
    } else diaUrl[name] = `(dry)/${dest}`;
  }
  console.log(`도해: ${Object.keys(diaUrl).length}종`);
  if (DRY) { console.log("샘플:", records.slice(0, 4).map((r) => r.title).join(" | ")); return; }

  let written = 0;
  for (let i = 0; i < records.length; i += 400) {
    const b = db.batch();
    for (const r of records.slice(i, i + 400)) {
      const ref = db.collection("records").doc();
      b.set(ref, {
        recordId: ref.id, carId: "", ownerUid: ARCHIVE_UID, ownerNickname: AUTHOR,
        make: r.make, modelKey: modelKeyOf(r.make, r.model), type: "MAINTENANCE",
        date: r.dateMs, mileageKm: r.mileageKm, title: r.title, description: r.description,
        photoUrls: [diaUrl[r.diagram]], cost: r.cost, liters: null, fuelType: null,
        shared: true, createdAt: r.dateMs,
      });
      written++;
    }
    await b.commit();
    console.log(`  ...${Math.min(i + 400, records.length)}/${records.length}`);
  }
  console.log(`완료: ${written}건 추가`);
}

main().catch((e) => { console.error(e); process.exit(1); });
