// 미국 머슬/포니카 세대·연식별 정비기록 생성(append). years 필드 포함.
// 사용: node add_muscle_years.mjs [--dry]

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
const PER_MODEL = 25;

const norm = (m) => m.trim().toLowerCase().replace(/[\s\-_]+/g, "");
const modelKeyOf = (make, model) => `${make.trim()}_${norm(model)}`;
const T = (t, d, v) => ({ t, d, v });

const TOPICS = [
  T("V8 점화순서·배선 정리", "firing_order_v8", "점화순서에 맞춘 케이블 배열로 부조·역화 방지."),
  T("배전기 포인트→전자점화 전환", "ignition_timing", "드웰 유지·신뢰성 향상 위해 전자점화 개조."),
  T("점화시기 조정(진공/원심 진각)", "ignition_timing", "타이밍라이트로 초기+진각 곡선 확인."),
  T("점화플러그·케이블 교체", "ignition_timing", "열가·갭 규격, 케이블 저항 점검."),
  T("4배럴 카뷰레터 튜닝", "carburetor_adjust", "젯·파워밸브·아이들 믹스처 세팅."),
  T("기계식 연료펌프·베이퍼락 대책", "fuel_injection", "압력 점검, 연료라인 열차단·라우팅."),
  T("연료탱크·센딩유닛 정비", "fuel_injection", "녹·게이지 센더 점검, 라인 교체."),
  T("냉각 업그레이드(대용량 라디)", "cooling_flow", "3~4열/알루미늄 라디, 셔라우드 조합."),
  T("워터펌프·서모스탯 교체", "cooling_flow", "고온 환경 대비 적정 개도 서모스탯."),
  T("팬클러치/전동팬 개조", "cooling_flow", "저속 정체 발열 대책."),
  T("밸브 간극/유압태핏 점검", "valve_clearance", "래시 조정 또는 유압태핏 소음 점검."),
  T("밸브커버·인테이크 개스킷", "head_torque_sequence", "누유·진공 누기 점검·교체."),
  T("헤드개스킷·토크 점검", "head_torque_sequence", "혼입·과열 이력 점검, 규정 토크 순서."),
  T("프론트 디스크 전환(드럼→디스크)", "brake_bleed_order", "제동력·페이드 개선 키트."),
  T("싱글→듀얼 마스터실린더 개조", "brake_bleed_order", "안전 회로 분리."),
  T("드럼 브레이크 정비", "brake_bleed_order", "슈·휠실린더·자동조정 점검."),
  T("파워 부스터/유압 점검", "brake_bleed_order", "배력·누유 점검, 블리딩."),
  T("리프스프링·부싱 리프레시", "suspension_bushings", "새그·부싱 교체로 후륜 안정."),
  T("프론트엔드 리빌드", "suspension_bushings", "볼조인트·부싱·타이로드 일괄."),
  T("아이들러암/피트먼암 교체", "suspension_bushings", "조향 유격 해소."),
  T("파워스티어링 컨트롤밸브/램", "suspension_bushings", "누유·유격 점검(정밀형)."),
  T("리어액슬(9인치/8¾) 정비", "auto_trans", "픽업 프리로드·시일·기어비 점검."),
  T("자동변속기(TorqueFlite/C4) 정비", "auto_trans", "밴드 조정·오일/필터·모듈레이터."),
  T("수동 4단/클러치 정비", "auto_trans", "클러치 세트·유격, 미션오일."),
  T("배기 헤더·듀얼 시스템", "rust_points", "헤더·듀얼 파이프·행거 정비."),
  T("유니바디/프레임 부식 복원", "rust_points", "토크박스·프레임레일·플로어 보강."),
  T("트렁크/플로어팬 판금", "rust_points", "관통 부식 패널 교체."),
  T("충전(제너레이터→알터) 개조", "charging_circuit", "충전 안정화, 접지 보강."),
  T("실내 전장·게이지 정비", "charging_circuit", "게이지·스위치·배선 점검."),
  T("도색/외장 복원", "rust_points", "판금 후 색상 매칭 도색."),
];

const D = (make, model, years) => ({ make, model, years });
const MODELS = [
  D("Ford", "머스탱 1세대", "1964½–1973"),
  D("Ford", "머스탱 II", "1974–1978"),
  D("Ford", "머스탱 Fox-body", "1979–1993"),
  D("Dodge", "챌린저 (E-body)", "1970–1974"),
  D("Dodge", "차저 (B-body)", "1966–1974"),
  D("Chevrolet", "카마로 1세대", "1967–1969"),
  D("Chevrolet", "카마로 2세대", "1970–1981"),
  D("Chevrolet", "콜벳 C2", "1963–1967"),
  D("Chevrolet", "콜벳 C3", "1968–1982"),
  D("Pontiac", "GTO", "1964–1974"),
  D("Pontiac", "파이어버드/트랜스암", "1967–1981"),
];

function build() {
  const base = Date.parse("2016-01-01T00:00:00Z");
  const out = [];
  let idx = 0;
  for (const m of MODELS) {
    for (const t of TOPICS.slice(0, PER_MODEL)) {
      out.push({
        make: m.make, model: m.model, years: m.years,
        title: `${m.model} ${t.t}`, description: `${t.v} (${m.years})`,
        dateMs: base + idx * 20 * 3600 * 1000, mileageKm: 60000 + ((idx * 907) % 180000),
        cost: 200000, diagram: t.d,
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
  if (DRY) { console.log("샘플:", records.slice(0, 3).map((r) => `${r.title} [${r.years}]`).join(" | ")); return; }

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
  console.log(`완료: ${written}건 추가`);
}

main().catch((e) => { console.error(e); process.exit(1); });
