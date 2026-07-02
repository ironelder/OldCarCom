// 국산 클래식을 세대·연식별로 재정리: 기존 국산('오픈개러지 가이드') 시드 삭제 후 세대별 재생성.
// years 필드 포함. 사용: node add_korean_gen.mjs [--dry]

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
const KOREAN_MAKES = ["현대", "기아", "대우", "쌍용"];
const PER_MODEL = 30;

const norm = (m) => m.trim().toLowerCase().replace(/[\s\-_]+/g, "");
const modelKeyOf = (make, model) => `${make.trim()}_${norm(model)}`;
const T = (t, d, v) => ({ t, d, v });

const UNIV = [
  T("워터펌프 교체", "cooling_flow", "임펠러·베어링·누수 점검 후 교체."),
  T("라디에이터 리코어/교체", "cooling_flow", "코어 막힘 시 리코어 또는 교체."),
  T("서모스탯 교체", "cooling_flow", "고착 시 과열/난방불량, 규격 개도품."),
  T("히터코어 점검", "cooling_flow", "막힘/누수 시 플러싱 또는 교체."),
  T("냉각팬/팬클러치 점검", "cooling_flow", "전동팬 모터·릴레이 또는 클러치 유격."),
  T("타이밍벨트 교체", "timing_marks", "간섭형 주기 준수, 펌프·텐셔너 세트."),
  T("캠/크랭크 오일실 교체", "timing_marks", "타이밍커버·풀리 뒤 누유 시 교체."),
  T("밸브커버 개스킷 교체", "valve_clearance", "상부 누유 시 개스킷 교체."),
  T("헤드개스킷 점검", "head_torque_sequence", "혼입·과열 이력 점검, 규정 토크."),
  T("흡기 매니폴드 개스킷", "head_torque_sequence", "진공 누기 시 공회전 불안정."),
  T("브레이크 패드/디스크", "brake_bleed_order", "마모 한계·두께 점검 교체."),
  T("캘리퍼 오버홀", "brake_bleed_order", "슬라이드핀·피스톤 실 키트."),
  T("마스터실린더/호스", "brake_bleed_order", "누유·부풀음 교체 후 블리딩."),
  T("쇽업소버 교체", "suspension_bushings", "누유·리바운드 저하 시 세트 교체."),
  T("로어암/볼조인트 교체", "suspension_bushings", "유격 시 소음, 교체 후 얼라인."),
  T("스태빌라이저 링크/부싱", "suspension_bushings", "요철 통통 소음 원인."),
  T("휠베어링 교체", "suspension_bushings", "웅~ 소음·유격 시 교체."),
  T("타이로드엔드 교체", "suspension_bushings", "조향 유격 시 교체·토우 조정."),
  T("파워스티어링 펌프/오일", "suspension_bushings", "소음·무거움·누유 점검."),
  T("알터네이터 교체", "charging_circuit", "충전 불량·다이오드 고장 교체."),
  T("스타터모터 오버홀", "charging_circuit", "크랭킹 약함 시 브러시·솔레노이드."),
  T("배터리·접지 점검", "charging_circuit", "단자 부식·접지 스트랩 노후."),
  T("등화/릴레이/퓨즈 점검", "charging_circuit", "릴레이·플래셔·접점 점검."),
  T("파워윈도우/중앙잠금", "charging_circuit", "모터·레귤레이터·액추에이터."),
  T("에어컨 컴프레서/가스", "charging_circuit", "클러치·냉방·누기 점검."),
  T("엔진마운트 교체", "auto_trans", "경화·파손 시 진동 증가."),
  T("변속기 오일/클러치", "auto_trans", "규격 교환, 클러치 유격 점검."),
  T("배기 머플러/파이프", "rust_points", "부식 천공·소음 시 구간 교체."),
  T("하부 방청/언더코팅", "rust_points", "표면녹 제거·방청 도포."),
  T("판금/부식 복원", "rust_points", "관통 부식 판금 보강."),
  T("실링/웨더스트립 교체", "rust_points", "노후 실링 누수 방지."),
  T("실내 전장/계기판", "charging_circuit", "게이지·스위치·배선 점검."),
];
const GAS = [
  T("점화플러그/케이블 교체", "ignition_timing", "갭·저항 점검, 실화 개선.", "gas"),
  T("배전기 오버홀·점화시기", "ignition_timing", "캡·로터 점검, 타이밍 조정.", "gas"),
  T("연료펌프/필터 교체", "fuel_injection", "압력·막힘 점검.", "gas"),
  T("인젝터 청소/교체", "fuel_injection", "분무 불량 시 세척/교체.", "gas"),
  T("카뷰레터 오버홀", "carburetor_adjust", "고착 세척, 공회전·혼합비 조정.", "gas"),
];
const DIESEL = [
  T("예열플러그 교체", "glow_plug", "개별 저항 점검, 시동성 확보.", "diesel"),
  T("예열 릴레이 점검", "glow_plug", "작동음·통전 점검.", "diesel"),
  T("인젝터/인젝션펌프", "glow_plug", "분사압·타이밍·누유 점검.", "diesel"),
  T("연료 수분분리기/필터", "glow_plug", "수분 배출·필터 교체.", "diesel"),
  T("터보 부스트/유격", "glow_plug", "누기·샤프트 유격 점검.", "diesel"),
];
const FOURWD = [
  T("트랜스퍼 오일 교환", "drivetrain_4wd", "규격·주기 교환, 결합 작동 확인."),
  T("전/후 디퍼렌셜 오일", "drivetrain_4wd", "기어오일 교환, 시일 점검."),
  T("프리휠 허브 점검", "drivetrain_4wd", "작동·고착·그리스 점검."),
  T("프로펠러샤프트 U조인트", "drivetrain_4wd", "유격·소음, 그리스 주입."),
];

