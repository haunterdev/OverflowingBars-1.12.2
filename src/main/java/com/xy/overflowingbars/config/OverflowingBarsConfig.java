package com.xy.overflowingbars.config;

import com.xy.overflowingbars.OverflowingBars;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;

/**
 * Forge 1.12.2 configuration mirroring the structure of the original Puzzles Lib
 * ClientConfig. Values are grouped into nested holders so renderer code can read
 * them exactly like the 1.20.1 source (e.g. {@code OverflowingBarsConfig.health.colorizeFirstRow}).
 */
public class OverflowingBarsConfig {

    public static final IconRowConfig health = new IconRowConfig();
    public static final ArmorRowConfig armor = new ArmorRowConfig();
    public static final ToughnessRowConfig toughness = new ToughnessRowConfig();
    public static final RowCountConfig rowCount = new RowCountConfig();

    public static boolean moveChatAboveArmor = true;

    /** Debug overrides: when > 0, force the rendered armor/toughness point count so the
     *  overflow rows can be tested without modded armor. 0 = use real values. */
    public static int debugArmorPoints = 0;
    public static int debugToughnessPoints = 0;
    public static int debugHealthPoints = 0;

    private static Configuration config;

    public static class IconRowConfig {
        public boolean allowLayers = true;
        public boolean allowCount = true;
        public boolean colorizeFirstRow = false;
        public boolean inverseColoring = false;
    }

    public static class ArmorRowConfig extends IconRowConfig {
        public boolean skipEmptyArmorPoints = true;
    }

    public static class ToughnessRowConfig extends ArmorRowConfig {
        public boolean armorToughnessBar = true;
        public boolean leftSide = false;
    }

    public static class RowCountConfig {
        /** Resolved RGB color (0xRRGGBB) of the row count text. */
        public int rowCountColor = 0xFFFFFF;
        public boolean forceFontRenderer = false;
        public boolean countFullRowsOnly = false;
        public boolean alwaysRenderRowCount = false;
        public boolean rowCountX = true;
    }

    public static void init(File configFile) {
        config = new Configuration(configFile);
        load();
    }

