package com.lutia.autoignition;

import com.hypixel.hytale.builtin.crafting.state.ProcessingBenchState;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.meta.BlockStateModule;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.util.Config;
import com.lutia.autoignition.system.AutoIgnitionSystem;

import javax.annotation.Nonnull;
import java.util.logging.Level;

public class AutoIgnitionPlugin extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private final Config<AutoIgnitionConfig> config;

    public AutoIgnitionPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        this.config = this.withConfig("AutoIgnitionConfig", AutoIgnitionConfig.CODEC);
    }

    @Override
    public void setup() {
        config.save();
    }

    @Override
    protected void start() {
        @SuppressWarnings("removal")
        ComponentType<ChunkStore, ProcessingBenchState> benchType =
                BlockStateModule.get().getComponentType(ProcessingBenchState.class);

        if (benchType != null) {
            this.getChunkStoreRegistry().registerSystem(new AutoIgnitionSystem(benchType, config));
        } else {
            LOGGER.at(Level.SEVERE).log("[AutoIgnition] Could not register AutoIgnitionSystem!");
        }
    }
}