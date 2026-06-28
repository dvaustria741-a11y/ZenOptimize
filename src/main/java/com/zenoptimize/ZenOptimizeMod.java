package com.zenoptimize;

import com.zenoptimize.config.ZenConfig;
import com.zenoptimize.util.FpsTracker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZenOptimizeMod implements ClientModInitializer {

    public static final String MOD_ID = "zenoptimize";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Sodium/Lithium presence flags — detected once at startup
    public static final boolean SODIUM_LOADED  = FabricLoader.getInstance().isModLoaded("sodium");
    public static final boolean LITHIUM_LOADED = FabricLoader.getInstance().isModLoaded("lithium");
    public static final boolean IRIS_LOADED    = FabricLoader.getInstance().isModLoaded("iris");

    private static ZenOptimizeMod instance;

    private int tickCounter  = 0;
    private int gcTickCounter = 0;
    private int originalRenderDistance = -1;
    private boolean dynamicRenderActive = false;

    @Override
    public void onInitializeClient() {
        instance = this;
        ZenConfig.load();

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);

        LOGGER.info("[ZenOptimize] v1.1.0 loaded — mobile optimizations active.");
        LOGGER.info("[ZenOptimize] Sodium={} Lithium={} Iris={}",
                SODIUM_LOADED, LITHIUM_LOADED, IRIS_LOADED);

        if (SODIUM_LOADED) {
            LOGGER.info("[ZenOptimize] Sodium detected — fog/chunk optimizations deferred to Sodium.");
        }
        if (LITHIUM_LOADED) {
            LOGGER.info("[ZenOptimize] Lithium detected — tick optimizations deferred to Lithium.");
        }
    }

    private void onClientTick(MinecraftClient client) {
        tickCounter++;
        gcTickCounter++;

        if (tickCounter >= 20) {
            tickCounter = 0;
            onFpsTick(client);
        }
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

        boolean lowFps  = FpsTracker.isBelowTarget(ZenConfig.targetFps);
        boolean goodFps = FpsTracker.isAboveTarget(ZenConfig.targetFps);

        if (lowFps && !dynamicRenderActive) {
            int current = rdOption.getValue();
            int reduced = Math.max(ZenConfig.minRenderDistance, current - 2);
            if (reduced != current) {
                rdOption.setValue(reduced);
                // Also reduce simulation distance (Sodium-safe — it is just an option value)
                SimpleOption<Integer> simOption = client.options.getSimulationDistance();
                int simReduced = Math.max(ZenConfig.minRenderDistance, simOption.getValue() - 2);
                simOption.setValue(simReduced);
                client.worldRenderer.scheduleTerrainUpdate();
                dynamicRenderActive = true;
                LOGGER.debug("[ZenOptimize] Low FPS — reduced RD to {}", reduced);
            }
        } else if (goodFps && dynamicRenderActive) {
            if (originalRenderDistance != -1) {
                rdOption.setValue(originalRenderDistance);
                client.worldRenderer.scheduleTerrainUpdate();
            }
            dynamicRenderActive = false;
            LOGGER.debug("[ZenOptimize] FPS recovered — restored RD to {}", originalRenderDistance);
        }
    }

    private void onGcTick() {
        if (!ZenConfig.periodicGc) return;
        Runtime rt = Runtime.getRuntime();
        float ratio = (float) rt.freeMemory() / rt.totalMemory();
        if (ratio < ZenConfig.gcThreshold) {
            LOGGER.debug("[ZenOptimize] Low memory ({:.1f}%) — hinting GC", ratio * 100f);
            System.gc();
        }
    }

    public static ZenOptimizeMod getInstance() { return instance; }
}