const G = (make, model, years, opt = {}) => ({ make, model, years, fuel: "gas", drive: "2wd", carb: false, ...opt });
const MODELS = [
  G("현대", "포니", "1975–1990", { carb: true }),
  G("현대", "포니2", "1982–1990", { carb: true }),
  G("현대", "스텔라", "1983–1997", { carb: true }),
  G("현대", "쏘나타 Y2", "1988–1993"),
  G("현대", "쏘나타 Y3", "1993–1998"),
  G("현대", "그랜저 1세대(각그랜저)", "1986–1992"),
  G("현대", "엑셀 1세대", "1985–1989", { carb: true }),
  G("현대", "엑셀 2세대", "1989–1994"),
  G("현대", "엘란트라", "1990–1995"),
  G("현대", "아반떼 1세대", "1995–2000"),
  G("현대", "갤로퍼 1세대", "1991–1997", { fuel: "diesel", drive: "4wd" }),
  G("현대", "갤로퍼 2세대", "1998–2003", { fuel: "diesel", drive: "4wd" }),
  G("기아", "프라이드", "1987–2000", { carb: true }),
  G("기아", "세피아", "1992–1997"),
  G("기아", "스포티지 1세대", "1993–2002", { fuel: "diesel", drive: "4wd" }),
  G("기아", "봉고", "1980–1997", { fuel: "diesel" }),
  G("대우", "르망", "1986–1997", { carb: true }),
  G("대우", "에스페로", "1990–1997"),
  G("대우", "프린스", "1991–1999"),
  G("대우", "티코", "1991–2001", { carb: true }),
  G("대우", "마티즈 1세대", "1998–2005"),
  G("쌍용", "코란도 훼미리", "1988–1996", { fuel: "diesel", drive: "4wd" }),
  G("쌍용", "무쏘", "1993–2005", { fuel: "diesel", drive: "4wd" }),
  G("쌍용", "코란도", "1996–2006", { fuel: "diesel", drive: "4wd" }),
];

function pool(m) {
  const fuelExtra = m.fuel === "diesel" ? DIESEL : GAS.filter((g) => g.t !== "카뷰레터 오버홀" || m.carb);
  const drive = m.drive === "4wd" ? FOURWD : [];
  return [...fuelExtra, ...drive, ...UNIV];
}

function build() {
  const base = Date.parse("2015-01-01T00:00:00Z");
  const out = [];
  let idx = 0;
  for (const m of MODELS) {
    for (const t of pool(m).slice(0, PER_MODEL)) {
      out.push({
        make: m.make, model: m.model, years: m.years,
        title: `${m.model} ${t.t}`, description: `${t.v} (${m.years})`,
        dateMs: base + idx * 26 * 3600 * 1000, mileageKm: 70000 + ((idx * 733) % 210000),
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
  console.log(`생성: ${records.length}건 (${MODELS.length}세대 × ${PER_MODEL})${DRY ? " (dry-run)" : ""}`);

  const diaNames = [...new Set(records.map((r) => r.diagram))];
  const diaUrl = {};
  for (const name of diaNames) {
    const local = join(HERE, "diagrams", `${name}.svg`);
    const dest = `shared/diagrams/${name}.svg`;
    if (!DRY) {
      await bucket.upload(local, { destination: dest, metadata: { contentType: "image/svg+xml" } });
      const [url] = await bucket.file(dest).getSignedUrl({ action: "read", expires: "2099-12-31" });
      diaUrl[name] = url;
    } else diaUrl[name] = `(dry)/${dest}`;
  }
  console.log(`도해: ${Object.keys(diaUrl).length}종`);
  if (DRY) { console.log("샘플:", records.slice(0, 3).map((r) => `${r.title} [${r.years}]`).join(" | ")); return; }

  // 기존 국산 시드 삭제
  let deleted = 0;
  for (const mk of KOREAN_MAKES) {
    const snap = await db.collection("records").where("ownerUid", "==", ARCHIVE_UID).where("make", "==", mk).get();
    for (let i = 0; i < snap.docs.length; i += 400) {
      const b = db.batch();
      snap.docs.slice(i, i + 400).forEach((d) => { if (d.data().ownerNickname === AUTHOR) { b.delete(d.ref); deleted++; } });
      await b.commit();
    }
  }
  console.log(`기존 국산 시드 삭제: ${deleted}건`);

  let written = 0;
  for (let i = 0; i < records.length; i += 400) {
    const b = db.batch();
    for (const r of records.slice(i, i + 400)) {
      const ref = db.collection("records").doc();
      b.set(ref, {
        recordId: ref.id, carId: "", ownerUid: ARCHIVE_UID, ownerNickname: AUTHOR,
        make: r.make, modelKey: modelKeyOf(r.make, r.model), years: r.years, type: "MAINTENANCE",
        date: r.dateMs, mileageKm: r.mileageKm, title: r.title, description: r.description,
        photoUrls: [diaUrl[r.diagram]], cost: r.cost, liters: null, fuelType: null,
        shared: true, createdAt: r.dateMs,
      });
      written++;
    }
    await b.commit();
    console.log(`  ...${Math.min(i + 400, records.length)}/${records.length}`);
  }
  console.log(`완료: ${written}건 등록`);
}

main().catch((e) => { console.error(e); process.exit(1); });
