package xyz.kulka.client;

import java.util.HashSet;
import java.util.Set;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.component.DataComponentTypes;
import org.joml.Matrix3x2fStack;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class KulkaBettercooldownsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        KulkaConfig.load();
        HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS,
                Identifier.of("kulka-bettercooldowns", "cooldown_hud"), this::onHudRender);
    }

    private void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        if (!KulkaConfig.enabled) return;

        float tickDelta = tickCounter.getTickProgress(true);

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;

        PlayerEntity player   = client.player;
        TextRenderer renderer = client.textRenderer;
        float scale           = KulkaConfig.scale;

        int drawX    = (int)((KulkaConfig.x * context.getScaledWindowWidth()) / scale);
        int currentY = (int)((KulkaConfig.y * context.getScaledWindowHeight()) / scale);

        ItemCooldownManager cooldownManager = player.getItemCooldownManager();
        Set<Identifier> renderedGroups = new HashSet<>();

        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.scale(scale, scale);

        int bgArgb = KulkaConfig.getBgArgb();

        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;

            Identifier group = cooldownManager.getGroup(stack);
            float progress = cooldownManager.getCooldownProgress(stack, tickDelta);
            if (progress <= 0.0f || renderedGroups.contains(group)) continue;

            ItemStack usedStack = CooldownCache.getUsedStack(group);
            ItemStack displayStack = usedStack != null ? usedStack : stack;

            String raw;
            if (KulkaConfig.useSeconds) {
                float remaining = CooldownCache.getRemainingSeconds(group, progress);
                raw = String.format("%.1fs", remaining);
            } else {
                String fmt = KulkaConfig.percentDecimals ? "%.1f%%" : "%.0f%%";
                raw = String.format(fmt, progress * 100.0f);
            }

            int textWidth = renderer.getWidth(raw);

            if (KulkaConfig.showItemName) {
                Text cached   = CooldownCache.getUsedName(group);
                Text nameText = cached != null ? cached : resolveItemName(displayStack);
                String nameStr = nameText.getString();
                int nameWidth  = renderer.getWidth(nameStr);

                context.fill(drawX - 2, currentY - 2,
                        drawX + 20 + nameWidth + 4 + textWidth + 2, currentY + 16,
                        bgArgb);

                context.drawItem(displayStack, drawX, currentY);
                context.drawText(renderer, nameStr, drawX + 20,                 currentY + 4, 0xFFFFFFFF, false);
                context.drawText(renderer, raw,     drawX + 20 + nameWidth + 4, currentY + 4, 0xFFFFFFFF, false);
            } else {
                context.fill(drawX - 2, currentY - 2,
                        drawX + 24 + textWidth, currentY + 16,
                        bgArgb);

                context.drawItem(displayStack, drawX, currentY);
                context.drawText(renderer, raw, drawX + 20, currentY + 4, 0xFFFFFFFF, false);
            }

            renderedGroups.add(group);
            currentY += 20;
        }

        matrices.popMatrix();
    }

    public static Text resolveItemName(ItemStack stack) {
        Text customName = stack.getComponents().get(DataComponentTypes.CUSTOM_NAME);
        return customName != null ? customName : stack.getItem().getName(stack);
    }
}
