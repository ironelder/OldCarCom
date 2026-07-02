// 오픈개러지 일괄 임포터
// 동의받아 확보한 정비/복원 글(posts.json) + 사진(images/)을 Firestore `records` 에 등록.
//
// 사용:
//   1) Firebase 콘솔 → 프로젝트 설정 → 서비스 계정 → 새 비공개 키 생성 → serviceAccountKey.json 저장(이 폴더)
//   2) 본인(또는 전용) 계정 uid 를 ARCHIVE_UID 로 설정 (앱에서 한 번 로그인 후 users 컬렉션에서 확인)
//   3) posts.json 작성 + images/ 에 사진 배치 (posts.sample.json 참고)
//   4) npm install && npm run import   (미리보기: npm run import -- --dry)
//
// 주의: 저작권 동의를 받은 콘텐츠만 넣으세요. 네이버 자동 스크래핑 결과를 넣는 용도가 아닙니다.

import { readFileSync, existsSync } from "node:fs";
import { basename, join, dirname } from "node:path";
import { fileURLToPath } from "node:url";
import { initializeApp, cert } from "firebase-admin/app";
import { getFirestore, FieldValue } from "firebase-admin/firestore";
import { getStorage } from "firebase-admin/storage";

const HERE = dirname(fileURLToPath(import.meta.url));
const DRY = process.argv.includes("--dry");

// ── 설정 ─────────────────────────────────────────────
const ARCHIVE_UID = "REPLACE_WITH_ARCHIVE_ACCOUNT_UID"; // 아카이브 계정 uid
const STORAGE_BUCKET = "opengarage-5154b.firebasestorage.app";
// ────────────────────────────────────────────────────

// 앱의 Car.makeModelKey 와 동일 정규화 (대소문자/공백/하이픈 무시)
function makeModelKey(make, model) {
  const m = String(model).trim().toLowerCase().replace(/[\s\-_]+/g, "");
  return `${String(make).trim()}_${m}`;
}

function parseDateMs(s) {
  if (!s) return Date.now();
  const t = Date.parse(s);
  return Number.isNaN(t) ? Date.now() : t;
}

async function main() {
  const keyPath = join(HERE, "serviceAccountKey.json");
  if (!existsSync(keyPath)) {
    console.error("✗ serviceAccountKey.json 없음. Firebase 콘솔에서 서비스 계정 키를 받아 이 폴더에 두세요.");
    process.exit(1);
  }
  if (ARCHIVE_UID.startsWith("REPLACE_")) {
    console.error("✗ import.mjs 의 ARCHIVE_UID 를 실제 계정 uid 로 바꾸세요.");
    process.exit(1);
  }

  const serviceAccount = JSON.parse(readFileSync(keyPath, "utf8"));
  initializeApp({ credential: cert(serviceAccount), storageBucket: STORAGE_BUCKET });
  const db = getFirestore();
  const bucket = getStorage().bucket();

  const posts = JSON.parse(readFileSync(join(HERE, "posts.json"), "utf8"));
  console.log(`${posts.length}개 글 임포트${DRY ? " (dry-run)" : ""}\n`);

  let ok = 0;
  for (const [i, p] of posts.entries()) {
    const label = `[${i + 1}/${posts.length}] ${p.make} ${p.model} · ${p.title}`;
    if (!p.make || !p.model || !p.title) {
      console.warn(`  건너뜀(필수 누락): ${label}`);
      continue;
    }

    const ref = db.collection("records").doc();
    const recordId = ref.id;

    // 사진 업로드
    const photoUrls = [];
    for (const [j, rel] of (p.photos ?? []).entries()) {
      const local = join(HERE, rel);
      if (!existsSync(local)) {
        console.warn(`  사진 없음: ${rel}`);
        continue;
      }
      const dest = `users/${ARCHIVE_UID}/records/${recordId}/photo_${j}${extOf(rel)}`;
      if (!DRY) {
        await bucket.upload(local, { destination: dest, metadata: { contentType: contentType(rel) } });
        const [url] = await bucket.file(dest).getSignedUrl({ action: "read", expires: "2099-12-31" });
        photoUrls.push(url);
      } else {
        photoUrls.push(`(dry)/${dest}`);
      }
    }

    const record = {
      recordId,
      carId: "",
      ownerUid: ARCHIVE_UID,
      ownerNickname: p.author ?? "비회원",
      make: String(p.make).trim(),
      modelKey: makeModelKey(p.make, p.model),
      type: "MAINTENANCE",
      date: parseDateMs(p.date),
      mileageKm: Number(p.mileageKm) || 0,
      title: String(p.title).trim(),
      description: String(p.description ?? "").trim(),
      photoUrls,
      cost: Number(p.cost) || 0,
      liters: null,
      fuelType: null,
      shared: true,
      createdAt: parseDateMs(p.date),
    };

    if (!DRY) await ref.set(record);
    ok++;
    console.log(`  ✓ ${label} (사진 ${photoUrls.length})`);
  }

  console.log(`\n완료: ${ok}/${posts.length}${DRY ? " (dry-run, 미반영)" : ""}`);
}

function extOf(p) {
  const b = basename(p);
  const i = b.lastIndexOf(".");
  return i >= 0 ? b.slice(i) : ".jpg";
}
function contentType(p) {
  const e = extOf(p).toLowerCase();
  if (e === ".png") return "image/png";
  if (e === ".webp") return "image/webp";
  if (e === ".svg") return "image/svg+xml";
  return "image/jpeg";
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
