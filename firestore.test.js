const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_ID = "phone_01711223344";
const ADMIN_ID = "phone_01767563095";
const SAMPLE_HASH =
  "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
// SHA-256 of "889747"
const ADMIN_PASS_HASH =
  "8f7d4d88e66e3b732938d940368541407d91c2c3d24fc7b73ab99e4594bcd4ae";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validInspectionPayload(userId, overrides = {}) {
  const now = new Date(Date.now() - 1000);
  return {
    userId,
    reportMonth: "2026-10",
    inspectionDate: "2026-10-19",
    inspectorName: "মোঃ রাশেদুল ইসলাম",
    ownerName: "আব্দুল করিম",
    buildingAddress: "বাড়ি ১২, রোড ৫, ধানমন্ডি, ঢাকা",
    ownerMobile: "01711223344",
    buildingType: "আবাসিক",
    previousInspectionCount: 2,
    authorizedOfficerInspected: true,
    deviationType: "অনুমোদিত নকশা বহির্ভূত",
    actionTaken: "নোটিশ প্রদান",
    remarks: "৩য় তলার কাজ চলমান",
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

function validUserAccountPayload(userId, overrides = {}) {
  const now = new Date(Date.now() - 1000);
  return {
    userId,
    email: "",
    inspectorName: "",
    designation: "ইমারত পরিদর্শক",
    officeName: "",
    phoneNumber: "01711223344",
    passwordHash: SAMPLE_HASH,
    approvalStatus: "pending",
    role: "inspector",
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
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

test("User can register with phone & passwordHash without Google Sign-In as pending, but CANNOT self-register as approved or admin", async () => {
  const db = testEnv.unauthenticatedContext().firestore();
  const accountRef = db.collection("user_accounts").doc(ALICE_ID);

  // Attempting to register as already 'approved' with non-admin phone/password must fail
  await assertFails(
    accountRef.set(
      validUserAccountPayload(ALICE_ID, { approvalStatus: "approved" })
    )
  );

  // Attempting to register with role 'admin' with non-admin phone/password must fail
  await assertFails(
    accountRef.set(validUserAccountPayload(ALICE_ID, { role: "admin" }))
  );

  // Registering as 'pending' + 'inspector' with empty inspectorName succeeds
  await assertSucceeds(accountRef.set(validUserAccountPayload(ALICE_ID)));

  // Attempting to self-approve before Admin (01767563095) has logged in must fail
  await assertFails(
    accountRef.update({
      approvalStatus: "approved",
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Admin login with phone 01767563095 and password 889747 activates admin role, approves user, and user sets name after login", async () => {
  const db = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(
    db
      .collection("user_accounts")
      .doc(ALICE_ID)
      .set(validUserAccountPayload(ALICE_ID))
  );

  // Before admin activates account, listing all user_accounts fails
  await assertFails(db.collection("user_accounts").get());

  // Admin logs in with phone 01767563095 and password 889747
  await assertSucceeds(
    db
      .collection("user_accounts")
      .doc(ADMIN_ID)
      .set(
        validUserAccountPayload(ADMIN_ID, {
          phoneNumber: "01767563095",
          passwordHash: ADMIN_PASS_HASH,
          approvalStatus: "approved",
          role: "admin",
        })
      )
  );

  // Admin can now list all user_accounts (for the Admin Approval Panel)
  await assertSucceeds(db.collection("user_accounts").get());

  // Admin approves Alice's pending registration
  await assertSucceeds(
    db
      .collection("user_accounts")
      .doc(ALICE_ID)
      .update({
        approvalStatus: "approved",
        updatedAt: new Date(Date.now() - 500),
      })
  );

  // After login, Alice sets her name on her approved account
  await assertSucceeds(
    db
      .collection("user_accounts")
      .doc(ALICE_ID)
      .update({
        inspectorName: "প্রকৌশলী আলিস রহমান",
        updatedAt: new Date(Date.now() - 200),
      })
  );
});

test("User can create, get, list, update, and delete their own inspection report with inspectionDate", async () => {
  const db = testEnv.unauthenticatedContext().firestore();
  const docRef = db
    .collection("users")
    .doc(ALICE_ID)
    .collection("inspections")
    .doc("rep_1");

  await assertSucceeds(docRef.set(validInspectionPayload(ALICE_ID)));
  await assertSucceeds(docRef.get());
  await assertSucceeds(
    db
      .collection("users")
      .doc(ALICE_ID)
      .collection("inspections")
      .where("userId", "==", ALICE_ID)
      .get()
  );

  await assertSucceeds(
    docRef.update({
      actionTaken: "কাজ বন্ধের নির্দেশ",
      previousInspectionCount: 3,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Also test Unresolved Report ("অনিষ্পন্ন রিপোর্ট") with 5 fields and 3 duration options
  const unresolvedRef = db
    .collection("users")
    .doc(ALICE_ID)
    .collection("inspections")
    .doc("rep_unresolved_1");
  await assertSucceeds(
    unresolvedRef.set(
      validInspectionPayload(ALICE_ID, {
        reportCategory: "অনিষ্পন্ন রিপোর্ট",
        previousMonthBalance: 12,
        currentMonthReceived: 8,
        totalReceived: 20,
        unresolvedAtMonthEnd: 5,
        unresolvedDurationCategory: "১ সপ্তাহের অধিক অথচ ২ সপ্তাহের অধিক নয়",
        remarks: "যথাসময়ে নিষ্পত্তির কার্যক্রম চলমান",
      })
    )
  );
  await assertSucceeds(
    unresolvedRef.update({
      unresolvedDurationCategory: "২ সপ্তাহের অধিক অথচ ১ মাসের অধিক নয়",
      updatedAt: new Date(Date.now() - 300),
    })
  );

  await assertSucceeds(docRef.delete());
});

test("Mismatched userId or invalid schema is rejected", async () => {
  const db = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    db
      .collection("users")
      .doc(ALICE_ID)
      .collection("inspections")
      .doc("rep_bad")
      .set(validInspectionPayload("wrong_user_id"))
  );
});
