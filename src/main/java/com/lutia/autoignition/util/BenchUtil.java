package com.lutia.autoignition.util;

import com.hypixel.hytale.builtin.crafting.state.ProcessingBenchState;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;

import java.lang.reflect.Field;
import java.util.logging.Level;

public class BenchUtil {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static Field outputField;
    private static Field fuelField;
    private static Field inputField;

    static {
        try {
            fuelField = setAccessible("fuelContainer");
            inputField = setAccessible("inputContainer");
            outputField = setAccessible("outputContainer");
        } catch (NoSuchFieldException e) {
            LOGGER.at(Level.SEVERE).log("[AutoIgnition] Failed to map fields: " + e.getMessage());
        }
    }

    private static Field setAccessible(String name) throws NoSuchFieldException {
        Field f = ProcessingBenchState.class.getDeclaredField(name);
        f.setAccessible(true);
        return f;
    }

    public static ItemContainer getFuel(ProcessingBenchState bench) {
        return getContainer(fuelField, bench);
    }

    public static ItemContainer getInput(ProcessingBenchState bench) {
        return getContainer(inputField, bench);
    }

    public static ItemContainer getOutput(ProcessingBenchState bench) {
        return getContainer(outputField, bench);
    }

    private static ItemContainer getContainer(Field field, ProcessingBenchState bench) {
        if (field == null || bench == null) return null;
        try {
            return (ItemContainer) field.get(bench);
        } catch (IllegalAccessException e) {
            return null;
        }
    }
}