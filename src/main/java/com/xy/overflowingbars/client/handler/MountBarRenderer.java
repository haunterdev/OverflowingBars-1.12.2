package com.xy.overflowingbars.client.handler;

import com.xy.overflowingbars.config.OverflowingBarsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;

/**
 * Stacked overflow rendering for the mount health bar (horse, donkey, pig, etc.).
 *
 * Vanilla {@code GuiIngameForge.renderHealthMount} already wraps mount hearts into
 * extra rows but caps at 30 hearts and shows no overflow indication. This mirrors
 * the player health bar instead: a single compact row using the vanilla mount heart
 * sprites for the front row, the mod's orange heart for overflow, and an optional
 * "Nx" row count for the true total.
 */
public class MountBarRenderer {

    // Vanilla mount heart sprites on textures/gui/icons.png (all on the v = 9 row).
    private static final int MOUNT_V = 9;
    private static final int MOUNT_CONTAINER_U = 52;
    private static final int MOUNT_FULL_U = 88;
    private static final int MOUNT_HALF_U = 97;

    // Gold overflow heart on the mod sheet. Vanilla mount hearts are already orange, so the
    // player bar's orange overflow has no contrast against them; gold reads as a clean escalation.
    private static final int GOLD_V = 45;
    private static final int GOLD_FULL_U = 0;
    private static final int GOLD_HALF_U = 9;

    public static void renderMountHealth(int screenWidth, int screenHeight, Minecraft mc,
                                         int rightHeight, boolean rowCount) {
        EntityLivingBase mount = getMount(mc);
        if (mount == null) return;

        int health = OverflowingBarsConfig.debugMountHealthPoints > 0
                ? OverflowingBarsConfig.debugMountHealthPoints : MathHelper.ceil(mount.getHealth());
        float maxHealth = Math.max(mount.getMaxHealth(), (float) health);
        int normalHearts = Math.min(10, MathHelper.ceil((double) maxHealth / 2.0));
        if (normalHearts <= 0) return;

        int rightAlign = screenWidth / 2 + 91;
        int posY = screenHeight - rightHeight;
        boolean colorize = OverflowingBarsConfig.mount.colorizeFirstRow;
        int filled = Math.min(20, health);
        // Row of the overflow color: hearts whose value falls in the current (top) row.
        int lastRow = (health - 1) % 20 + 1;

        // Pass 1 — vanilla sheet: containers + red front-row hearts.
        mc.getTextureManager().bindTexture(BarOverlayRenderer.GUI_ICONS_LOCATION);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        for (int i = 0; i < normalHearts; ++i) {
            int x = rightAlign - i * 8 - 9;
            Gui.drawModalRectWithCustomSizedTexture(x, posY, MOUNT_CONTAINER_U, MOUNT_V, 9, 9, 256.0F, 256.0F);
        }
        for (int i = 0; i < normalHearts; ++i) {
            if (i * 2 >= filled) continue;
            int x = rightAlign - i * 8 - 9;
            boolean half = i * 2 + 1 == lastRow;
            boolean orange = (health > 20 || colorize) && i * 2 + 1 <= lastRow;
            if (orange) {
                // Draw a red full heart under an orange half so the split heart shows both.
                if (half) {
                    Gui.drawModalRectWithCustomSizedTexture(x, posY, MOUNT_FULL_U, MOUNT_V, 9, 9, 256.0F, 256.0F);
                }
            } else {
                Gui.drawModalRectWithCustomSizedTexture(x, posY,
                        half ? MOUNT_HALF_U : MOUNT_FULL_U, MOUNT_V, 9, 9, 256.0F, 256.0F);
            }
        }

        // Pass 2 — mod sheet: gold overflow hearts on top.
        mc.getTextureManager().bindTexture(BarOverlayRenderer.OVERFLOWING_ICONS_LOCATION);
        for (int i = 0; i < normalHearts; ++i) {
            if (i * 2 >= filled) continue;
            boolean overflow = (health > 20 || colorize) && i * 2 + 1 <= lastRow;
            if (!overflow) continue;
            int x = rightAlign - i * 8 - 9;
            boolean half = i * 2 + 1 == lastRow;
            Gui.drawModalRectWithCustomSizedTexture(x, posY,
                    half ? GOLD_HALF_U : GOLD_FULL_U, GOLD_V, 9, 9, 256.0F, 256.0F);
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        if (rowCount) {
            RowCountRenderer.drawBarRowCount(rightAlign + 2, posY, health, false, mc.fontRenderer);
        }
    }

    private static EntityLivingBase getMount(Minecraft mc) {
        Entity view = mc.getRenderViewEntity();
        if (!(view instanceof EntityPlayer)) return null;
        Entity riding = view.getRidingEntity();
        return riding instanceof EntityLivingBase ? (EntityLivingBase) riding : null;
    }
}
