package com.xy.overflowingbars.client.handler;

import com.xy.overflowingbars.config.OverflowingBarsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class BarOverlayRenderer {

    static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");
    static final ResourceLocation OVERFLOWING_ICONS_LOCATION = new ResourceLocation("overflowingbars", "textures/gui/icons.png");

    public static void renderHealthLevelBars(int screenWidth, int screenHeight, Minecraft mc,
                                             int leftHeight, boolean rowCount) {
        EntityPlayer player = getCameraPlayer(mc);
        if (player == null) return;

        int posX = screenWidth / 2 - 91;
        int posY = screenHeight - leftHeight;

        HealthBarRenderer.INSTANCE.renderPlayerHealth(posX, posY, player);

        if (rowCount) {
            int allHearts = OverflowingBarsConfig.debugHealthPoints > 0
                    ? OverflowingBarsConfig.debugHealthPoints : MathHelper.ceil(player.getHealth());
            RowCountRenderer.drawBarRowCount(posX - 2, posY, allHearts, true, mc.fontRenderer);

            int maxAbsorption = (20 - MathHelper.ceil((float) Math.min(20, allHearts) / 2.0F)) * 2;
            RowCountRenderer.drawBarRowCount(posX - 2, posY - 10,
                    MathHelper.ceil(player.getAbsorptionAmount()), true, maxAbsorption, mc.fontRenderer);
        }
    }

    public static void renderArmorLevelBar(int screenWidth, int screenHeight, Minecraft mc,
                                           int leftHeight, boolean rowCount, boolean unmodified) {
        EntityPlayer player = getCameraPlayer(mc);
        if (player == null) return;

        int posX = screenWidth / 2 - 91;
        int posY = screenHeight - leftHeight;

        ArmorBarRenderer.renderArmorBar(posX, posY, player, unmodified);

        if (rowCount && !unmodified) {
            RowCountRenderer.drawBarRowCount(posX - 2, posY, player.getTotalArmorValue(), true, mc.fontRenderer);
        }
    }

    public static void renderToughnessLevelBar(int screenWidth, int screenHeight, Minecraft mc,
                                               int rightHeight, boolean rowCount, boolean left, boolean unmodified) {
        EntityPlayer player = getCameraPlayer(mc);
        if (player == null) return;

        int posX = screenWidth / 2 + (left ? -91 : 91);
        int posY = screenHeight - rightHeight;

        ArmorBarRenderer.renderToughnessBar(posX, posY, player, left, unmodified);

        if (rowCount && !unmodified) {
            int toughnessValue = MathHelper.floor(
                    player.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
            RowCountRenderer.drawBarRowCount(posX + (left ? -2 : 2), posY, toughnessValue, left, mc.fontRenderer);
        }
    }

    private static EntityPlayer getCameraPlayer(Minecraft mc) {
        Entity entity = mc.getRenderViewEntity();
        return entity instanceof EntityPlayer ? (EntityPlayer) entity : null;
    }

    public static void resetRenderState() {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }
}
