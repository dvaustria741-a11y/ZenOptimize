package com.zenoptimize.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.*;
import java.nio.file.Path;

public class ZenConfig {

    // Dynamic Render Distance
    public static boolean dynamicRenderDistance = true;
    public static int targetFps = 30;
    public static int minRenderDistance = 2;

    // Particles
    public static boolean limitParticles = true;
    public static int maxParticles = 200;

    // Memory
    public static boolean periodicGc = true;
    public static float gcThreshold = 0.15f; // GC when <15% free

    // Rendering
    public static boolean reduceFogDensity = false;
    public static boolean skipOffscreenEntities = true;

    // Mobile frame cap (0 = disabled)
    public static int mobileFrameCap = 60;

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
        dynamicRenderDistance = d.dynamicRenderDistance;
        targetFps = d.targetFps;
        minRenderDistance = d.minRenderDistance;
        limitParticles = d.limitParticles;
        maxParticles = d.maxParticles;
        periodicGc = d.periodicGc;
        gcThreshold = d.gcThreshold;
        reduceFogDensity = d.reduceFogDensity;
        skipOffscreenEntities = d.skipOffscreenEntities;
        mobileFrameCap = d.mobileFrameCap;
    }

    private static ZenConfigData buildData() {
        ZenConfigData d = new ZenConfigData();
        d.dynamicRenderDistance = dynamicRenderDistance;
        d.targetFps = targetFps;
        d.minRenderDistance = minRenderDistance;
        d.limitParticles = limitParticles;
        d.maxParticles = maxParticles;
        d.periodicGc = periodicGc;
        d.gcThreshold = gcThreshold;
        d.reduceFogDensity = reduceFogDensity;
        d.skipOffscreenEntities = skipOffscreenEntities;
        d.mobileFrameCap = mobileFrameCap;
        return d;
    }

    public static class ZenConfigData {
        public boolean dynamicRenderDistance = true;
        public int targetFps = 30;
        public int minRenderDistance = 2;
        public boolean limitParticles = true;
        public int maxParticles = 200;
        public boolean periodicGc = true;
        public float gcThreshold = 0.15f;
        public boolean reduceFogDensity = false;
        public boolean skipOffscreenEntities = true;
        public int mobileFrameCap = 60;
    }
}