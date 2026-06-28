# ZenOptimize

> **FPS Boost & Lag Spike Reducer for Minecraft Java on Android**
> Built for Fabric 1.21.1 — designed for mobile launchers like Zalith Launcher 2, PojavLauncher, MojoLauncher, and more.

---

## What It Does

ZenOptimize is a lightweight client-side Fabric mod that squeezes every bit of performance out of Minecraft Java Edition on low-end Android devices. It dynamically responds to your current FPS instead of forcing fixed settings, so it stays smooth without looking worse than it needs to.

### Features

| Feature | Description |
|---|---|
| **Dynamic Render Distance** | Automatically lowers render distance when FPS drops below your target, restores it when recovered |
| **Particle Limiter** | Caps active particles to prevent particle storms from tanking FPS |
| **Mobile Frame Cap** | Caps FPS to prevent thermal throttling on Android devices |
| **Periodic GC Hinting** | Nudges the JVM garbage collector when free memory is critically low to reduce GC lag spikes |
| **Fog Optimization** | Optionally pushes fog start further out to reduce overdraw cost on low-end GPUs |

---

## Compatibility

Works with any Android launcher that supports Minecraft Java Edition via Fabric:

- ✅ Zalith Launcher 2
- ✅ Zalith Launcher
- ✅ PojavLauncher
- ✅ MojoLauncher
- ✅ Any other Fabric-compatible Java launcher

---

## Installation

1. Download the latest `.jar` from [Releases](https://github.com/dvaustria741-a11y/ZenOptimize/releases)
2. Drop it into your launcher's `mods/` folder
3. Make sure you have [Fabric Loader](https://fabricmc.net/use/installer/) and [Fabric API](https://modrinth.com/mod/fabric-api) installed
4. Launch the game — ZenOptimize activates automatically

---

## Configuration

After first launch, a config file is created at:
```
.minecraft/config/zenoptimize.json
```

| Option | Default | Description |
|---|---|---|
| `dynamicRenderDistance` | `true` | Enable auto render distance scaling |
| `targetFps` | `30` | FPS threshold that triggers adjustments |
| `minRenderDistance` | `2` | Minimum chunks to allow (never goes below this) |
| `limitParticles` | `true` | Enable particle cap |
| `maxParticles` | `200` | Maximum simultaneous particles |
| `periodicGc` | `true` | Enable GC hinting on low memory |
| `gcThreshold` | `0.15` | Free memory ratio that triggers GC hint |
| `reduceFogDensity` | `false` | Extend fog start to reduce overdraw |
| `mobileFrameCap` | `60` | Max FPS cap (0 = disabled) |

---

## Building from Source

Requires JDK 21.

```bash
git clone https://github.com/dvaustria741-a11y/ZenOptimize.git
cd ZenOptimize
./gradlew build
```

Output jar will be at `build/libs/zenoptimize-*.jar`.

---

## Requirements

- Minecraft **1.21.1**
- Fabric Loader **≥ 0.15.0**
- Fabric API
- Java **21**

---

## License

MIT — do whatever you want with it.