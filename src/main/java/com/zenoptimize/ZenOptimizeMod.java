package com.zenoptimize;

import com.zenoptimize.config.ZenConfig;
import com.zenoptimize.util.FpsTracker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZenOptimizeMod implements ClientModInitializer {

    public static final String MOD_ID = "zenoptimize";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static ZenOptimizeMod instance;

    private int tickCounter = 0;
    private int originalRenderDistance = -1;
    private boolean dynamicRenderActive = false;
    private int gcTickCounter = 0;

    @Override
    public void onInitializeClient() {
        instance = this;
        ZenConfig.load();

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);

        LOGGER.info("[ZenOptimize] v{} loaded — mobile launcher optimizations active.", "1.0.0");
        LOGGER.info("[ZenOptimize] Dynamic RD={}, Particle limit={}, Periodic GC={}",
                ZenConfig.dynamicRenderDistance, ZenConfig.limitParticles, ZenConfig.periodicGc);
    }

    private void onClientTick(MinecraftClient client) {
        tickCounter++;
        gcTickCounter++;

        // Every second: check FPS and adjust render distance
        if (tickCounter >= 20) {
            tickCounter = 0;
            onFpsTick(client);
        }

        // Every 30 seconds: GC check
        if (gcTickCounter >= 600) {
            gcTickCounter = 0;
            onGcTick();
        }
    }

    private void onFpsTick(MinecraftClient client) {
        if (!ZenConfig.dynamicRenderDistance) return;
        if (client.world == null || client.player == null) return;

        SimpleOption<Integer> rdOption = client.options.getViewDistance();

        if (originalRenderDistance == -1) {
            originalRenderDistance = rdOption.getValue();
        }

        if (FpsTracker.isBelowTarget(ZenConfig.targetFps) && !dynamicRenderActive) {
            int current = rdOption.getValue();
            int reduced = Math.max(ZenConfig.minRenderDistance, current - 2);
            if (reduced != current) {
                rdOption.setValue(reduced);
                client.worldRenderer.scheduleTerrainUpdate();
                dynamicRenderActive = true;
                LOGGER.debug("[ZenOptimize] Low FPS ({:.1f}) — reducing render distance to {}",
                        FpsTracker.getAverageFps(), reduced);
            }
        } else if (FpsTracker.isAboveTarget(ZenConfig.targetFps) && dynamicRenderActive) {
            if (originalRenderDistance != -1) {
                rdOption.setValue(originalRenderDistance);
                client.worldRenderer.scheduleTerrainUpdate();
            }
            dynamicRenderActive = false;
            LOGGER.debug("[ZenOptimize] FPS recovered — restoring render distance to {}",
                    originalRenderDistance);
        }
    }

    private void onGcTick() {
        if (!ZenConfig.periodicGc) return;
        Runtime rt = Runtime.getRuntime();
        long free = rt.freeMemory();
        long total = rt.totalMemory();
        float ratio = (float) free / total;
        if (ratio < ZenConfig.gcThreshold) {
            LOGGER.debug("[ZenOptimize] Low free memory ({:.1f}%) — hinting GC", ratio * 100f);
            System.gc();
        }
    }

    public static ZenOptimizeMod getInstance() {
        return instance;
    }
}