// 대량 시드 생성기: 모델 × 정비항목 매트릭스로 500+ 정비 레퍼런스 생성.
// 항목별 도해 자동 배정(도해는 1회만 업로드해 공유 URL 사용) + 기존 시드('오픈개러지 가이드') 교체.
// 사용: node bulk_seed.mjs [--dry]
//
// ⚠️ 기존 '오픈개러지 가이드' 작성 시드 문서를 삭제 후 다시 씁니다(사용자 개인 기록은 닉네임이 달라 보존).

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
const TARGET_MAX = 560;

const norm = (m) => m.trim().toLowerCase().replace(/[\s\-_]+/g, "");
const modelKeyOf = (make, model) => `${make.trim()}_${norm(model)}`;

// ── 정비항목(토픽): 제목·도해·적용조건·설명 변형 3종 ──────────────
const TOPICS = [
  { key: "timing", title: "타이밍벨트/체인 점검·교체", dia: "timing_marks", when: () => true, cost: 200000,
    v: [
      "타이밍 구동 방식(벨트/체인)에 맞춰 주기 점검. 벨트는 6만~10만km 주기 교체하며 워터펌프·텐셔너·아이들러를 세트로 교환.",
      "간섭형 엔진은 벨트 끊김이 밸브 손상으로 직결되므로 주기 초과 금지. 캠·크랭크 타이밍 마크 정렬과 장력 규격 준수.",
      "체인 방식은 늘어짐·텐셔너·가이드레일 노후 점검. 냉간 래틀음은 텐셔너/체인 스트레치 신호.",
    ] },
  { key: "cooling", title: "냉각계통 점검(워터펌프·서모스탯)", dia: "cooling_flow", when: () => true, cost: 120000,
    v: [
      "냉각수 2년/4만km 주기 교환, 부동액 농도 확인. 호스 경화·크랙, 라디에이터 코어 막힘 점검.",
      "서모스탯 고착 시 과열/난방불량. 워터펌프 베어링 소음·누수 확인, 전동팬/팬클러치 작동 점검.",
      "연식 노후 시 라디에이터·호스·서모스탯 예방적 세트 교환으로 과열 리스크 감소.",
    ] },
  { key: "brake", title: "브레이크 정비(패드·라이닝·유압)", dia: "brake_bleed_order", when: () => true, cost: 100000,
    v: [
      "패드/라이닝 마모 한계 확인, 디스크 두께·드럼 진원도 점검. 브레이크액 2년 주기 교환·블리딩.",
      "캘리퍼 슬라이드핀 고착 시 편마모·끌림. 휠실린더·마스터실린더 누유, 페달 유격 확인.",
      "노후 하드라인·소프트호스 크랙 점검·교체로 제동 안전 확보. 블리딩은 먼 바퀴부터.",
    ] },
  { key: "susp", title: "현가·부싱 리프레시", dia: "suspension_bushings", when: () => true, cost: 200000,
    v: [
      "컨트롤암 부싱·볼조인트·타이로드엔드 유격이 소음·조향 유격의 주원인. 교체 후 얼라인먼트 필수.",
      "쇽업소버 누유·성능저하, 스태빌라이저 링크·부싱 점검. 요철 통통 소음은 링크/부싱 의심.",
      "노후 고무 부싱 경화로 승차감·정렬 틀어짐. 좌우 세트 교체 권장.",
    ] },
  { key: "charge", title: "전장·충전계통 점검", dia: "charging_circuit", when: () => true, cost: 90000,
    v: [
      "충전전압(약 13.8~14.5V) 확인, 알터네이터·레귤레이터·벨트 장력 점검.",
      "접지 스트랩(엔진-바디-배터리) 노후 시 다발성 전기 증상 → 접지 재작업으로 개선.",
      "퓨즈박스 접점 산화, 배선 피복 경화·단선 구간 점검. 릴레이 작동 확인.",
    ] },
  { key: "rust", title: "부식·방청 점검(복원)", dia: "rust_points", when: () => true, cost: 300000,
    v: [
      "앞펜더 하단·젝킹포인트·플로어·트렁크·도어 하단 등 대표 부식점 점검. 배수구 막힘 확인.",
      "표면녹은 제거 후 방청·도포, 관통 부식은 판금 보강. 하부 언더코팅 아래 진행성 부식 주의.",
      "실링·웨더스트립 노후로 인한 침수·부식 확인. 프레임형은 프레임·크로스멤버 집중 점검.",
    ] },
  { key: "head", title: "헤드개스킷·토크 점검", dia: "head_torque_sequence", when: () => true, cost: 320000,
    v: [
      "헤드개스킷 손상 시 오일·냉각수 혼입, 백연, 과열. 상태 점검 후 필요 시 교체.",
      "헤드볼트는 중앙→바깥 순서로 규정 토크를 2~3단계 나눠 조임(각도조임 사양 확인).",
      "냉각수 유실·오버히트 이력 차량은 헤드 평면도·크랙 점검 병행.",
    ] },
  { key: "valve", title: "밸브 간극 점검·조정", dia: "valve_clearance", when: () => true, cost: 90000,
    v: [
      "기계식 태핏은 밸브간극 주기 조정(냉간/온간 규격). 심 또는 조정나사로 조정.",
      "간극 과대 시 타핑음·출력저하, 과소 시 밸브 소손 위험. 규격 준수.",
      "유압태핏 사양은 소음 시 오일압·태핏 점검.",
    ] },
  { key: "ign", title: "점화계통·점화시기 조정", dia: "ignition_timing", when: (m) => m.fuel === "gas", cost: 70000,
    v: [
      "점화플러그 갭·열가 확인, 배전기 캡·로터 접점·하이텐션 케이블 점검.",
      "타이밍 라이트로 점화시기 조정 시 시동성·연비·공회전 안정화.",
      "코일팩(DIS) 사양은 실화 시 코일·플러그 점검.",
    ] },
  { key: "fuel", title: "연료계통 점검", dia: "fuel_injection", when: (m) => m.fuel === "gas", cost: 120000,
    v: [
      "연료펌프 압력·소음, 연료필터 막힘, 인젝터 분무·누유 점검.",
      "연료압 레귤레이터·리턴라인 확인. 기계식(K-Jetronic)은 워밍업 레귤레이터 점검.",
      "장기보관 후 연료 변질·통로 막힘 시 세척. 연료라인 노후 교체.",
    ] },
  { key: "carb", title: "카뷰레터 조정(구형 가솔린)", dia: "carburetor_adjust", when: (m) => m.fuel === "gas" && m.carb, cost: 130000,
    v: [
      "초크 작동, 공회전 속도·혼합비 스크류 조정. 워밍업 후 규정 rpm 세팅.",
      "플로트 유면·니들&시트 누유, 액셀펌프 샷 점검. 진공라인 누기는 공회전 불안정 원인.",
      "장기 고착 시 분해 세척. 종류(솔렉스/베버/홀리)별 셋업 상이.",
    ] },
  { key: "atrans", title: "변속기 점검(오일·클러치)", dia: "auto_trans", when: () => true, cost: 180000,
    v: [
      "자동은 오일·필터·팬개스킷 주기 교환, 규정온도 유량 확인. 변속충격/미끄러짐은 오일 열화 신호.",
      "수동은 클러치 유격·미끄러짐, 릴리즈 베어링 소음, 미션오일 점검.",
      "엔진·미션 마운트 노후 시 진동·변속 이질감. 마운트 점검 병행.",
    ] },
  { key: "firing", title: "점화순서·배선 배열 점검", dia: null, when: (m) => m.fuel === "gas" && (m.layout === "v8" || m.layout === "i6"), cost: 60000,
    v: [
      "점화순서에 맞춘 플러그 케이블 배열 확인(오배열 시 부조·역화).",
      "실린더 번호와 배전기 로터 회전방향 기준으로 케이블 정리.",
      "코일팩 사양은 실린더-코일 매핑 확인.",
    ] },
  { key: "glow", title: "디젤 예열·연료계통 점검", dia: "glow_plug", when: (m) => m.fuel === "diesel", cost: 110000,
    v: [
      "예열플러그 개별 저항(단선) 측정, 예열 릴레이 작동 점검 → 겨울 시동성 확보.",
      "연료필터 수분배출·교체, 인젝터 분사압/분무, 인젝션펌프 누유 점검.",
      "터보 사양은 부스트 누기·터보 샤프트 유격·오일리턴 점검.",
    ] },
  { key: "drive", title: "4WD 구동계통 점검", dia: "drivetrain_4wd", when: (m) => m.drive === "4wd", cost: 220000,
    v: [
      "트랜스퍼케이스·전후 디퍼렌셜 오일 주기 교환, 액슬 시일 누유 점검.",
      "프리휠 허브 작동, 프로펠러샤프트 U조인트·센터베어링 유격 점검.",
      "파트타임 4WD 결합·해제 작동 확인, 구동계 소음 점검.",
    ] },
];

