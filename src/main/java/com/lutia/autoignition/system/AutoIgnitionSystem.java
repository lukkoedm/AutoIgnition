package com.lutia.autoignition.system;

import com.hypixel.hytale.builtin.crafting.state.ProcessingBenchState;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.util.Config;
import com.lutia.autoignition.AutoIgnitionConfig;
import com.lutia.autoignition.logic.BenchItemsTransferManager;
import com.lutia.autoignition.logic.BenchIgnitionManager;

import javax.annotation.Nonnull;

public class AutoIgnitionSystem extends EntityTickingSystem<ChunkStore> {
    private final ComponentType<ChunkStore, ProcessingBenchState> benchComponentType;

    private final BenchIgnitionManager benchIgnitionManager;
    private final BenchItemsTransferManager benchItemsTransferManager;

    public AutoIgnitionSystem(ComponentType<ChunkStore, ProcessingBenchState> benchComponentType, Config<AutoIgnitionConfig> config) {
        this.benchComponentType = benchComponentType;
        this.benchIgnitionManager = new BenchIgnitionManager();
        this.benchItemsTransferManager = new BenchItemsTransferManager(config);
    }

    @Override
    public void tick(
            float dt,
            int entityIndex,
            ArchetypeChunk<ChunkStore> archetypeChunk,
            @Nonnull Store<ChunkStore> store,
            @Nonnull CommandBuffer<ChunkStore> commandBuffer
    ) {
        ProcessingBenchState bench = archetypeChunk.getComponent(entityIndex, this.benchComponentType);
        if (bench == null) return;

        benchIgnitionManager.tick(bench);
        benchItemsTransferManager.tick(bench);
    }

    @Override
    public Query<ChunkStore> getQuery() {
        return this.benchComponentType;
    }

    @Override
    public boolean isParallel(int archetypeChunkSize, int taskCount) {
        return true;
    }
}