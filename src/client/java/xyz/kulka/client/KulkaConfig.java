package xyz.kulka.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.awt.Color;
import java.io.*;

public class KulkaConfig {

    private static final File CONFIG_FILE =
            FabricLoader.getInstance().getConfigDir()
                    .resolve("kulka-bettercooldowns.json").toFile();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static boolean enabled           = true;
    /** Fraction (0.0-1.0) of the scaled window width/height, so the HUD stays in the same relative spot across window sizes/GUI scales. */
    public static float   x                 = 10f / 854f;
    public static float   y                 = 50f / 480f;
    public static float   scale             = 1.0f;
    public static boolean useSeconds        = false;
    public static boolean percentDecimals   = true;
    public static int     bgColor           = 0x000000;
    /** 0 = fully opaque, 100 = fully transparent */
    public static int     bgTransparency    = 44;
    public static boolean showItemName      = false;

    public static void load() {
        if (!CONFIG_FILE.exists()) return;
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            ConfigData d = GSON.fromJson(reader, ConfigData.class);
            if (d == null) return;
            enabled         = d.enabled;
            // Legacy configs stored x/y as absolute pixel values; anything outside 0-1 is treated as legacy
            // and converted using the reference resolution those pixel values were originally tuned against.
            x               = (d.x > 2f || d.x < -2f) ? d.x / 854f : d.x;
            y               = (d.y > 2f || d.y < -2f) ? d.y / 480f : d.y;
            scale           = d.scale != 0 ? d.scale : 1.0f;
            useSeconds      = d.useSeconds;
            percentDecimals = d.percentDecimals != null ? d.percentDecimals : true;
            bgColor         = d.bgColorHex      != null ? parseHex(d.bgColorHex) : 0x000000;
            bgTransparency  = d.bgTransparency  != null ? d.bgTransparency  : 44;
            showItemName    = d.showItemName    != null ? d.showItemName    : false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(new ConfigData(enabled, x, y, scale, useSeconds, percentDecimals,
                    String.format("#%06X", bgColor), bgTransparency, showItemName), writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int parseHex(String hex) {
        try { return Integer.parseInt(hex.replace("#", ""), 16) & 0xFFFFFF; }
        catch (Exception e) { return 0; }
    }

    /** bgTransparency 0 = fully opaque (alpha 255), 100 = fully transparent (alpha 0) */
    public static int getBgArgb() {
        int alpha = 255 * (100 - bgTransparency) / 100;
        return (alpha << 24) | bgColor;
    }

    @SuppressWarnings("unchecked")
    public static Screen createScreen(Screen parent) {
        Option<Boolean>[] percentDecimalsOpt = new Option[1];
        return YetAnotherConfigLib.createBuilder()
                .title(Text.literal("Better Cooldowns Config"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("General Settings"))

                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Enable Mod"))
                                .binding(true, () -> enabled, v -> enabled = v)
                                .controller(TickBoxControllerBuilder::create)
                                .build())

                        .option(ButtonOption.createBuilder()
                                .name(Text.literal("Edit HUD Position"))
                                .text(Text.literal("Change Position"))
                                .action((yaclScreen, opt) -> {
                                    Screen cur = MinecraftClient.getInstance().currentScreen;
                                    MinecraftClient.getInstance().setScreen(new HudEditorScreen(cur));
                                })
                                .build())

                        .option(Option.<Float>createBuilder()
                                .name(Text.literal("Scale"))
                                .binding(1f, () -> scale, v -> scale = v)
                                .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                        .range(0.5f, 3f).step(0.1f))
                                .build())

                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Use Seconds"))
                                .binding(false, () -> useSeconds, v -> {
                                    useSeconds = v;
                                    percentDecimalsOpt[0].setAvailable(!v);
                                })
                                .controller(TickBoxControllerBuilder::create)
                                .build())

                        .option((percentDecimalsOpt[0] = Option.<Boolean>createBuilder()
                                .name(Text.literal("Percentage Decimals (xx.x%)"))
                                .binding(true, () -> percentDecimals, v -> percentDecimals = v)
                                .controller(TickBoxControllerBuilder::create)
                                .available(!useSeconds)
                                .build()))

                        .option(Option.<Color>createBuilder()
                                .name(Text.literal("Background Color"))
                                .binding(Color.BLACK,
                                        () -> new Color((bgColor >> 16) & 0xFF, (bgColor >> 8) & 0xFF, bgColor & 0xFF),
                                        v -> bgColor = (v.getRed() << 16) | (v.getGreen() << 8) | v.getBlue())
                                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(false))
                                .build())

                        .option(Option.<Integer>createBuilder()
                                .name(Text.literal("Background Transparency"))
                                .binding(44, () -> bgTransparency, v -> bgTransparency = v)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                        .range(0, 100).step(1)
                                        .formatValue(v -> Text.literal(v + "%")))
                                .build())

                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Show Item Name"))
                                .binding(false, () -> showItemName, v -> showItemName = v)
                                .controller(TickBoxControllerBuilder::create)
                                .build())

                        .build())
                .save(KulkaConfig::save)
                .build()
                .generateScreen(parent);
    }

    public static class ConfigData {
        boolean  enabled;
        float    x, y;
        float    scale;
        boolean  useSeconds;
        Boolean  percentDecimals;
        String   bgColorHex;
        Integer  bgTransparency;
        Boolean  showItemName;

        public ConfigData(boolean enabled, float x, float y, float scale, boolean useSeconds,
                          boolean percentDecimals, String bgColorHex, int bgTransparency,
                          boolean showItemName) {
            this.enabled = enabled; this.x = x; this.y = y;
            this.scale = scale; this.useSeconds = useSeconds;
            this.percentDecimals = percentDecimals; this.bgColorHex = bgColorHex;
            this.bgTransparency = bgTransparency;
            this.showItemName = showItemName;
        }
    }
}
