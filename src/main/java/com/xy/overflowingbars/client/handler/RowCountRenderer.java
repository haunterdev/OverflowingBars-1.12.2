package com.xy.overflowingbars.client.handler;

import com.xy.overflowingbars.config.OverflowingBarsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class RowCountRenderer {

    private static final ResourceLocation TINY_NUMBERS_LOCATION = new ResourceLocation("overflowingbars", "textures/font/tiny_numbers.png");

    public static void drawBarRowCount(int posX, int posY, int barValue, boolean left, FontRenderer font) {
        drawBarRowCount(posX, posY, barValue, left, 20, font);
    }

    public static void drawBarRowCount(int posX, int posY, int barValue, boolean left, int maxRowCount, FontRenderer font) {
        if (barValue <= 0 || maxRowCount <= 0) return;

        float rowCount = (float) barValue / (float) maxRowCount;
        if (!OverflowingBarsConfig.rowCount.alwaysRenderRowCount && rowCount <= 1.0F) return;

        int numberValue = OverflowingBarsConfig.rowCount.countFullRowsOnly
                ? MathHelper.floor(rowCount)
                : MathHelper.ceil(rowCount);

        int textColor = OverflowingBarsConfig.rowCount.rowCountColor;

        if (OverflowingBarsConfig.rowCount.forceFontRenderer) {
            String text = String.valueOf(numberValue);
            if (OverflowingBarsConfig.rowCount.rowCountX) text = text + "x";
            if (left) posX -= font.getStringWidth(text);
            drawBorderedText(posX, posY + 1, text, textColor, 255, font);
            // Font rendering dirties GL color in 1.12.2; reset so it doesn't tint later HUD elements.
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        } else {
            // Build digit array manually (Java 8 has no 3-arg IntStream.iterate)
            List<Integer> ds = new ArrayList<>();
            for (int n = numberValue; n > 0; n /= 10) {
                ds.add(n % 10);
            }
            int[] numberDigits = new int[ds.size()];
            for (int i = 0; i < ds.size(); i++) {
                numberDigits[i] = ds.get(i);
            }

            float red = (textColor >> 16 & 0xFF) / 255.0F;
            float green = (textColor >> 8 & 0xFF) / 255.0F;
            float blue = (textColor & 0xFF) / 255.0F;

            posX = left
                    ? (posX - (OverflowingBarsConfig.rowCount.rowCountX ? 7 : 3))
                    : (posX + 4 * numberDigits.length);

            // Bind the tiny numbers texture before drawing sprites
            Minecraft.getMinecraft().getTextureManager().bindTexture(TINY_NUMBERS_LOCATION);

            for (int i = 0; i < numberDigits.length; ++i) {
                drawBorderedSprite(3, 5, posX - 4 * i, posY + 2, 5 * numberDigits[i], 0, red, green, blue, 1.0F);
            }

            if (OverflowingBarsConfig.rowCount.rowCountX) {
                drawBorderedSprite(3, 5, posX + 4, posY + 2, 0, 7, red, green, blue, 1.0F);
            }
        }
    }

    private static void drawBorderedSprite(int width, int height, int posX, int posY,
                                           int textureX, int textureY,
                                           float red, float green, float blue, float alpha) {
        // Black border via 4 offset draws
        GlStateManager.color(0.0F, 0.0F, 0.0F, alpha);
        Gui.drawModalRectWithCustomSizedTexture(posX - 1, posY, (float) textureX, (float) textureY, width, height, 256.0F, 256.0F);
        Gui.drawModalRectWithCustomSizedTexture(posX + 1, posY, (float) textureX, (float) textureY, width, height, 256.0F, 256.0F);
        Gui.drawModalRectWithCustomSizedTexture(posX, posY - 1, (float) textureX, (float) textureY, width, height, 256.0F, 256.0F);
        Gui.drawModalRectWithCustomSizedTexture(posX, posY + 1, (float) textureX, (float) textureY, width, height, 256.0F, 256.0F);

        // Colored center
        GlStateManager.color(red, green, blue, alpha);
        Gui.drawModalRectWithCustomSizedTexture(posX, posY, (float) textureX, (float) textureY, width, height, 256.0F, 256.0F);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void drawBorderedText(int posX, int posY, String text, int color, int alpha, FontRenderer font) {
        // Black border via 4 offset draws
        int black = alpha << 24;
        font.drawString(text, posX - 1, posY, black);
        font.drawString(text, posX + 1, posY, black);
        font.drawString(text, posX, posY - 1, black);
        font.drawString(text, posX, posY + 1, black);

        // Colored center
        font.drawString(text, posX, posY, (color & 0xFFFFFF) | alpha << 24);
    }
}
