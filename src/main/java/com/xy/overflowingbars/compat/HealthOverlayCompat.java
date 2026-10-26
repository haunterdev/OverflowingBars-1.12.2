package com.xy.overflowingbars.compat;

import com.xy.overflowingbars.client.handler.HealthBarRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Draws two other mods' heart overlays on the single layered health row.
 *
 * <p>Farmer's Delight Legacy (the Comfort sheen) and Brewin' And Chewin' Legacy (Tipsy hearts)
 * place their overlay with vanilla's heart layout: one row per 20 health, rows stacked upwards.
 * With health layered into one row, that puts the Comfort sheen a row below the hearts and the
 * Tipsy hearts past 20 health on a row that is never drawn. The late mixins in
 * {@code mixin.compat} cancel both draw methods while this mod owns the health bar and call
 * these instead, which repeat the same drawing on the hearts exactly where they were drawn this
 * frame. Both mods are MIT licensed; the drawing follows their
 * {@code ComfortHealthOverlay.renderComfortOverlay} and
 * {@code BNCClientEffectEvents.renderTipsyHearts}.
 */
public final class HealthOverlayCompat {

    private static final ResourceLocation FD_ICONS = new ResourceLocation("farmersdelight", "textures/gui/fd_icons.png");

    private static boolean ownsHealthRow;
    private static boolean tipsyTicksLookedUp;
    private static MethodHandle tipsyTicks;

    private HealthOverlayCompat() {
    }

    /** True only while the Post(HEALTH) this mod fires after drawing its own health bar runs. */
    public static void setOwnsHealthRow(boolean owns) {
        ownsHealthRow = owns;
    }

    public static boolean renderComfort(Minecraft mc) {
        if (!ownsHealthRow) return false;
        HealthBarRenderer hearts = HealthBarRenderer.INSTANCE;
        int sheen = mc.ingameGUI.getUpdateCounter() % 50;
        int frame = sheen % 2;
        int sheenColumn = sheen / 2;

        GlStateManager.enableBlend();
        mc.getTextureManager().bindTexture(FD_ICONS);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        for (int column = 0; column < hearts.rowHearts(); ++column) {
            int x = hearts.heartX(column);
            int y = hearts.heartY(column);
            if (column == sheenColumn) {
                Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 9.0F, frame == 0 ? 5 : 9, 9, 256.0F, 256.0F);
            }
            if (column == sheenColumn - 1 && frame == 0) {
                Gui.drawModalRectWithCustomSizedTexture(x + 5, y, 5.0F, 9.0F, 4, 9, 256.0F, 256.0F);
            }
        }
        mc.getTextureManager().bindTexture(Gui.ICONS);
        GlStateManager.disableBlend();
        return true;
    }

    public static boolean renderTipsyHearts(Minecraft mc, float tipsyDamage) {
        if (!ownsHealthRow || mc.player == null) return false;
        HealthBarRenderer hearts = HealthBarRenderer.INSTANCE;
        int health = MathHelper.ceil(mc.player.getHealth());
        int remaining = MathHelper.ceil(tipsyDamage);
        int end = Math.max(0, (health + 1) / 2);
        int start = Math.min(end, Math.max(0, (health - remaining + 1) / 2));
        // One layered row shows at most its own width of the newest hearts.
        start = Math.max(start, end - hearts.rowHearts());
        int textureY = mc.world.getWorldInfo().isHardcoreModeEnabled() ? 45 : 0;

        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 0.62F, 0.22F, tipsyDamageTicks() < 80 ? 0.65F : 0.9F);
        mc.getTextureManager().bindTexture(Gui.ICONS);
        for (int i = start; i < end; i++) {
            int column = i % 10;
            if (column >= hearts.rowHearts()) continue;
            int heartsBefore = Math.max(0, i * 2 - (health - remaining));
            boolean half = remaining - heartsBefore == 1;
            Gui.drawModalRectWithCustomSizedTexture(hearts.heartX(column), hearts.heartY(column),
                    half ? 61.0F : 52.0F, (float) textureY, 9, 9, 256.0F, 256.0F);
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        return true;
    }

    // BNCClientEffectState.getTipsyDamageTicks(), looked up once; 80 (the settled alpha) without it.
    private static int tipsyDamageTicks() {
        if (!tipsyTicksLookedUp) {
            tipsyTicksLookedUp = true;
            try {
                tipsyTicks = MethodHandles.publicLookup().findStatic(
                        Class.forName("xy177.brewinandchewinlegacy.common.network.BNCClientEffectState"),
                        "getTipsyDamageTicks", MethodType.methodType(int.class));
            } catch (ReflectiveOperationException | LinkageError e) {
                tipsyTicks = null;
            }
        }
        if (tipsyTicks == null) return 80;
        try {
            return (int) tipsyTicks.invokeExact();
        } catch (Throwable t) {
            return 80;
        }
    }
}
