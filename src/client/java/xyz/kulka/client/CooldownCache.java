package xyz.kulka.client;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public final class CooldownCache {

    private static final Map<Identifier, Integer> durations = new HashMap<>();
    private static final Map<Identifier, Text> usedNames = new HashMap<>();
    private static final Map<Identifier, ItemStack> usedStacks = new HashMap<>();

    public static void set(Identifier group, int duration) {
        durations.put(group, duration);
    }

    public static void remove(Identifier group) {
        durations.remove(group);
        usedNames.remove(group);
        usedStacks.remove(group);
    }

    public static void setUsedName(Identifier group, Text name) {
        usedNames.put(group, name);
    }

    public static Text getUsedName(Identifier group) {
        return usedNames.get(group);
    }

    public static void setUsedStack(Identifier group, ItemStack stack) {
        usedStacks.put(group, stack);
    }

    public static ItemStack getUsedStack(Identifier group) {
        return usedStacks.get(group);
    }

    public static float getRemainingSeconds(Identifier group, float progress) {
        int duration = durations.getOrDefault(group, 0);
        return Math.max(0.0f, progress * duration / 20.0f);
    }
}
