# SimpleVoiceChatScroll

SimpleVoiceChatScroll is a client-side Minecraft mod for Fabric, Forge, and NeoForge that
lets you adjust the volume of individual
[Simple Voice Chat](https://modrinth.com/plugin/simple-voice-chat) players
without opening a menu.

This branch builds the Minecraft 1.21.9 universal JAR for Fabric, Forge, NeoForge.

## Features

- Adjust a player's voice volume by looking at them, holding the right mouse
  button, and scrolling the mouse wheel.
- Change the volume from 0% to 400%.
- See the new volume above the targeted player for three seconds.
- Keep individual player volumes saved through Simple Voice Chat.
- Install the mod only on the client; servers do not need SimpleVoiceChatScroll.
- Choose Fabric, Forge, or NeoForge on supported versions.

> **The volume indicator still works when player name tags are disabled or
> hidden.**

## Usage

1. Point the crosshair at a player connected to Simple Voice Chat.
2. Hold the right mouse button.
3. Scroll up to increase their volume or down to decrease it.

Below the default 100% volume, each scroll step changes the volume by 5
percentage points. At or above 100%, each step changes it by 10 percentage
points.

The indicator displays the difference from the default volume:

- `0%` means the default 100% volume.
- `+100%` means 200% volume.
- `-100%` means the player is muted.

## Compatibility

| Minecraft | Fabric | Forge | NeoForge | Java |
|---|---|---|---|---|
| 26.2 | ✅ | ✅ | ✅ | 25+ |
| 26.1, 26.1.1, 26.1.2 | ✅ | ✅ | ✅ | 25+ |
| 1.21.11 | ✅ | ✅ | ✅ | 21+ |
| 1.21.10 | ✅ | — | — | 21+ |
| 1.21.1, 1.21.3–1.21.9 | ✅ | ✅ | ✅ | 21+ |
| 1.21.2 | ✅ | — | ✅ | 21+ |

Every build requires Simple Voice Chat and the matching mod loader. Fabric
builds also require Fabric API.

## Installation

1. Install Fabric Loader with Fabric API, Forge, or NeoForge.
2. Install Simple Voice Chat for the same loader.
3. Download the universal SimpleVoiceChatScroll JAR for your Minecraft version from
   [GitHub Releases](https://github.com/Don4ara/SimpleVoiceChatScroll/releases) or
   [Modrinth](https://modrinth.com/mod/voicewheel).
4. Place the downloaded JAR in the Minecraft `mods` directory.

## Building

Clone the repository and run:

```shell
./gradlew build
```

On Windows:

```powershell
.\gradlew.bat build
```

The compiled mod JAR is written to `build/libs`.

Changes limited to CI configuration, documentation, or Markdown files do not
trigger a build. To skip a build for another commit, include `[ci skip]` in its
commit message.

## Releases

Every push to `main`, `fabric/<version>`, `forge/<version>`, or
`neoforge/<version>` publishes the compiled JAR to a version-specific GitHub release.
The current multiloader build packages the Fabric, Forge, and NeoForge entrypoints
and metadata into one universal JAR.

Release tags combine the mod and Minecraft versions, for example
`v0.0.2-mc1.21.9`. The release asset is:

- `SimpleVoiceChatScroll-0.0.2+mc1.21.9-universal.jar`

Update `mod_version` before publishing a new version. Pushing an explicit tag
starting with `v` is also supported.

## License

All rights reserved.
