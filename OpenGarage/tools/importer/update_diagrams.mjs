// 이미 등록된 시드 기록 중, 도해가 새로 붙은 항목의 photoUrls 만 갱신(중복 등록 없이).
// 사용: node update_diagrams.mjs   (serviceAccountKey.json + ARCHIVE_UID 필요)

import { readFileSync, existsSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";
import { initializeApp, cert } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";
import { getStorage } from "firebase-admin/storage";

const HERE = dirname(fileURLToPath(import.meta.url));
const ARCHIVE_UID = "qJmvCaSr8PTZH9i4HJ0uACcEGjI3";
const STORAGE_BUCKET = "opengarage-5154b.firebasestorage.app";

// 제목 → 도해 파일
const MAP = [
  { title: "카뷰레터/연료계통 점검 (구형 가솔린)", svg: "diagrams/carburetor_adjust.svg" },
  { title: "현가·부싱 리프레시 (승차감 개선)", svg: "diagrams/suspension_bushings.svg" },
  { title: "클래식 머스탱 카뷰레터(오토라이트/홀리) 세팅", svg: "diagrams/carburetor_adjust.svg" },
  { title: "W124 M104 헤드개스킷·타이밍·누유 점검", svg: "diagrams/head_torque_sequence.svg" },
];

initializeApp({ credential: cert(JSON.parse(readFileSync(join(HERE, "serviceAccountKey.json"), "utf8"))), storageBucket: STORAGE_BUCKET });
const db = getFirestore();
const bucket = getStorage().bucket();

for (const { title, svg } of MAP) {
  const snap = await db.collection("records")
    .where("ownerUid", "==", ARCHIVE_UID)
    .where("title", "==", title)
    .get();
  if (snap.empty) { console.warn(`건너뜀(없음): ${title}`); continue; }
  const doc = snap.docs[0];
  const local = join(HERE, svg);
  if (!existsSync(local)) { console.warn(`도해 없음: ${svg}`); continue; }
  const dest = `users/${ARCHIVE_UID}/records/${doc.id}/diagram.svg`;
  await bucket.upload(local, { destination: dest, metadata: { contentType: "image/svg+xml" } });
  const [url] = await bucket.file(dest).getSignedUrl({ action: "read", expires: "2099-12-31" });
  await doc.ref.update({ photoUrls: [url] });
  console.log(`✓ ${title} ← ${svg}`);
}
console.log("완료");
