# VoiceWheel

VoiceWheel is a client-side Fabric mod that lets you adjust the volume of
individual [Simple Voice Chat](https://modrinth.com/plugin/simple-voice-chat)
players without opening a menu.

## Features

- Adjust a player's voice volume by looking at them, holding the right mouse
  button, and scrolling the mouse wheel.
- Change the volume from 0% to 400%.
- See the new volume above the targeted player for three seconds.
- Keep individual player volumes saved through Simple Voice Chat.
- Install the mod only on the client; servers do not need VoiceWheel.

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

## Requirements

- Minecraft 26.2
- Fabric Loader 0.19.3 or newer
- Fabric API
- Simple Voice Chat
- Java 25 or newer

## Installation

1. Install Fabric Loader and Fabric API.
2. Install Simple Voice Chat.
3. Download VoiceWheel from
   [GitHub Releases](https://github.com/Don4ara/VoiceWheel/releases) or
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

Every push to `main` publishes the compiled JAR to a GitHub release. The
release tag is generated from `mod_version` in `gradle.properties`, for example
`v0.0.1`.

Update `mod_version` before publishing a new version. Pushing an explicit tag
starting with `v` is also supported.

## License

All rights reserved.
