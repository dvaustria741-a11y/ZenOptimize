# ZenOptimize

> **FPS Boost & Lag Spike Reducer for Minecraft Java on Android**
> Built for Fabric 1.21.1 — designed for mobile launchers like Zalith Launcher 2, PojavLauncher, MojoLauncher, and more.

---

## What It Does

ZenOptimize is a lightweight client-side Fabric mod that squeezes every bit of performance out of Minecraft Java Edition on low-end Android devices. It dynamically responds to your current FPS instead of forcing fixed settings, so it stays smooth without looking worse than it needs to.

### Features

| Feature | Description |
|---|---|
| **Particle Limiter** | Caps active particles to prevent particle storms from tanking FPS |
| **Offscreen Entity Culling** | Skips rendering far-away entities once FPS drops below target, clawing back frames during the heaviest moments |
| **Mobile Frame Cap** | Caps FPS to prevent thermal throttling on Android devices, via the game's own frame limiter (no extra busy-waiting on top) |
| **Fog Optimization** | Optionally pushes fog start further out to reduce overdraw cost on low-end GPUs |
| **Smooth Camera** | Eases camera rotation across frames to remove micro-jitter, without adding any delay to your actual aim/input |

> **Removed in 1.2.0:** Dynamic Render Distance and Periodic GC Hinting. Both were causing the exact lag they were meant to prevent — Dynamic Render Distance triggered a full chunk re-render every time it adjusted, and the GC hint forced a blocking `System.gc()` pause. Both fought systems (chunk loading, JVM memory management) that already handle themselves well; removing them was a net smoothness win.

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
| `targetFps` | `30` | FPS threshold below which offscreen entities get culled more aggressively |
| `skipOffscreenEntities` | `true` | Enable extra entity culling when FPS drops below `targetFps` |
| `limitParticles` | `true` | Enable particle cap |
| `maxParticles` | `200` | Maximum simultaneous particles |
| `reduceFogDensity` | `false` | Extend fog start to reduce overdraw |
| `mobileFrameCap` | `60` | Max FPS cap (0 = disabled). Applied through the vanilla max-FPS option so frame pacing stays smooth — does not run its own separate limiter |
| `smoothCamera` | `true` | Eases camera rotation between frames to reduce jitter; only affects what's rendered, never your actual aim |
| `smoothCameraSpeed` | `35.0` | How tightly the camera follows your real aim. Higher = snappier (less smoothing), lower = smoother (more lag) |

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
