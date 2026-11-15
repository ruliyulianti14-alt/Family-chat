const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ADMIN_UID = "admin_ayah_01";
const MEMBER_UID = "member_anak_02";
const FUND_MGR_UID = "member_ibu_03";
const OUTSIDER_UID = "outsider_99";
const FAMILY_ID = "family_utama_01";
const INVITE_CODE = "KELUARGA2026";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function pastDate() {
  return new Date(Date.now() - 5000);
}

async function seedFamilyWithMembers() {
  const ts = pastDate();
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("families").doc(FAMILY_ID).set({
      name: "Keluarga Harmoni",
      slogan: "One Family, One Connection, One Love.",
      adminUid: ADMIN_UID,
      inviteCode: INVITE_CODE,
      memberUids: [ADMIN_UID, MEMBER_UID, FUND_MGR_UID],
      createdAt: ts,
      updatedAt: ts,
    });
    await db.collection("family_invites").doc(INVITE_CODE).set({
      familyId: FAMILY_ID,
      familyName: "Keluarga Harmoni",
      createdBy: ADMIN_UID,
      active: true,
      createdAt: ts,
      updatedAt: ts,
    });
    await db
      .collection("families")
      .doc(FAMILY_ID)
      .collection("members")
      .doc(ADMIN_UID)
      .set({
        userId: ADMIN_UID,
        familyId: FAMILY_ID,
        displayName: "Ayah Budi",
        relationship: "Ayah",
        role: "admin",
        fundManager: true,
        phone: "08123456789",
        showPhone: true,
        avatarColorIndex: 0,
        locationSharingEnabled: false,
        memberUids: [ADMIN_UID, MEMBER_UID, FUND_MGR_UID],
        createdAt: ts,
        updatedAt: ts,
      });
    await db
      .collection("families")
      .doc(FAMILY_ID)
      .collection("members")
      .doc(MEMBER_UID)
      .set({
        userId: MEMBER_UID,
        familyId: FAMILY_ID,
        displayName: "Rani",
        relationship: "Anak",
        role: "member",
        fundManager: false,
        phone: "08198765432",
        showPhone: false,
        avatarColorIndex: 2,
        locationSharingEnabled: false,
        memberUids: [ADMIN_UID, MEMBER_UID, FUND_MGR_UID],
        createdAt: ts,
        updatedAt: ts,
      });
    await db
      .collection("families")
      .doc(FAMILY_ID)
      .collection("members")
      .doc(FUND_MGR_UID)
      .set({
        userId: FUND_MGR_UID,
        familyId: FAMILY_ID,
        displayName: "Ibu Sari",
        relationship: "Ibu",
        role: "member",
        fundManager: true,
        phone: "08122233344",
        showPhone: true,
        avatarColorIndex: 1,
        locationSharingEnabled: false,
        memberUids: [ADMIN_UID, MEMBER_UID, FUND_MGR_UID],
        createdAt: ts,
        updatedAt: ts,
      });
  });
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read users, families, or groups", async () => {
  await seedFamilyWithMembers();
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ADMIN_UID).get());
  await assertFails(unauthDb.collection("families").doc(FAMILY_ID).get());
  await assertFails(
    unauthDb.collection("families").doc(FAMILY_ID).collection("groups").get()
  );
});

test("PII isolation: User cannot read another user's private profile", async () => {
  const ts = pastDate();
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("users").doc(ADMIN_UID).set({
      userId: ADMIN_UID,
      displayName: "Ayah Budi",
      email: "ayah@example.com",
      familyId: FAMILY_ID,
      createdAt: ts,
      updatedAt: ts,
    });
  });
  const memberDb = testEnv.authenticatedContext(MEMBER_UID).firestore();
  await assertFails(memberDb.collection("users").doc(ADMIN_UID).get());
  const adminDb = testEnv.authenticatedContext(ADMIN_UID).firestore();
  await assertSucceeds(adminDb.collection("users").doc(ADMIN_UID).get());
});

test("ADMIN-ONLY GROUP CREATION: Regular member is denied creating, editing, or deleting a group", async () => {
  await seedFamilyWithMembers();
  const ts = pastDate();
  const memberDb = testEnv.authenticatedContext(MEMBER_UID).firestore();
  const adminDb = testEnv.authenticatedContext(ADMIN_UID).firestore();

  // Member tries to create a group -> MUST FAIL
  await assertFails(
    memberDb
      .collection("families")
      .doc(FAMILY_ID)
      .collection("groups")
      .doc("group_ilegal")
      .set({
        familyId: FAMILY_ID,
        name: "Grup Rahasia",
        description: "Dibuat member biasa",
        category: "Santai",
        createdBy: MEMBER_UID,
        memberUids: [MEMBER_UID, ADMIN_UID],
        createdAt: ts,
        updatedAt: ts,
      })
  );

  // Admin creates a group -> MUST SUCCEED
  await assertSucceeds(
    adminDb
      .collection("families")
      .doc(FAMILY_ID)
      .collection("groups")
      .doc("group_besar")
      .set({
        familyId: FAMILY_ID,
        name: "Keluarga Besar",
        description: "Grup utama keluarga",
        category: "Utama",
        createdBy: ADMIN_UID,
        memberUids: [ADMIN_UID, MEMBER_UID, FUND_MGR_UID],
        createdAt: ts,
        updatedAt: ts,
      })
  );

  // Member tries to rename the group -> MUST FAIL
  await assertFails(
    memberDb
      .collection("families")
      .doc(FAMILY_ID)
      .collection("groups")
      .doc("group_besar")
      .update({
        name: "Diubah Member",
        updatedAt: pastDate(),
      })
  );

  // Member tries to delete the group -> MUST FAIL
  await assertFails(
    memberDb
      .collection("families")
      .doc(FAMILY_ID)
      .collection("groups")
      .doc("group_besar")
      .delete()
  );

  // Member CAN update lastMessageText when sending a message
  await assertSucceeds(
    memberDb
      .collection("families")
      .doc(FAMILY_ID)
      .collection("groups")
      .doc("group_besar")
      .update({
        lastMessageText: "Halo semua!",
        lastMessageSender: "Rani",
        updatedAt: pastDate(),
      })
  );
});

