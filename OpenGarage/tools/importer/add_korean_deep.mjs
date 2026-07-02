// 인기 국산 클래식카별 세부 정비항목 30개씩 추가(기존 유지, append).
// 기존 광범위 항목과 다른 '세분화' 항목으로 구성. 도해 항목별 자동 배정(공유 URL).
// 사용: node add_korean_deep.mjs [--dry]

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
const PER_MODEL = 30;

const norm = (m) => m.trim().toLowerCase().replace(/[\s\-_]+/g, "");
const modelKeyOf = (make, model) => `${make.trim()}_${norm(model)}`;
const T = (t, d, v, f = "any") => ({ t, d, v, f }); // title, diagram, variants[2], fuel적용

// 세분화 정비항목 풀
const UNIV = [
  T("워터펌프 교체", "cooling_flow", ["임펠러 부식·베어링 소음·누수 시 교체. 타이밍벨트 세트 작업 시 함께.", "교체 후 냉각수 보충·에어빼기 필수."]),
  T("라디에이터 리코어/교체", "cooling_flow", ["코어 막힘으로 냉각효율 저하 시 리코어 또는 교체.", "구형은 알루미늄/구리 코어 선택, 팬스위치 작동 확인."]),
  T("서모스탯 교체", "cooling_flow", ["고착 시 과열/난방불량. 적정 개도 규격품 사용.", "교체 후 개폐 온도·수온 정상화 확인."]),
  T("히터코어 점검·교체", "cooling_flow", ["막힘 시 난방불량, 누수 시 실내 김서림·냄새.", "플러싱으로 개선 안 되면 교체."]),
  T("냉각팬/팬클러치 점검", "cooling_flow", ["전동팬 모터·릴레이 또는 팬클러치 유격 점검.", "저속 정체 시 과열은 팬 작동 불량 의심."]),
  T("타이밍벨트 텐셔너 교체", "timing_marks", ["텐셔너 베어링 소음·장력 저하 시 교체.", "벨트 교체와 동시 작업 권장."]),
  T("캠샤프트 오일실 교체", "timing_marks", ["타이밍커버 주변 누유는 캠실 노후 의심.", "실 교체 후 타이밍 재정렬."]),
  T("크랭크 프론트 오일실 교체", "timing_marks", ["크랭크 풀리 뒤 누유 시 프론트 실 교체.", "풀리 탈거·타이밍 정렬 병행."]),
  T("밸브커버 개스킷 교체", "valve_clearance", ["경화로 상부 누유가 흔함. 개스킷·그로밋 교체.", "볼트 규정 토크로 균일 체결."]),
  T("오일팬 개스킷 교체", "head_torque_sequence", ["하부 누유 시 오일팬 탈거·개스킷 교체.", "면 청소 후 규정 토크 순서 체결."]),
  T("브레이크 캘리퍼 오버홀", "brake_bleed_order", ["슬라이드핀 고착·피스톤 부식 시 오버홀.", "실 키트 교체 후 끌림·편마모 해소."]),
  T("마스터실린더 교체", "brake_bleed_order", ["내부 누유로 페달 꺼짐 시 교체.", "교체 후 블리딩 필수."]),
  T("브레이크 호스/라인 교체", "brake_bleed_order", ["소프트호스 부풀음·하드라인 부식 교체.", "안전 직결, 노후 시 예방 교체."]),
  T("주차브레이크 조정", "brake_bleed_order", ["케이블 늘어짐 시 유격 조정.", "고착 케이블은 교체."]),
  T("쇽업소버 교체", "suspension_bushings", ["누유·리바운드 저하 시 좌우 세트 교체.", "교체 후 승차감 회복."]),
  T("로어암/볼조인트 교체", "suspension_bushings", ["볼조인트 유격은 조향 유격·소음 원인.", "교체 후 얼라인먼트 필수."]),
  T("스태빌라이저 링크/부싱", "suspension_bushings", ["요철 통통 소음은 링크·부싱 유격.", "세트 교체 권장."]),
  T("휠베어링 교체", "suspension_bushings", ["주행 웅~ 소음·유격 시 교체.", "허브 프리로드 규격 준수."]),
  T("타이로드엔드/조향 링키지", "suspension_bushings", ["유격 시 조향 정확도 저하·편마모.", "교체 후 토우 조정."]),
  T("파워스티어링 펌프/오일", "suspension_bushings", ["소음·무거움·누유 시 펌프·호스 점검.", "오일 규격·레벨 확인."]),
  T("알터네이터 교체", "charging_circuit", ["충전 불량·소음·다이오드 고장 시 교체.", "벨트 장력·충전전압 확인."]),
  T("스타터모터 오버홀", "charging_circuit", ["크랭킹 약함·간헐 시동불가 시 브러시·솔레노이드 점검.", "접지·배선 병행 확인."]),
  T("배터리·케이블·접지 점검", "charging_circuit", ["단자 부식·접지 스트랩 노후 점검.", "다발성 전기증상은 접지 재작업."]),
  T("등화류/릴레이/퓨즈 점검", "charging_circuit", ["헤드라이트 릴레이·플래셔·퓨즈 접점 점검.", "배선 피복 노후 구간 보강."]),
  T("엔진마운트 교체", "auto_trans", ["경화·파손 시 진동·소음 증가.", "미션마운트 함께 점검."]),
  T("미션오일 교환/변속 점검", "auto_trans", ["규격·주기 교환. 변속 이질감 점검.", "자동은 필터·팬개스킷 병행."]),
  T("배기 머플러/파이프 교체", "rust_points", ["부식 천공·소음 시 구간 교체.", "행거·개스킷 함께 점검."]),
  T("배기 매니폴드 개스킷", "head_torque_sequence", ["누기 시 배기음·성능저하.", "볼트 고착 주의, 규정 토크 체결."]),
  T("하부 방청/언더코팅", "rust_points", ["표면녹 제거 후 방청·도포.", "배수구·실링 점검으로 재발 방지."]),
  T("판금/도색 복원", "rust_points", ["관통 부식 판금 보강 후 도색.", "색상 매칭·클리어 마감."]),
  T("흡기 매니폴드 개스킷", "head_torque_sequence", ["진공 누기 시 공회전 불안정.", "개스킷 교체·토크 체결."]),
  T("에어컨 컴프레서/가스충전", "charging_circuit", ["냉방 약함 시 가스량·컴프레서 클러치 점검.", "누기 지점 확인 후 충전."]),
  T("실내 전장/계기판 점검", "charging_circuit", ["계기·경고등·시가잭 배선 점검.", "접점·커넥터 산화 확인."]),
  T("도어/윈도우 레귤레이터", "charging_circuit", ["파워윈도우 모터·레귤레이터 유격 점검.", "스위치 접점 세척 또는 교체."]),
];
const GAS = [
  T("점화플러그 교체", "ignition_timing", ["갭·열가 규격 확인 후 교체.", "실화·부조 개선."], "gas"),
  T("배전기 오버홀", "ignition_timing", ["캡·로터 접점·어드밴스 점검.", "타이밍 재조정."], "gas"),
  T("점화코일/케이블 교체", "ignition_timing", ["케이블 저항·크랙, 코일 점검.", "누전 시 실화."], "gas"),
  T("연료펌프 교체", "fuel_injection", ["압력 부족·소음 시 교체.", "필터 동시 교체 권장."], "gas"),
  T("연료필터/탱크 세척", "fuel_injection", ["막힘·이물 시 필터 교체·탱크 세척.", "장기보관 변질 주의."], "gas"),
  T("인젝터 청소/교체", "fuel_injection", ["분무 불량·막힘 시 초음파 세척 또는 교체.", "연료압 병행 점검."], "gas"),
  T("카뷰레터 오버홀", "carburetor_adjust", ["바니시 고착 분해 세척, 유면·초크 점검.", "공회전·혼합비 재조정."], "gas"),
];
const DIESEL = [
  T("예열플러그 교체", "glow_plug", ["개별 저항 측정 후 단선품 교체.", "겨울 시동성 확보."], "diesel"),
  T("예열 릴레이/타이머 점검", "glow_plug", ["작동음·통전 점검.", "예열등 미점등 시 의심."], "diesel"),
  T("인젝터 노즐 점검", "glow_plug", ["분사압·분무 상태 점검.", "매연·부조 시 노즐 정비."], "diesel"),
  T("인젝션펌프 점검", "glow_plug", ["누유·타이밍·연료량 점검.", "전문 장비 조정 필요."], "diesel"),
  T("연료 수분분리기/필터", "glow_plug", ["수분 배출·필터 교체.", "수분 경고 시 즉시 배출."], "diesel"),
  T("터보 부스트/유격 점검", "glow_plug", ["부스트 누기·터보 샤프트 유격.", "오일리턴 라인 점검."], "diesel"),
];
const FOURWD = [
  T("트랜스퍼케이스 오일 교환", "drivetrain_4wd", ["규격·주기 교환, 누유 점검.", "결합·해제 작동 확인."], "any"),
  T("전/후 디퍼렌셜 오일 교환", "drivetrain_4wd", ["기어오일 교환, LSD는 전용유.", "출력축 시일 누유 점검."], "any"),
  T("프리휠 허브 점검", "drivetrain_4wd", ["작동·고착·그리스 상태 점검.", "오토허브는 진공/기계식 확인."], "any"),
  T("프로펠러샤프트 U조인트", "drivetrain_4wd", ["유격·소음 점검, 그리스 주입.", "센터베어링 노후 점검."], "any"),
];