// ── 모델 매트릭스 ────────────────────────────────────────────
// fuel: gas|diesel, layout: i4|i6|v8, drive: 2wd|4wd, carb: 구형 카뷰레터 여부
const M = (make, model, opt = {}) => ({ make, model, fuel: "gas", layout: "i4", drive: "2wd", carb: false, note: "", ...opt });
const MODELS = [
  M("현대", "포니", { carb: true, note: "초기 국산차로 배선·충전계통 노후와 부품 수급이 관건." }),
  M("현대", "스텔라", { carb: true, note: "배전기 점화 기반으로 점화시기·공회전 관리가 중요." }),
  M("현대", "엑셀", { carb: true, note: "소형 대중차로 부품 흔함, 냉각·점화 기본 관리 위주." }),
  M("현대", "쏘나타", { note: "Y2/Y3 세대는 자동변속기 오일 관리가 수명 좌우." }),
  M("현대", "그랜저", { layout: "i6", note: "각그랜저는 전동 전장 노후가 잦아 접점·릴레이 점검 필요." }),
  M("현대", "엘란트라", { note: "밸브커버·크랭크 리어실 누유가 흔한 편." }),
  M("현대", "티뷰론", { note: "베타 DOHC는 타이밍벨트·코일팩 점검이 핵심." }),
  M("현대", "마르샤", { layout: "i6", note: "중형 6기통으로 냉각 여유·전장 점검이 중요." }),
  M("현대", "갤로퍼", { fuel: "diesel", drive: "4wd", note: "프레임형 디젤 SUV로 하부 부식·구동계 관리가 수명 좌우." }),
  M("기아", "프라이드", { carb: true, note: "소형 SOHC로 타이밍벨트·클러치가 단골 정비." }),
  M("기아", "세피아", { note: "워터펌프 누수·서모스탯 고착 과열이 잦음." }),
  M("기아", "콩코드", { note: "마쓰다 계열 기반으로 냉각·점화 기본 관리." }),
  M("기아", "포텐샤", { layout: "i6", note: "대형세단으로 전장·냉각 여유 점검 필요." }),
  M("기아", "스포티지", { drive: "4wd", note: "1세대 프레임형으로 하부·구동계 부식 점검." }),
  M("기아", "봉고", { fuel: "diesel", note: "상용 디젤로 클러치·예열·연료계통 관리." }),
  M("대우", "르망", { carb: true, note: "구형 가솔린으로 카뷰레터/연료계통 셋업이 시동성 좌우." }),
  M("대우", "에스페로", { note: "노후 시 로어암 부싱·쇽 성능저하가 승차감 주범." }),
  M("대우", "프린스", { note: "중형으로 냉각·전장 노후 점검." }),
  M("대우", "씨에로", { note: "소형으로 점화·연료 기본 관리 위주." }),
  M("대우", "누비라", { note: "DOHC로 타이밍벨트·코일 점검." }),
  M("대우", "레간자", { note: "정숙성 지향 중형, 마운트·냉각 점검." }),
  M("대우", "티코", { carb: true, note: "경차 3기통 진동 특성상 엔진마운트 점검 중요." }),
  M("대우", "마티즈", { note: "경차로 볼조인트·타이로드 유격이 소음 주원인." }),
  M("대우", "라노스", { note: "소형으로 점화·냉각 기본 관리." }),
  M("쌍용", "무쏘", { fuel: "diesel", drive: "4wd", note: "벤츠계 디젤로 내구성 좋으나 오일·타이밍 관리 필수." }),
  M("쌍용", "코란도", { fuel: "diesel", drive: "4wd", note: "오프로드 이력 시 프레임·구동계 부식 집중 점검." }),
  M("쌍용", "체어맨", { layout: "i6", note: "벤츠계 6기통으로 전장·냉각 점검." }),
  M("BMW", "E30", { layout: "i6", note: "냉각 플라스틱 부품·쇽타워 부식·리어 서브프레임이 고질." }),
  M("BMW", "E36", { layout: "i6", note: "리어 서브프레임 크랙·바노스·냉각 플라스틱 점검." }),
  M("BMW", "E28", { layout: "i6", note: "M30 빅식스는 타이밍체인·가이드레일 점검." }),
  M("BMW", "2002", { layout: "i4", carb: true, note: "M10 기계식으로 밸브간극·점화 조정이 기본." }),
  M("BMW", "E24", { layout: "i6", note: "대형 쿠페로 냉각 여유·전장 노후 점검." }),
  M("BMW", "E34", { layout: "i6", note: "셀프레벨링·냉각·누유 점검이 핵심." }),
  M("Mercedes-Benz", "W123", { fuel: "diesel", note: "OM617 디젤 내구성의 상징, 진공계통·밸브간극 관리." }),
  M("Mercedes-Benz", "W124", { layout: "i6", note: "특정 연식 엔진 하네스 경화로 전기 잔고장." }),
  M("Mercedes-Benz", "W126", { layout: "v8", note: "V8 타이밍체인·유압태핏·대형 전장 점검." }),
  M("Mercedes-Benz", "W201", { note: "190E는 K/KE-Jetronic 연료계통·멀티링크 점검." }),
  M("Mercedes-Benz", "R107", { layout: "v8", note: "SL 로드스터로 소프트톱·유압·V8 점검." }),
  M("Toyota", "코롤라", { note: "AE86 4A-GE는 밸브간극·점화 셋업. 경량 후륜 특성." }),
  M("Toyota", "크라운", { layout: "i6", note: "대형세단으로 냉각·전장·현가 점검." }),
  M("Toyota", "랜드크루저", { fuel: "diesel", drive: "4wd", note: "헤비듀티 프레임으로 하부·액슬·냉각 관리." }),
  M("Honda", "시빅", { note: "VTEC 사양은 밸브간극·VTEC 솔레노이드·오일압 점검." }),
  M("Honda", "어코드", { note: "타이밍벨트·워터펌프 세트 교체 권장." }),
  M("Volkswagen", "Beetle", { carb: true, note: "공랭 플랫4로 밸브간극·점화·팬벨트가 온도관리 핵심." }),
  M("Volkswagen", "Golf", { note: "MK1/2는 K-Jetronic 연료·냉각 팬스위치 점검." }),
  M("Volvo", "240", { note: "레드블록은 타이밍벨트(간섭형)·오버드라이브 점검." }),
  M("Porsche", "911", { layout: "i6", note: "공랭 플랫6는 밸브·점화·드라이섬프 오일 관리가 수명 핵심." }),
  M("Nissan", "Skyline", { layout: "i6", note: "RB 직렬6는 코일팩·냉각·터보 유격 점검." }),
  M("Ford", "Mustang", { layout: "v8", carb: true, note: "클래식 V8은 토크박스 부식·점화순서·카뷰레터가 단골." }),
  M("Mini", "Classic Mini", { carb: true, note: "엔진·미션 오일 공유 구조라 오일 관리가 양쪽 수명 직결." }),
  M("Land Rover", "Defender", { fuel: "diesel", drive: "4wd", note: "알루미늄 바디+스틸 프레임 갈바닉 부식 점검." }),
  M("Jaguar", "XJ", { layout: "i6", note: "냉각 여유 빠듯·전장 노후로 과열/잔고장 점검." }),
  M("Alfa Romeo", "Alfetta", { note: "트윈캠·후방 트랜스액슬(도넛 커플링) 특유 점검." }),
];

