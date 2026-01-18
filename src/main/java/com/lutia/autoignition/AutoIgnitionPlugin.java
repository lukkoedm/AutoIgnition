package com.lutia.autoignition;

import com.hypixel.hytale.builtin.crafting.state.ProcessingBenchState;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.meta.BlockStateModule;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

import javax.annotation.Nonnull;
import java.util.logging.Level;

public class AutoIgnitionPlugin extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public AutoIgnitionPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void start() {
        @SuppressWarnings("removal")
        ComponentType<ChunkStore, ProcessingBenchState> benchType =
                BlockStateModule.get().getComponentType(ProcessingBenchState.class);

        if (benchType != null) {
            this.getChunkStoreRegistry().registerSystem(new AutoIgnitionSystem(benchType));
        } else {
            LOGGER.at(Level.SEVERE).log("Could not find ProcessingBenchState component!");
        }
    }
}