const M = (make, model, opt = {}) => ({ make, model, fuel: "gas", drive: "2wd", carb: false, note: "", ...opt });
const MODELS = [
  M("현대", "포니", { carb: true, note: "초기 국산차, 부품 수급·배선 노후가 관건." }),
  M("현대", "스텔라", { carb: true, note: "80년대 중형, 점화·냉각 기본 관리." }),
  M("현대", "그랜저", { note: "각그랜저, 고급 전장 노후 점검 중요." }),
  M("현대", "엑셀", { carb: true, note: "80년대 소형 베스트셀러, 부품 흔함." }),
  M("현대", "엘란트라", { note: "90년대 준중형, 누유·전장 점검." }),
  M("현대", "갤로퍼", { fuel: "diesel", drive: "4wd", note: "프레임 디젤 SUV, 하부·구동계 관리." }),
  M("기아", "프라이드", { carb: true, note: "소형 명차, 타이밍·클러치 단골." }),
  M("대우", "르망", { carb: true, note: "GM계 소형, 카뷰레터/전장 셋업." }),
  M("대우", "티코", { carb: true, note: "경차 3기통, 마운트·냉각 점검." }),
  M("쌍용", "무쏘", { fuel: "diesel", drive: "4wd", note: "벤츠계 디젤, 오일·구동계 관리." }),
];

function pool(m) {
  const fuelExtra = m.fuel === "diesel" ? DIESEL : GAS.filter((g) => g.t !== "카뷰레터 오버홀" || m.carb);
  const drive = m.drive === "4wd" ? FOURWD : [];
  // 모델특화(연료/구동) 먼저 → 세분화 커버리지, 그다음 범용
  return [...fuelExtra, ...drive, ...UNIV];
}

function build() {
  const base = Date.parse("2018-06-01T00:00:00Z");
  const out = [];
  let idx = 0;
  for (const m of MODELS) {
    const topics = pool(m).slice(0, PER_MODEL);
    for (const t of topics) {
      const variant = t.v[idx % t.v.length];
      out.push({
        make: m.make, model: m.model, title: `${m.model} ${t.t}`,
        description: m.note ? `${variant} 모델 특화: ${m.note}` : variant,
        dateMs: base + idx * 30 * 3600 * 1000, mileageKm: 70000 + ((idx * 733) % 210000),
        cost: 100000, diagram: t.d,
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
