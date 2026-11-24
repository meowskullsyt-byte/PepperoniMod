# Pepperoni — Minecraft Java 1.20.1 (Forge)

A starter Forge mod project adding Pepperoni as a friendly, tamable companion mob.

## Target
- Minecraft **1.20.1**
- Minecraft Forge **47.2.20**
- Java **17**
- Mod ID: `pepperoni`

> You selected “Java 1.20”. This project targets the widely used 1.20.1 patch release. If you meant exactly 1.20.0, the Forge version and mappings need to be changed.

## Features in this build
- Tall custom humanoid model with oversized red block-curl hair, bell bow, bunny sweater, and striped socks.
- 20 HP and a 1.92-block hitbox height.
- Neutral/passive wandering behavior.
- Tame with Sweet Berries or Cake (one-in-three chance per treat).
- Tamed Pepperoni follows her owner; right-click to make her sit/stand.
- Nighttime sleep behavior and walk/sit/sleep/cuddle pose animations.
- Owner interaction menu: talk, give gifts, view bond score, follow, mining, fight, and sleep-together request.
- Bond score increases through conversations and gifts; favorite gifts include cake, berries, flowers, cookies, honey, and amethyst.
- Mining companion mode searches nearby exposed vanilla ores and mines them for drops.
- Guard mode targets hostile monsters and retaliates against attackers.
- Right-click with an armor item to equip a visible chest armor overlay and increase her armor stat (iron/gold/diamond/netherite tier items are accepted). Bond, mode, and armor tier are saved with the entity.
- Sleep-together request requires two separate beds placed side-by-side: select the option, then sleep in your own bed. Pepperoni uses the second bed. With sufficient bond, there is a small chance she briefly curls up on the player's bedcovers for a cuddle animation.
- Spawn egg in the Spawn Eggs creative tab.
- Drops red wool, sweet berries, and a rare bell.

## Build the mod
1. Install a Java 17 JDK.
2. Install Gradle 8.1.1 or newer.
3. Open this folder in a terminal.
4. Run `gradle build`.
5. The mod JAR will be created in `build/libs/pepperoni-1.0.0.jar`.

## Install
1. Install Minecraft Forge 1.20.1 (47.2.20) in your launcher.
2. Build the JAR as above.
3. Put the JAR into your `.minecraft/mods` folder.
4. Launch the Forge 1.20.1 profile.
5. Find the spawn egg in the Spawn Eggs tab, or use `/summon pepperoni:pepperoni`.

## Notes
This is the editable source project, not a precompiled JAR. Gradle must download ForgeGradle and Minecraft/Forge dependencies on the first build, so an internet connection is needed for that initial build. The model and texture are a first playable pass. Mining is intentionally limited to nearby exposed vanilla ore blocks (not full cave navigation), and armor is represented by a visible chestplate overlay rather than separate armor slots for every body part. The sleep/cuddle sequence is a lightweight companion animation, not a bed GUI integration. This expanded source has not been compiled or run in Minecraft in this environment; check the build locally and share any Gradle/compiler output for fixes.

## Build without installing Gradle on your Windows PC (GitHub Actions)

This repository includes `.github/workflows/build.yml`. GitHub's hosted runner installs Java 17 and Gradle 8.8 and builds the mod remotely.

1. Create a repository on GitHub (a new empty repository is fine).
2. Upload the contents of this `PepperoniMod` folder to the repository root. Make sure `.github/workflows/build.yml` is included. If uploading through the website, enable viewing hidden files or upload the `.github` folder too.
3. Open the repository's **Actions** tab. If prompted, enable workflows.
4. The workflow starts automatically after a push. You can also select **Build Pepperoni Mod** and choose **Run workflow**.
5. Wait for the run to finish with a green check.
6. Open the completed run and download the `pepperoni-forge-1.20.1` artifact. Extract it; the mod `.jar` is inside.

This avoids installing Gradle locally, but the hosted build still uses Gradle behind the scenes. A successful GitHub run is required before the JAR is considered built; this source has not yet been compile-tested.
