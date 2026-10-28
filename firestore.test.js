const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_trainer_1";
const BOB_UID = "bob_trainer_2";

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

function validProfile(uid) {
  const now = new Date();
  return {
    userId: uid,
    trainerName: "AetherTrainer",
    level: 12,
    gold: 1500,
    continentId: 1,
    badgesCount: 3,
    capturedCount: 18,
    questsCompleted: 7,
    factionSylvariaRep: 120,
    factionPyroclastRep: 50,
    factionAetherisRep: 80,
    saveStateJson: "{\"monsters\":[]}",
    createdAt: now,
    updatedAt: now,
  };
}

function validMonster(uid) {
  const now = new Date();
  return {
    userId: uid,
    speciesId: 1,
    name: "Sylphling",
    primaryElement: "TERRA",
    currentLevel: 14,
    xp: 240,
    attack: 88,
    defense: 76,
    speed: 84,
    elementalPower: 82,
    maxHp: 175,
    trainedAttack: 24,
    trainedDefense: 12,
    trainedSpeed: 18,
    geneticIvRank: "S",
    geneticTraitId: "trait_solar_vein",
    geneticTraitName: "Solar-Ader",
    generation: 1,
    evolutionStage: 1,
    evolutionProgressPercent: 72,
    targetEvolutionSpeciesId: 2,
    requiredArtifactId: "",
    createdAt: now,
    updatedAt: now,
  };
}

function validTrade(ownerId) {
  const now = new Date();
  return {
    ownerId: ownerId,
    ownerName: "Alice",
    offeredSpeciesId: 1,
    offeredMonsterName: "Sylphox",
    offeredLevel: 25,
    offeredIvRank: "S",
    offeredTrait: "Solar-Puls",
    offeredTrainedStats: "ATK+40 / SPD+35",
    requestedSpeciesId: 82,
    requestedSpeciesName: "Pyrodrake",
    status: "open",
    completedByUid: "",
    createdAt: now,
    updatedAt: now,
  };
}

test("Unauthenticated user cannot read or write profiles, monsters, or trades", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("monsters").doc("mon_1").get());
  await assertFails(unauthDb.collection("trades").where("status", "==", "open").get());
});

test("Authenticated user can create and read their own PlayerCloudProfile, but not another user's", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(validProfile(ALICE_UID)));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
});

test("Monster entity CRUD, stats/evolution update, and cross-user isolation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  const monRef = aliceDb.collection("users").doc(ALICE_UID).collection("monsters").doc("mon_sylph_1");
  await assertSucceeds(monRef.set(validMonster(ALICE_UID)));
  await assertSucceeds(monRef.get());
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("monsters").where("userId", "==", ALICE_UID).get()
  );

  // Update stats, currentLevel, and evolutionProgressPercent
  await assertSucceeds(
    monRef.update({
      currentLevel: 18,
      attack: 112,
      defense: 90,
      speed: 105,
      trainedAttack: 40,
      evolutionProgressPercent: 100,
      updatedAt: new Date(),
    })
  );

  // Cross-user read & delete rejected
  const bobMonRef = bobDb.collection("users").doc(ALICE_UID).collection("monsters").doc("mon_sylph_1");
  await assertFails(bobMonRef.get());
  await assertFails(bobMonRef.delete());

  // Owner delete succeeds
  await assertSucceeds(monRef.delete());
});

test("Shadow update with ghost field on PlayerCloudProfile is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const profile = validProfile(ALICE_UID);
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(profile));

  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      gold: 9999,
      isAdmin: true,
      updatedAt: new Date(),
    })
  );
});

test("TradeOffer creation, query alignment, fulfillment, and terminal state locking", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(aliceDb.collection("trades").doc("trade_1").set(validTrade(ALICE_UID)));
  await assertSucceeds(bobDb.collection("trades").where("status", "==", "open").get());
  await assertFails(bobDb.collection("trades").get());

  await assertSucceeds(
    bobDb.collection("trades").doc("trade_1").update({
      status: "completed",
      completedByUid: BOB_UID,
      updatedAt: new Date(),
    })
  );

  await assertFails(
    aliceDb.collection("trades").doc("trade_1").update({
      requestedSpeciesId: 10,
      requestedSpeciesName: "Aquafin",
      updatedAt: new Date(),
    })
  );
});

test("ArenaRoster creation and active status query alignment", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date();

  await assertSucceeds(
    aliceDb.collection("arena_rosters").doc(ALICE_UID).set({
      userId: ALICE_UID,
      trainerName: "Alice",
      combatPower: 4200,
      badgesCount: 8,
      wins: 12,
      leadSpeciesId: 3,
      teamSummary: "Sylphox Lv.36 | Ignisaur Lv.35 | Aetherwing Lv.37",
      status: "active",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(bobDb.collection("arena_rosters").where("status", "==", "active").get());
  await assertFails(bobDb.collection("arena_rosters").doc(ALICE_UID).delete());
});
