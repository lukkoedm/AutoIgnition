package com.lutia.autoignition.util;

import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;

import java.util.List;
import java.util.function.Predicate;

public class ItemsUtil {
    public static int countItems(ItemContainer container) {
        int count = 0;
        for (short i = 0; i < container.getCapacity(); i++) {
            ItemStack stack = container.getItemStack(i);
            if (stack != null) count += stack.getQuantity();
        }
        return count;
    }

    public static void moveItems(ItemContainer source, ItemContainer target, Predicate<ItemStack> filter) {
        if (source == null || target == null || source.isEmpty()) return;

        for (short i = 0; i < source.getCapacity(); i++) {
            ItemStack item = source.getItemStack(i);
            if (item != null && !item.isEmpty() && filter.test(item)) {
                if (target.canAddItemStack(item)) {
                    source.moveItemStackFromSlot(i, target);
                }
            }
        }
    }

    public static void distributeItems(ItemContainer source, List<ItemContainer> targets) {
        if (source == null || source.isEmpty() || targets.isEmpty()) return;

        for (short i = 0; i < source.getCapacity(); i++) {
            ItemStack item = source.getItemStack(i);
            if (item == null || item.isEmpty()) continue;

            for (ItemContainer target : targets) {
                if (target.canAddItemStack(item)) {
                    ItemStackTransaction trans = target.addItemStack(item);
                    if (trans.succeeded()) {
                        ItemStack remainder = trans.getRemainder();
                        if (remainder == null || remainder.isEmpty()) {
                            source.removeItemStackFromSlot(i);
                            break;
                        } else {
                            source.setItemStackForSlot(i, remainder);
                            break;
                        }
                    }
                }
            }
        }
    }
}