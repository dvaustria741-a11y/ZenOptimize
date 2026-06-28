package com.zenoptimize.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.*;
import java.nio.file.Path;

public class ZenConfig {

    // FPS threshold used by the offscreen entity culling feature below
    public static int targetFps = 30;

    // Particles
    public static boolean limitParticles = true;
    public static int maxParticles = 200;

    // Rendering
    public static boolean reduceFogDensity = false;
    public static boolean skipOffscreenEntities = true;

    // Mobile frame cap (0 = disabled)
    public static int mobileFrameCap = 60;

    // Smooth Camera — eases camera rotation, doesn't touch actual aim input
    public static boolean smoothCamera = true;
    public static double smoothCameraSpeed = 35.0; // higher = snappier/less delay, lower = smoother/more lag

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("zenoptimize.json");

    public static void load() {
        File file = CONFIG_PATH.toFile();
        if (!file.exists()) {
            save();
            return;
        }
        try (Reader reader = new FileReader(file)) {
            ZenConfigData data = GSON.fromJson(reader, ZenConfigData.class);
            if (data != null) applyData(data);
        } catch (Exception e) {
            save();
        }
    }

    public static void save() {
        try (Writer writer = new FileWriter(CONFIG_PATH.toFile())) {
            GSON.toJson(buildData(), writer);
        } catch (Exception ignored) {}
    }

    private static void applyData(ZenConfigData d) {
        targetFps = d.targetFps;
        limitParticles = d.limitParticles;
        maxParticles = d.maxParticles;
        reduceFogDensity = d.reduceFogDensity;
        skipOffscreenEntities = d.skipOffscreenEntities;
        mobileFrameCap = d.mobileFrameCap;
        smoothCamera = d.smoothCamera;
        smoothCameraSpeed = d.smoothCameraSpeed;
    }

    private static ZenConfigData buildData() {
        ZenConfigData d = new ZenConfigData();
        d.targetFps = targetFps;
        d.limitParticles = limitParticles;
        d.maxParticles = maxParticles;
        d.reduceFogDensity = reduceFogDensity;
        d.skipOffscreenEntities = skipOffscreenEntities;
        d.mobileFrameCap = mobileFrameCap;
        d.smoothCamera = smoothCamera;
        d.smoothCameraSpeed = smoothCameraSpeed;
        return d;
    }

    // NOTE: old config files on disk may still contain dynamicRenderDistance,
    // minRenderDistance, periodicGc, gcThreshold from before 1.2.0 — Gson
    // simply ignores fields that no longer exist on ZenConfigData, so this
    // loads fine without any migration step needed.
    public static class ZenConfigData {
        public int targetFps = 30;
        public boolean limitParticles = true;
        public int maxParticles = 200;
        public boolean reduceFogDensity = false;
        public boolean skipOffscreenEntities = true;
        public int mobileFrameCap = 60;
        public boolean smoothCamera = true;
        public double smoothCameraSpeed = 35.0;
    }
}
