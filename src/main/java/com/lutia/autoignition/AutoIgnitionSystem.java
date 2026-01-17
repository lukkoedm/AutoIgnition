package com.lutia.autoignition;

import com.hypixel.hytale.builtin.crafting.state.ProcessingBenchState;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

import javax.annotation.Nonnull;

public class AutoIgnitionSystem extends EntityTickingSystem<ChunkStore> {
    private final ComponentType<ChunkStore, ProcessingBenchState> benchComponentType;

    public AutoIgnitionSystem(ComponentType<ChunkStore, ProcessingBenchState> benchComponentType) {
        this.benchComponentType = benchComponentType;
    }

    @Override
    public void tick(float dt, int entityIndex, ArchetypeChunk<ChunkStore> archetypeChunk, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
        ProcessingBenchState bench = archetypeChunk.getComponent(entityIndex, this.benchComponentType);

        if (bench == null || bench.isActive()) return;

        @SuppressWarnings("removal")
        BlockType blockType = bench.getBlockType();

        if (blockType == null || blockType.getId() == null) return;

        String id = blockType.getId().toLowerCase();
        boolean isTargetBench = id.contains("furnace") || id.contains("campfire");

        if (!isTargetBench) return;

        if (bench.getRecipe() != null) {
            bench.setActive(true);
        }
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