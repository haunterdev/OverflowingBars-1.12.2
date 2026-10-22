package com.xy.overflowingbars.client.handler;

import com.xy.overflowingbars.compat.PotionCoreCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

/**
 * Draws Potion Core's damage-resistance outline and magic-shielding glint over our
 * own armor bar. The drawing math mirrors Potion Core's
 * {@code PotionCoreEventHandlerClient} so the overlays look identical to vanilla +
 * Potion Core, but anchored to our compacted bar's position instead of vanilla's.
 *
 * Only the two overlays Potion Core adds that we don't already render are handled
 * here; armor, toughness, and the over-20 overflow are ours. When the player has a
 * relevant effect but no armor, we draw an empty armor row as a backdrop, matching
 * Potion Core's behaviour.
 *
 * @see PotionCoreCompat
 */
public final class PotionCoreArmorRenderer {

    /** Potion Core's own texture sheets; present at runtime whenever Potion Core is loaded. */
    private static final ResourceLocation ARMOR_OVERLAYS =
            new ResourceLocation("potioncore", "textures/gui/armor_overlays.png");
    private static final ResourceLocation ARMOR_MAGIC_SHIELD =
            new ResourceLocation("potioncore", "textures/gui/armor_magic_shield.png");

    private PotionCoreArmorRenderer() {}

    /**
     * Render the resistance + magic-shield overlays at the armor bar origin
     * ({@code posX}, {@code posY}). When {@code drawBaseRow} is true and the player has
     * a relevant effect but no armor icons of our own underneath, an empty armor row is
     * drawn first as a backdrop.
     *
     * @return true if anything was drawn (so the caller can reserve a row of height).
     */
    public static boolean render(int posX, int posY, Minecraft mc, EntityPlayer player, boolean drawBaseRow) {
        if (!PotionCoreCompat.isActive() || player == null) return false;

        double resistance = PotionCoreCompat.getResistancePoints(player);
        double magicShield = PotionCoreCompat.getMagicShielding(player);
        if (resistance <= 0.0 && magicShield <= 0.0) return false;

        GlStateManager.enableBlend();

        if (drawBaseRow) {
            mc.getTextureManager().bindTexture(BarOverlayRenderer.GUI_ICONS_LOCATION);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            for (int i = 0; i < 10; ++i) {
                Gui.drawModalRectWithCustomSizedTexture(posX + i * 8, posY, 16.0F, 9.0F, 9, 9, 256.0F, 256.0F);
            }
        }

        // Damage resistance: a red armor-shaped outline over each armor point.
        if (resistance > 0.0) {
            mc.getTextureManager().bindTexture(ARMOR_OVERLAYS);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            int currentLeft = posX;
            for (int i = 2; i <= 20; i += 2) {
                if ((double) i <= resistance) {
                    Gui.drawModalRectWithCustomSizedTexture(currentLeft - 1, posY - 1, 27.0F, 0.0F, 11, 11, 64.0F, 32.0F);
                } else if (resistance >= (double) (i - 2)) {
                    int partial = MathHelper.floor(resistance % 2.0 * 11.0 / 2.0);
                    Gui.drawModalRectWithCustomSizedTexture(currentLeft - 1, posY - 1, 27.0F, 0.0F, partial, 11, 64.0F, 32.0F);
                    break;
                }
                currentLeft += 8;
            }
        }

        // Magic shielding: an animated enchantment glint sized to the shield value.
        if (magicShield > 0.0) {
            int glintWidth = Math.min(82, MathHelper.floor(magicShield * 4.0 + 2.0));
            if (glintWidth == 82) {
                glintWidth = 83;
            }
            if (glintWidth > 2) {
                mc.getTextureManager().bindTexture(ARMOR_MAGIC_SHIELD);
                drawEnchantmentGlint(posX - 1, posY - 1, -30.0, glintWidth, 11.0);
            }
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        return true;
    }

    /**
     * Ported verbatim from Potion Core's {@code drawEnchantmentGlint}: two scrolling,
     * rotated passes of the glint sheet through the texture matrix.
     */
    private static void drawEnchantmentGlint(double x, double y, double z, double width, double height) {
        float scale = 8.0F;
        GlStateManager.pushMatrix();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.depthMask(false);
        GlStateManager.depthFunc(GL11.GL_GREATER);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
        GlStateManager.matrixMode(GL11.GL_TEXTURE);
        GlStateManager.color(0.95F, 0.75F, 0.95F);

        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        float f = (float) (Minecraft.getSystemTime() % 3000L) / 3000.0F / 8.0F;
        GlStateManager.translate(f, 0.0F, 0.0F);
        GlStateManager.rotate(-50.0F, 0.0F, 0.0F, 1.0F);
        drawGlintRectangle(x, y, z, width, height);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.scale(8.0F, 8.0F, 8.0F);
        float f1 = (float) (Minecraft.getSystemTime() % 4873L) / 4873.0F / 8.0F;
        GlStateManager.translate(-f1, 0.0F, 0.0F);
        GlStateManager.rotate(10.0F, 0.0F, 0.0F, 1.0F);
        drawGlintRectangle(x, y, z, width, height);
        GlStateManager.popMatrix();

        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        GlStateManager.depthMask(true);
        GlStateManager.cullFace(GlStateManager.CullFace.BACK);
        GlStateManager.popMatrix();
    }

    private static void drawGlintRectangle(double x, double y, double z, double width, double height) {
        double uWidth = width / 80.0;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, y + height, z).tex(0.0, 1.0).endVertex();
        buffer.pos(x + width, y + height, z).tex(uWidth, 1.0).endVertex();
        buffer.pos(x + width, y, z).tex(uWidth, 0.0).endVertex();
        buffer.pos(x, y, z).tex(0.0, 0.0).endVertex();
        tessellator.draw();
    }
}
