package com.lutia.autoignition.logic;

import com.hypixel.hytale.builtin.crafting.state.ProcessingBenchState;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.util.Config;
import com.hypixel.hytale.logger.HytaleLogger;
import com.lutia.autoignition.AutoIgnitionConfig;
import com.lutia.autoignition.util.BenchUtil;
import com.lutia.autoignition.util.ItemsUtil;

import java.util.*;
import java.util.logging.Level;

public class BenchItemsTransferManager {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public static final int[][] NEIGHBOR_OFFSETS = {
            {0, 1, 0}, {0, -1, 0},
            {1, 0, 0}, {-1, 0, 0},
            {0, 0, 1}, {0, 0, -1}
    };

    private final Map<ProcessingBenchState, Long> lastUpdateMap = Collections.synchronizedMap(new WeakHashMap<>());

    private final Config<AutoIgnitionConfig> config;

    public BenchItemsTransferManager(Config<AutoIgnitionConfig> config) {
        this.config = config;
    }

    public void tick(ProcessingBenchState bench) {
        try {
            internalTick(bench);
        } catch (Exception e) {
            LOGGER.at(Level.SEVERE).log("[AutoIgnition] Error in AutoIgnition BenchOutputManager: " + e.getMessage());
        }
    }

    private void internalTick(ProcessingBenchState bench) {
        ItemContainer output = BenchUtil.getOutput(bench);
        ItemContainer fuel = BenchUtil.getFuel(bench);

        List<String> validFuels = config.get().getFuelItems();
        long intervalMs = config.get().getUpdateIntervalMs();

        if (output != null && fuel != null) {
            ItemsUtil.moveItems(output, fuel,
                    item -> validFuels.contains(item.getItemId()));
        }

        if (!config.get().isEnableNearbyChestsTransfer()) {
            return;
        }

        long now = System.currentTimeMillis();
        long lastUpdate = lastUpdateMap.getOrDefault(bench, 0L);
        if (now - lastUpdate < intervalMs) {
            return;
        }
        lastUpdateMap.put(bench, now);

        boolean needsRefuel = false;
        if (fuel != null) {
            if (fuel.isEmpty()) {
                needsRefuel = true;
            } else {
                for (String fuelId : validFuels) {
                    if (fuel.canAddItemStack(new ItemStack(fuelId, 1))) {
                        needsRefuel = true;
                        break;
                    }
                }
            }
        }

        boolean needsOutputClear = (output != null && !output.isEmpty());

        if (!needsRefuel && !needsOutputClear) return;

        List<ItemContainer> neighbors = findAdjacentContainers(bench);
        if (neighbors.isEmpty()) return;

        if (needsRefuel) {
            for (ItemContainer chest : neighbors) {
                if (chest == output || chest == fuel) continue;

                ItemsUtil.moveItems(chest, fuel,
                        item -> validFuels.contains(item.getItemId()));

                boolean isFull = true;
                for (String fuelId : validFuels) {
                    if (fuel.canAddItemStack(new ItemStack(fuelId, 1))) {
                        isFull = false;
                        break;
                    }
                }
                if (isFull) break;
            }
        }

        if (needsOutputClear) {
            List<ItemContainer> validTargets = new ArrayList<>(neighbors);
            validTargets.removeIf(c -> c == output || c == fuel || (BenchUtil.getInput(bench) != null && c == BenchUtil.getInput(bench)));

            ItemsUtil.distributeItems(output, validTargets);
        }
    }

    private List<ItemContainer> findAdjacentContainers(ProcessingBenchState bench) {
        List<ItemContainer> containers = new ArrayList<>();
        @SuppressWarnings("removal")
        World world = bench.getChunk().getWorld();
        @SuppressWarnings("removal")
        Vector3i startPos = bench.getBlockPosition();

        if (world == null) return containers;

        BlockType sourceBlockType = world.getBlockType(startPos.x, startPos.y, startPos.z);
        String sourceId = (sourceBlockType != null) ? sourceBlockType.getId() : "unknown";

        Queue<Vector3i> queue = new LinkedList<>();
        Set<Vector3i> visited = new HashSet<>();

        queue.add(startPos);
        visited.add(startPos);

        int safetyLimit = 10;
        int processed = 0;

        while (!queue.isEmpty() && processed < safetyLimit) {
            Vector3i current = queue.poll();
            processed++;

            for (int[] offset : NEIGHBOR_OFFSETS) {
                Vector3i neighborPos = new Vector3i(current.x + offset[0], current.y + offset[1], current.z + offset[2]);

                if (visited.contains(neighborPos)) continue;

                @SuppressWarnings({"removal", "deprecation"})
                BlockState neighborState = world.getState(neighborPos.x, neighborPos.y, neighborPos.z, false);

                if (neighborState == null) {
                    visited.add(neighborPos);
                    continue;
                }

                BlockType neighborType = world.getBlockType(neighborPos.x, neighborPos.y, neighborPos.z);

                if (neighborState instanceof ItemContainerState) {
                    visited.add(neighborPos);
                    containers.add(((ItemContainerState) neighborState).getItemContainer());
                } else if (neighborType != null && neighborType.getId().equals(sourceId)) {
                    visited.add(neighborPos);
                    queue.add(neighborPos);
                } else {
                    visited.add(neighborPos);
                }
            }
        }
        return containers;
    }
}