test("PRIVILEGE ESCALATION GUARD: Member cannot promote self to admin or fund manager", async () => {
  await seedFamilyWithMembers();
  const memberDb = testEnv.authenticatedContext(MEMBER_UID).firestore();

  await assertFails(
    memberDb
      .collection("families")
      .doc(FAMILY_ID)
      .collection("members")
      .doc(MEMBER_UID)
      .update({
        role: "admin",
        updatedAt: pastDate(),
      })
  );

  await assertFails(
    memberDb
      .collection("families")
      .doc(FAMILY_ID)
      .collection("members")
      .doc(MEMBER_UID)
      .update({
        fundManager: true,
        updatedAt: pastDate(),
      })
  );

  // Self-update of displayName and showPhone MUST SUCCEED
  await assertSucceeds(
    memberDb
      .collection("families")
      .doc(FAMILY_ID)
      .collection("members")
      .doc(MEMBER_UID)
      .update({
        displayName: "Rani Putri",
        showPhone: true,
        updatedAt: pastDate(),
      })
  );
});

test("FAMILY FUND RBAC: Only admin or fund manager can write transactions, member can only list/read", async () => {
  await seedFamilyWithMembers();
  const ts = pastDate();
  const memberDb = testEnv.authenticatedContext(MEMBER_UID).firestore();
  const fundMgrDb = testEnv.authenticatedContext(FUND_MGR_UID).firestore();

  // Regular member tries to add transaction -> MUST FAIL
  await assertFails(
    memberDb
      .collection("families")
      .doc(FAMILY_ID)
      .collection("fund_transactions")
      .doc("tx_illegal")
      .set({
        familyId: FAMILY_ID,
        title: "Pemasukan Palsu",
        txType: "income",
        amountRupiah: 500000,
        category: "Iuran",
        recordedBy: MEMBER_UID,
        recordedByName: "Rani",
        memberUids: [ADMIN_UID, MEMBER_UID, FUND_MGR_UID],
        createdAt: ts,
        updatedAt: ts,
      })
  );

  // Fund manager adds transaction -> MUST SUCCEED
  await assertSucceeds(
    fundMgrDb
      .collection("families")
      .doc(FAMILY_ID)
      .collection("fund_transactions")
      .doc("tx_01")
      .set({
        familyId: FAMILY_ID,
        title: "Iuran Bulanan",
        txType: "income",
        amountRupiah: 250000,
        category: "Iuran",
        recordedBy: FUND_MGR_UID,
        recordedByName: "Ibu Sari",
        memberUids: [ADMIN_UID, MEMBER_UID, FUND_MGR_UID],
        createdAt: ts,
        updatedAt: ts,
      })
  );

  // Member can list with matching memberUids filter
  await assertSucceeds(
    memberDb
      .collection("families")
      .doc(FAMILY_ID)
      .collection("fund_transactions")
      .where("memberUids", "array-contains", MEMBER_UID)
      .get()
  );

  // Unfiltered list fails (rules are not filters)
  await assertFails(
    memberDb
      .collection("families")
      .doc(FAMILY_ID)
      .collection("fund_transactions")
      .get()
  );
});

test("Shadow Update & Email Spoofing & Outsider Isolation tests", async () => {
  await seedFamilyWithMembers();
  const ts = pastDate();

  // Outsider cannot read family
  const outsiderDb = testEnv.authenticatedContext(OUTSIDER_UID).firestore();
  await assertFails(outsiderDb.collection("families").doc(FAMILY_ID).get());

  // Spoofed unverified admin email cannot read family
  const spoofedDb = testEnv
    .authenticatedContext("spoof_uid", {
      email: "ruliyulianti14@gmail.com",
      email_verified: false,
    })
    .firestore();
  await assertFails(spoofedDb.collection("families").doc(FAMILY_ID).get());

  // Shadow update with ghost field fails
  const adminDb = testEnv.authenticatedContext(ADMIN_UID).firestore();
  await assertFails(
    adminDb
      .collection("families")
      .doc(FAMILY_ID)
      .collection("groups")
      .doc("group_ghost")
      .set({
        familyId: FAMILY_ID,
        name: "Grup Valid",
        description: "Desc",
        category: "Umum",
        createdBy: ADMIN_UID,
        memberUids: [ADMIN_UID],
        ghostField: true,
        createdAt: ts,
        updatedAt: ts,
      })
  );
});
