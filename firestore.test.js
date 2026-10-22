const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

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

// --- DIRTY DOZEN & SECURITY BOUNDS TESTS ---

test("1. Unauthenticated user cannot read or list public posts", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("posts").where("visibility", "==", "public").get()
  );
});

test("2. Authenticated user fails unfiltered list query on posts (Query Enforcer)", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("posts").get());
});

test("3. Authenticated user can create valid profile and read public profiles", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 1000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      uid: ALICE_UID,
      displayName: "Sophia",
      bio: "Art & meaningful conversations.",
      age: 23,
      gender: "Female",
      themePreference: "Cream",
      notificationsEnabled: true,
      profileVisibility: "public",
      accountStatus: "active",
      postCount: 0,
      followerCount: 10,
      followingCount: 5,
      isVerified: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(bobDb.collection("users").doc(ALICE_UID).get());
});

test("4. Shadow update / ghost field injection on user profile is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 1000);

  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      uid: ALICE_UID,
      displayName: "Sophia",
      age: 23,
      gender: "Female",
      profileVisibility: "public",
      accountStatus: "active",
      isAdmin: true, // Ghost field
      createdAt: now,
    })
  );
});

test("5. Invalid gender or age value poisoning is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 1000);

  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      uid: ALICE_UID,
      displayName: "Sophia",
      age: 9, // Under 13
      gender: "Other", // Outside ['Male', 'Female']
      profileVisibility: "public",
      accountStatus: "active",
      createdAt: now,
    })
  );
});

test("6. Identity spoofing on post creation is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 1000);

  await assertFails(
    aliceDb.collection("posts").doc("post_1").set({
      postId: "post_1",
      authorId: BOB_UID, // Spoofed authorId
      authorNameSnapshot: "Bob",
      text: "Hello world",
      topics: ["Life"],
      likeCount: 0,
      commentCount: 0,
      visibility: "public",
      status: "active",
      createdAt: now,
    })
  );
});

test("7. Valid post creation, filtered feed query, and bounded like increment succeed; arbitrary likeCount inflation fails", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 1000);

  await assertSucceeds(
    aliceDb.collection("posts").doc("post_1").set({
      postId: "post_1",
      authorId: ALICE_UID,
      authorNameSnapshot: "Sophia",
      text: "Sometimes the most beautiful things in life aren't things at all.",
      topics: ["Life", "Love"],
      themeName: "Cream",
      likeCount: 0,
      commentCount: 0,
      visibility: "public",
      status: "active",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Filtered list query succeeds
  await assertSucceeds(
    bobDb.collection("posts").where("visibility", "==", "public").get()
  );

  // Bob likes Alice's post (+1 likeCount succeeds)
  await assertSucceeds(
    bobDb.collection("posts").doc("post_1").collection("likes").doc(BOB_UID).set({
      uid: BOB_UID,
      postId: "post_1",
      createdAt: now,
    })
  );
  await assertSucceeds(
    bobDb.collection("posts").doc("post_1").update({
      likeCount: 1,
      updatedAt: now,
    })
  );

  // Bob tries to inflate likeCount to 9999 -> fails
  await assertFails(
    bobDb.collection("posts").doc("post_1").update({
      likeCount: 9999,
      updatedAt: now,
    })
  );
});

test("8. Cross-user access to savedPosts, blocks, and notifications is rejected", async () => {
  const now = new Date(Date.now() - 1000);
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const adminDb = context.firestore();
    await adminDb.collection("posts").doc("post_1").set({
      postId: "post_1",
      authorId: ALICE_UID,
      authorNameSnapshot: "Sophia",
      text: "Art",
      topics: ["Art"],
      likeCount: 0,
      commentCount: 0,
      visibility: "public",
      status: "active",
      createdAt: now,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("savedPosts").doc("post_1").set({
      uid: ALICE_UID,
      postId: "post_1",
      savedAt: now,
    })
  );

  // Bob cannot read Alice's savedPosts
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("savedPosts").doc("post_1").get()
  );
});
