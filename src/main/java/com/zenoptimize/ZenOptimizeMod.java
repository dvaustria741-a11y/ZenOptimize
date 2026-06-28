package com.zenoptimize;

import com.zenoptimize.config.ZenConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
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

    @Override
    public void onInitializeClient() {
        instance = this;
        ZenConfig.load();

        LOGGER.info("[ZenOptimize] v1.2.0 loaded — mobile optimizations active.");
        LOGGER.info("[ZenOptimize] Sodium={} Lithium={} Iris={}",
                SODIUM_LOADED, LITHIUM_LOADED, IRIS_LOADED);

        if (SODIUM_LOADED) {
            LOGGER.info("[ZenOptimize] Sodium detected — fog/chunk optimizations deferred to Sodium.");
        }
        if (LITHIUM_LOADED) {
            LOGGER.info("[ZenOptimize] Lithium detected — tick optimizations deferred to Lithium.");
        }
    }

    public static ZenOptimizeMod getInstance() { return instance; }
}
