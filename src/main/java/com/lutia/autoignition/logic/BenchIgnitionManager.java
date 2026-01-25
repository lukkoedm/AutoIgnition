package com.lutia.autoignition.logic;

import com.hypixel.hytale.builtin.crafting.state.ProcessingBenchState;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.lutia.autoignition.util.BenchUtil;
import com.lutia.autoignition.util.ItemsUtil;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;

public class BenchIgnitionManager {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final Map<ProcessingBenchState, BenchStateTracker> trackers = Collections.synchronizedMap(new WeakHashMap<>());

    public void tick(ProcessingBenchState bench) {
        BenchStateTracker tracker = trackers.computeIfAbsent(bench, this::initializeTracker);

        if (bench.isActive()) {
            tracker.resetRequest();
            tracker.syncState(bench);
        } else {
            if (tracker.isIgnitionRequested()) {
                if (bench.getRecipe() != null) {
                    bench.setActive(true);
                    tracker.resetRequest();
                }
            }
        }
    }

    private BenchStateTracker initializeTracker(ProcessingBenchState bench) {
        BenchStateTracker tracker = new BenchStateTracker();

        try {
            ItemContainer input = BenchUtil.getInput(bench);
            ItemContainer fuel = BenchUtil.getFuel(bench);

            if (input != null) {
                tracker.lastInputCount = ItemsUtil.countItems(input);

                input.registerChangeEvent(event -> {
                    synchronized (tracker) {
                        int currentCount = ItemsUtil.countItems(input);
                        if (currentCount > tracker.lastInputCount) {
                            tracker.requestIgnition();
                        }
                        tracker.lastInputCount = currentCount;
                    }
                });
            }

            if (fuel != null) {
                tracker.lastFuelEmpty = fuel.isEmpty();

                fuel.registerChangeEvent(event -> {
                    synchronized (tracker) {
                        boolean currentEmpty = fuel.isEmpty();
                        if (tracker.lastFuelEmpty && !currentEmpty) {
                            tracker.requestIgnition();
                        }
                        tracker.lastFuelEmpty = currentEmpty;
                    }
                });
            }

        } catch (Exception e) {
            LOGGER.at(Level.WARNING).log("[AutoIgnition] Failed to register inventory listeners for bench at " + bench.getBlockPosition());
        }

        return tracker;
    }

    private static class BenchStateTracker {
        volatile int lastInputCount = 0;
        volatile boolean lastFuelEmpty = true;
        private volatile boolean ignitionRequested = false;

        void requestIgnition() {
            this.ignitionRequested = true;
        }

        void resetRequest() {
            this.ignitionRequested = false;
        }

        boolean isIgnitionRequested() {
            return ignitionRequested;
        }

        void syncState(ProcessingBenchState bench) {
            ItemContainer input = BenchUtil.getInput(bench);
            ItemContainer fuel = BenchUtil.getFuel(bench);

            synchronized (this) {
                if (input != null) {
                    this.lastInputCount = ItemsUtil.countItems(input);
                }
                if (fuel != null) {
                    this.lastFuelEmpty = fuel.isEmpty();
                }
            }
        }
    }
}