function buildRecords() {
  const base = Date.parse("2019-01-01T00:00:00Z");
  const out = [];
  let idx = 0;
  for (const m of MODELS) {
    for (const t of TOPICS) {
      if (!t.when(m)) continue;
      const variant = t.v[idx % t.v.length];
      let dia = t.dia;
      if (t.key === "firing") dia = m.layout === "v8" ? "firing_order_v8" : "firing_order_i6";
      // 가솔린 EFI 로 카뷰레터 없는 모델의 fuel 토픽은 fuel_injection, 카뷰레터 토픽은 carb 모델만(when에서 처리)
      const desc = m.note ? `${variant} 모델 특화: ${m.note}` : variant;
      out.push({
        make: m.make, model: m.model, title: `${m.model} ${t.title}`,
        description: desc,
        dateMs: base + idx * 36 * 3600 * 1000, // 1.5일 간격
        mileageKm: 60000 + ((idx * 811) % 220000),
        cost: t.cost,
        diagram: dia,
      });
      idx++;
      if (out.length >= TARGET_MAX) return out;
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

  const records = buildRecords();
  console.log(`생성: ${records.length}건${DRY ? " (dry-run)" : ""}`);

  // 1) 도해 공유 업로드(1회) → name→url
  const diaNames = [...new Set(records.map((r) => r.diagram).filter(Boolean))];
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
  console.log(`도해 업로드: ${Object.keys(diaUrl).length}종`);

  if (DRY) {
    console.log("샘플:", records.slice(0, 3).map((r) => `${r.make} ${r.title}`).join(" | "));
    console.log("완료(dry-run, 미반영)");
    return;
  }

  // 2) 기존 시드('오픈개러지 가이드') 삭제
  const old = await db.collection("records").where("ownerUid", "==", ARCHIVE_UID).where("ownerNickname", "==", AUTHOR).get();
  console.log(`기존 시드 삭제: ${old.size}건`);
  for (let i = 0; i < old.docs.length; i += 400) {
    const b = db.batch();
    old.docs.slice(i, i + 400).forEach((d) => b.delete(d.ref));
    await b.commit();
  }

  // 3) 신규 기록 배치 등록
  let written = 0;
  for (let i = 0; i < records.length; i += 400) {
    const b = db.batch();
    for (const r of records.slice(i, i + 400)) {
      const ref = db.collection("records").doc();
      b.set(ref, {
        recordId: ref.id, carId: "", ownerUid: ARCHIVE_UID, ownerNickname: AUTHOR,
        make: r.make, modelKey: modelKeyOf(r.make, r.model),
        type: "MAINTENANCE", date: r.dateMs, mileageKm: r.mileageKm,
        title: r.title, description: r.description,
        photoUrls: r.diagram && diaUrl[r.diagram] ? [diaUrl[r.diagram]] : [],
        cost: r.cost, liters: null, fuelType: null, shared: true, createdAt: r.dateMs,
      });
      written++;
    }
    await b.commit();
    console.log(`  ...${Math.min(i + 400, records.length)}/${records.length}`);
  }
  console.log(`완료: ${written}건 등록`);
}

main().catch((e) => { console.error(e); process.exit(1); });
