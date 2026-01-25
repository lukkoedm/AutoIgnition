package com.lutia.autoignition;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AutoIgnitionConfig {
    public static final BuilderCodec<AutoIgnitionConfig> CODEC =
            BuilderCodec.builder(AutoIgnitionConfig.class, AutoIgnitionConfig::new)
                    .append(new KeyedCodec<>("UpdateIntervalMs", Codec.LONG),
                            (config, value, info) -> config.updateIntervalMs = value,
                            (config, info) -> config.updateIntervalMs)
                    .add()
                    .append(new KeyedCodec<>("EnableNearbyChestsTransfer", Codec.BOOLEAN),
                            (config, value, info) -> config.enableNearbyChestsTransfer = value,
                            (config, info) -> config.enableNearbyChestsTransfer)
                    .add()
                    .append(new KeyedCodec<>("FuelItems", Codec.STRING_ARRAY),
                            (config, value, info) -> config.fuelItems = new ArrayList<>(Arrays.asList(value)),
                            (config, info) -> config.fuelItems.toArray(new String[0]))
                    .add()
                    .build();

    private boolean enableNearbyChestsTransfer = true;
    private long updateIntervalMs = 500L;
    private List<String> fuelItems = new ArrayList<>(List.of("Ingredient_Charcoal"));

    public boolean isEnableNearbyChestsTransfer() {
        return enableNearbyChestsTransfer;
    }

    public long getUpdateIntervalMs() {
        return Math.max(50L, updateIntervalMs);
    }

    public List<String> getFuelItems() {
        return fuelItems;
    }
}