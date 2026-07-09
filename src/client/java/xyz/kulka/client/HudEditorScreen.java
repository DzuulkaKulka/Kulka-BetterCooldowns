package xyz.kulka.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;

public class HudEditorScreen extends Screen {

    private final Screen parent;
    private int hudX;
    private int hudY;
    private boolean dragging = false;
    private double dragOffsetX, dragOffsetY;

    private static final ItemStack PREVIEW_STACK = new ItemStack(Items.ENDER_PEARL);

    public HudEditorScreen(Screen parent) {
        super(Text.literal("Edit HUD Position"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.hudX = (int) (KulkaConfig.x * this.width);
        this.hudY = (int) (KulkaConfig.y * this.height);

        int cx   = this.width / 2;
        int btnY = this.height - 30;

        addDrawableChild(ButtonWidget.builder(Text.literal("Save & Close"), btn -> {
            KulkaConfig.x = hudX / (float) this.width;
            KulkaConfig.y = hudY / (float) this.height;
            KulkaConfig.save();
            MinecraftClient.getInstance().setScreen(parent);
        }).dimensions(cx - 105, btnY, 100, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), btn ->
                MinecraftClient.getInstance().setScreen(parent)
        ).dimensions(cx + 5, btnY, 100, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Drag the element to reposition it"), this.width / 2, 8, 0xAAAAAA);
        context.drawCenteredTextWithShadow(textRenderer,
                String.format("X: %d   Y: %d", hudX, hudY), this.width / 2, 20, 0xFFFFFF);

        String raw      = buildPreviewRaw();
        float scale     = KulkaConfig.scale;
        int textWidth   = textRenderer.getWidth(raw);
        int bgArgb      = KulkaConfig.getBgArgb();

        String nameStr = KulkaConfig.showItemName ? PREVIEW_STACK.getName().getString() : null;
        int nameWidth  = nameStr != null ? textRenderer.getWidth(nameStr) : 0;
        int nameGap    = nameStr != null ? 4 : 0;
        int bgRight    = nameStr != null ? (20 + nameWidth + nameGap + textWidth + 2) : (24 + textWidth);

        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(hudX, hudY);
        matrices.scale(scale, scale);

        context.fill(-2, -2, bgRight, 16, bgArgb);

        if (isOverPreview(mouseX, mouseY)) {
            context.fill(-2, -2, bgRight, 16, 0x30FFFFFF);
        }

        context.drawItem(PREVIEW_STACK, 0, 0);
        if (nameStr != null) {
            context.drawText(textRenderer, nameStr, 20,                       4, 0xFFFFFFFF, false);
            context.drawText(textRenderer, raw,     20 + nameWidth + nameGap, 4, 0xFFFFFFFF, false);
        } else {
            context.drawText(textRenderer, raw, 20, 4, 0xFFFFFFFF, false);
        }

        matrices.popMatrix();

        super.render(context, mouseX, mouseY, delta);
    }

    private String buildPreviewRaw() {
        if (KulkaConfig.useSeconds) return "15.0s";
        return KulkaConfig.percentDecimals ? "100.0%" : "100%";
    }

    private boolean isOverPreview(double mouseX, double mouseY) {
        float scale    = KulkaConfig.scale;
        String raw     = buildPreviewRaw();
        int textWidth  = textRenderer.getWidth(raw);
        String nameStr = KulkaConfig.showItemName ? PREVIEW_STACK.getName().getString() : null;
        int nameWidth  = nameStr != null ? textRenderer.getWidth(nameStr) : 0;
        int nameGap    = nameStr != null ? 4 : 0;
        int bgRight    = nameStr != null ? (20 + nameWidth + nameGap + textWidth + 2) : (24 + textWidth);
        return mouseX >= hudX - 2 * scale && mouseX <= hudX + bgRight * scale
                && mouseY >= hudY - 2 * scale && mouseY <= hudY + 16 * scale;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        if (click.button() == 0 && isOverPreview(mouseX, mouseY)) {
            dragging     = true;
            dragOffsetX  = mouseX - hudX;
            dragOffsetY  = mouseY - hudY;
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double dX, double dY) {
        if (dragging && click.button() == 0) {
            hudX = (int) Math.max(0, click.x() - dragOffsetX);
            hudY = (int) Math.max(0, click.y() - dragOffsetY);
            return true;
        }
        return super.mouseDragged(click, dX, dY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (click.button() == 0) dragging = false;
        return super.mouseReleased(click);
    }

    @Override
    public boolean shouldPause() { return true; }
}
