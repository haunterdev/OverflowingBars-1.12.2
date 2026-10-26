package com.xy.overflowingbars.client.handler;

import com.xy.overflowingbars.config.OverflowingBarsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;

public class ArmorBarRenderer {

    // The bar, its row count and the reserved row all read these, so a debug value drives all three.
    // Vanilla clamps generic.armor to 30 and generic.armorToughness to 20; a mod that raises those
    // caps raises what shows here, since this is the value damage is reduced by.
    public static int armorPoints(EntityPlayer player) {
        return OverflowingBarsConfig.debugArmorPoints > 0
                ? OverflowingBarsConfig.debugArmorPoints : player.getTotalArmorValue();
    }

    public static int toughnessPoints(EntityPlayer player) {
        return OverflowingBarsConfig.debugToughnessPoints > 0
                ? OverflowingBarsConfig.debugToughnessPoints
                : MathHelper.floor(player.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
    }

    public static void renderArmorBar(int posX, int posY, EntityPlayer player, boolean unmodified) {
        renderArmorBar(posX, posY, 18, armorPoints(player), true, unmodified, OverflowingBarsConfig.armor);
    }

    public static void renderToughnessBar(int posX, int posY, EntityPlayer player, boolean left, boolean unmodified) {
        renderArmorBar(posX, posY, left ? 9 : 0, toughnessPoints(player), left, unmodified, OverflowingBarsConfig.toughness);
    }

    public static void renderArmorBar(int posX, int posY, int vOffset, int armorPoints, boolean left,
                                      boolean unmodified, OverflowingBarsConfig.ArmorRowConfig config) {
        if (armorPoints <= 0) return;

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        boolean inverse = !unmodified && config.inverseColoring;
        boolean skip = !unmodified && config.skipEmptyArmorPoints;

        int lastRowArmorPoints = 0;
        if (!unmodified && (config.colorizeFirstRow || armorPoints > 20)) {
            lastRowArmorPoints = (armorPoints - 1) % 20 + 1;
        }

        Minecraft mc = Minecraft.getMinecraft();
        mc.getTextureManager().bindTexture(BarOverlayRenderer.OVERFLOWING_ICONS_LOCATION);

        for (int currentArmorPoint = 0; currentArmorPoint < 10; ++currentArmorPoint) {
            int startX = posX + (left ? currentArmorPoint * 8 : -currentArmorPoint * 8 - 9);

            if (currentArmorPoint * 2 + 1 < lastRowArmorPoints) {
                Gui.drawModalRectWithCustomSizedTexture(startX, posY,
                        (float) (inverse ? 18 : 36), (float) vOffset, 9, 9, 256.0F, 256.0F);
                continue;
            }
            if (currentArmorPoint * 2 + 1 == lastRowArmorPoints) {
                if (armorPoints > 20) {
                    Gui.drawModalRectWithCustomSizedTexture(startX, posY,
                            (float) (inverse ? 54 : 27), (float) vOffset, 9, 9, 256.0F, 256.0F);
                    continue;
                }
                Gui.drawModalRectWithCustomSizedTexture(startX, posY,
                        (float) (inverse ? 9 : 45), (float) vOffset, 9, 9, 256.0F, 256.0F);
                continue;
            }
            if (currentArmorPoint * 2 + 1 < armorPoints) {
                Gui.drawModalRectWithCustomSizedTexture(startX, posY,
                        (float) (inverse ? 36 : 18), (float) vOffset, 9, 9, 256.0F, 256.0F);
                continue;
            }
            if (currentArmorPoint * 2 + 1 == armorPoints) {
                Gui.drawModalRectWithCustomSizedTexture(startX, posY,
                        (float) (inverse ? 45 : 9), (float) vOffset, 9, 9, 256.0F, 256.0F);
                continue;
            }
            if (skip || currentArmorPoint * 2 + 1 <= armorPoints) continue;

            Gui.drawModalRectWithCustomSizedTexture(startX, posY,
                    0.0F, (float) vOffset, 9, 9, 256.0F, 256.0F);
        }
    }
}