    private static void load() {
        // health
        health.allowLayers = config.getBoolean("allowLayers", "health", true,
                "Add layers to this bar. When disabled any modifications to the bar from this mod will be turned off.");
        health.allowCount = config.getBoolean("allowCount", "health", true,
                "Render row count to indicate total amount of rows since not all may be visible at once due to the stacked rendering.");
        health.colorizeFirstRow = config.getBoolean("colorizeFirstRow", "health", false,
                "Show colorful icons on the front row, not just on all subsequent rows.");
        health.inverseColoring = config.getBoolean("inverseColoring", "health", false,
                "Use vanilla's icons on all front rows, use custom colored icons on the background row.");

        // armor
        armor.allowLayers = config.getBoolean("allowLayers", "armor", true,
                "Add layers to this bar. When disabled any modifications to the bar from this mod will be turned off.");
        armor.allowCount = config.getBoolean("allowCount", "armor", true,
                "Render row count to indicate total amount of rows since not all may be visible at once due to the stacked rendering.");
        armor.colorizeFirstRow = config.getBoolean("colorizeFirstRow", "armor", false,
                "Show colorful icons on the front row, not just on all subsequent rows.");
        armor.inverseColoring = config.getBoolean("inverseColoring", "armor", false,
                "Use vanilla's icons on all front rows, use custom colored icons on the background row.");
        armor.skipEmptyArmorPoints = config.getBoolean("skipEmptyArmorPoints", "armor", true,
                "Don't draw empty armor points, this will make the armor bar potentially shorter.");

        // toughness
        toughness.allowLayers = config.getBoolean("allowLayers", "toughness", true,
                "Add layers to this bar. When disabled any modifications to the bar from this mod will be turned off.");
        toughness.allowCount = config.getBoolean("allowCount", "toughness", true,
                "Render row count to indicate total amount of rows since not all may be visible at once due to the stacked rendering.");
        toughness.colorizeFirstRow = config.getBoolean("colorizeFirstRow", "toughness", false,
                "Show colorful icons on the front row, not just on all subsequent rows.");
        toughness.inverseColoring = config.getBoolean("inverseColoring", "toughness", false,
                "Use vanilla's icons on all front rows, use custom colored icons on the background row.");
        toughness.skipEmptyArmorPoints = config.getBoolean("skipEmptyArmorPoints", "toughness", true,
                "Don't draw empty armor points, this will make the toughness bar potentially shorter.");
        toughness.armorToughnessBar = config.getBoolean("armorToughnessBar", "toughness", true,
                "Render a separate armor bar for the armor toughness attribute (from diamond and netherite armor).");
        toughness.leftSide = config.getBoolean("leftSide", "toughness", false,
                "Render the toughness bar on the left side above the hotbar (where health and armor is rendered).");

        // rowCount
        String colorName = config.getString("rowCountColor", "rowCount", "WHITE",
                "Color of row count, use any chat formatting color value.",
                new String[]{"BLACK", "DARK_BLUE", "DARK_GREEN", "DARK_AQUA", "DARK_RED", "DARK_PURPLE",
                        "GOLD", "GRAY", "DARK_GRAY", "BLUE", "GREEN", "AQUA", "RED", "LIGHT_PURPLE", "YELLOW", "WHITE"});
        rowCount.rowCountColor = colorToRGB(colorName);
        rowCount.forceFontRenderer = config.getBoolean("forceFontRenderer", "rowCount", false,
                "Force drawing row count using the font renderer, will make numbers display larger.");
        rowCount.countFullRowsOnly = config.getBoolean("countFullRowsOnly", "rowCount", false,
                "Only include completely filled rows for the row count.");
        rowCount.alwaysRenderRowCount = config.getBoolean("alwaysRenderRowCount", "rowCount", false,
                "Show row count also when only one row is present.");
        rowCount.rowCountX = config.getBoolean("rowCountX", "rowCount", true,
                "Render an 'x' together with the row count number.");

        // general
        moveChatAboveArmor = config.getBoolean("moveChatAboveArmor", "general", true,
                "Move chat messages above armor/absorption bar.");

        debugArmorPoints = config.getInt("debugArmorPoints", "debug", 0, 0, 200,
                "TEST: force armor point count (0 = off). Set e.g. 45 to preview armor overflow rows without modded armor.");
        debugToughnessPoints = config.getInt("debugToughnessPoints", "debug", 0, 0, 200,
                "TEST: force armor toughness point count (0 = off). Set e.g. 45 to preview toughness overflow rows.");
        debugHealthPoints = config.getInt("debugHealthPoints", "debug", 0, 0, 400,
                "TEST: force health point count (0 = off). Set e.g. 50 to preview health overflow coloring + row count without healing.");

        if (config.hasChanged()) {
            config.save();
        }
    }

    /** Maps a vanilla chat-formatting color name to its 0xRRGGBB value, defaulting to white. */
    private static int colorToRGB(String name) {
        try {
            // TextFormatting in 1.12.2 has no direct RGB getter; use the vanilla color table.
            return vanillaColor(TextFormatting.valueOf(name));
        } catch (IllegalArgumentException e) {
            return 0xFFFFFF;
        }
    }

    private static int vanillaColor(TextFormatting formatting) {
        switch (formatting) {
            case BLACK: return 0x000000;
            case DARK_BLUE: return 0x0000AA;
            case DARK_GREEN: return 0x00AA00;
            case DARK_AQUA: return 0x00AAAA;
            case DARK_RED: return 0xAA0000;
            case DARK_PURPLE: return 0xAA00AA;
            case GOLD: return 0xFFAA00;
            case GRAY: return 0xAAAAAA;
            case DARK_GRAY: return 0x555555;
            case BLUE: return 0x5555FF;
            case GREEN: return 0x55FF55;
            case AQUA: return 0x55FFFF;
            case RED: return 0xFF5555;
            case LIGHT_PURPLE: return 0xFF55FF;
            case YELLOW: return 0xFFFF55;
            case WHITE:
            default: return 0xFFFFFF;
        }
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (OverflowingBars.MODID.equals(event.getModID()) && config != null) {
            load();
        }
    }
}
