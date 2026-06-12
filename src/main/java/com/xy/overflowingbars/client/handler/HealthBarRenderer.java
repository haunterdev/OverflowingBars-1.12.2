package com.xy.overflowingbars.client.handler;

import com.xy.overflowingbars.config.OverflowingBarsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import java.util.Random;

public class HealthBarRenderer {

    public static final HealthBarRenderer INSTANCE = new HealthBarRenderer();

    private final Random random = new Random();
    private int tickCount;
    private int lastHealth;
    private int displayHealth;
    private long lastHealthTime;
    private long healthBlinkTime;

    public void onStartTick(Minecraft minecraft) {
        ++this.tickCount;
    }

    public void renderPlayerHealth(int posX, int posY, EntityPlayer player) {
        BarOverlayRenderer.resetRenderState();
        GlStateManager.enableBlend();

        int currentHealth = OverflowingBarsConfig.debugHealthPoints > 0
                ? OverflowingBarsConfig.debugHealthPoints : MathHelper.ceil(player.getHealth());
        boolean blink = this.healthBlinkTime > (long) this.tickCount
                && (this.healthBlinkTime - (long) this.tickCount) / 3L % 2L == 1L;

        long millis = Minecraft.getSystemTime();

        if (currentHealth < this.lastHealth && player.hurtResistantTime > 0) {
            this.lastHealthTime = millis;
            this.healthBlinkTime = this.tickCount + 20;
        } else if (currentHealth > this.lastHealth && player.hurtResistantTime > 0) {
            this.lastHealthTime = millis;
            this.healthBlinkTime = this.tickCount + 10;
        }

        if (millis - this.lastHealthTime > 1000L) {
            this.displayHealth = currentHealth;
            this.lastHealthTime = millis;
        }

        this.lastHealth = currentHealth;
        int dHealth = this.displayHealth;
        this.random.setSeed((long) (this.tickCount * 312871));

        float maxHealth = Math.max(
                (float) player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getAttributeValue(),
                (float) Math.max(dHealth, currentHealth));
        int currentAbsorption = MathHelper.ceil(player.getAbsorptionAmount());

        int heartOffsetByRegen = -1;
        if (player.isPotionActive(MobEffects.REGENERATION)) {
            heartOffsetByRegen = this.tickCount % MathHelper.ceil(Math.min(20.0F, maxHealth) + 5.0F);
        }

        this.renderHearts(player, posX, posY, heartOffsetByRegen, maxHealth, currentHealth, dHealth, currentAbsorption, blink);

        GlStateManager.disableBlend();
    }

    private void renderHearts(EntityPlayer player, int posX, int posY, int heartOffsetByRegen,
                              float maxHealth, int currentHealth, int displayHealth,
                              int currentAbsorptionHealth, boolean blink) {

        boolean hardcore = player.world.getWorldInfo().isHardcoreModeEnabled();
        int normalHearts = Math.min(10, MathHelper.ceil((double) maxHealth / 2.0));
        int maxAbsorptionHearts = 20 - normalHearts;
        int absorptionHearts = Math.min(20 - normalHearts, MathHelper.ceil((double) currentAbsorptionHealth / 2.0));

        for (int currentHeart = 0; currentHeart < normalHearts + absorptionHearts; ++currentHeart) {
            boolean orange;
            boolean halfHeart;
            int currentAbsorption;

            int currentPosX = posX + currentHeart % 10 * 8;
            int currentPosY = posY - currentHeart / 10 * 10;

            if (currentHealth + currentAbsorptionHealth <= 4) {
                currentPosY += this.random.nextInt(2);
            }

            if (currentHeart < normalHearts && heartOffsetByRegen == currentHeart) {
                currentPosY -= 2;
            }

            GlStateManager.pushMatrix();
            this.renderHeart(HeartType.CONTAINER, currentPosX, currentPosY, blink, false, hardcore);

            if (currentHeart >= normalHearts
                    && (currentAbsorption = currentHeart * 2 - normalHearts * 2) < currentAbsorptionHealth) {
                int maxAbsorptionHealth = maxAbsorptionHearts * 2;
                boolean halfHeart2 = currentAbsorption + 1 == currentAbsorptionHealth % maxAbsorptionHealth;
                boolean orange2 = currentAbsorptionHealth > maxAbsorptionHealth
                        && currentAbsorption + 1 <= (currentAbsorptionHealth - 1) % maxAbsorptionHealth + 1;
                if (halfHeart2 && orange2) {
                    this.renderHeart(HeartType.forPlayer(player, true, false), currentPosX, currentPosY, false, false, hardcore);
                }
                this.renderHeart(HeartType.forPlayer(player, true, orange2), currentPosX, currentPosY, false, halfHeart2, hardcore);
            }

            if (blink && currentHeart * 2 < Math.min(20, displayHealth)) {
                halfHeart = currentHeart * 2 + 1 == (displayHealth - 1) % 20 + 1;
                orange = displayHealth > 20 && currentHeart * 2 + 1 <= (displayHealth - 1) % 20 + 1;
                if (halfHeart && orange) {
                    this.renderHeart(HeartType.forPlayer(player, false, false), currentPosX, currentPosY, true, false, hardcore);
                }
                this.renderHeart(HeartType.forPlayer(player, false,
                        orange || OverflowingBarsConfig.health.colorizeFirstRow
                                && currentHeart * 2 + 1 <= (displayHealth - 1) % 20 + 1),
                        currentPosX, currentPosY, true, halfHeart, hardcore);
            }

            if (currentHeart * 2 < Math.min(20, currentHealth)) {
                halfHeart = currentHeart * 2 + 1 == (currentHealth - 1) % 20 + 1;
                orange = currentHealth > 20 && currentHeart * 2 + 1 <= (currentHealth - 1) % 20 + 1;
                if (halfHeart && orange) {
                    this.renderHeart(HeartType.forPlayer(player, false, false), currentPosX, currentPosY, false, false, hardcore);
                }
                this.renderHeart(HeartType.forPlayer(player, false,
                        orange || OverflowingBarsConfig.health.colorizeFirstRow
                                && currentHeart * 2 + 1 <= (currentHealth - 1) % 20 + 1),
                        currentPosX, currentPosY, false, halfHeart, hardcore);
            }

            GlStateManager.popMatrix();
        }
    }

    private void renderHeart(HeartType heartType, int posX, int posY, boolean blink, boolean halfHeart, boolean hardcore) {
        GlStateManager.translate(0.0F, 0.0F, 0.03F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(heartType.textureSheet);
        Gui.drawModalRectWithCustomSizedTexture(posX, posY,
                (float) heartType.getX(halfHeart, blink), (float) heartType.getY(hardcore),
                9, 9, 256.0F, 256.0F);
    }

    enum HeartType {
        CONTAINER(0, false),
        NORMAL(2, true),
        POISONED(4, true),
        WITHERED(6, true),
        ABSORBING(8, false),
        ORANGE(0, 3, 4, BarOverlayRenderer.OVERFLOWING_ICONS_LOCATION, true);

        private final int textureIndexX;
        private final int textureIndexY;
        private final int hardcoreIndexY;
        public final ResourceLocation textureSheet;
        private final boolean canBlink;

        HeartType(int textureIndexX, boolean blink) {
            this(textureIndexX, 0, 5, BarOverlayRenderer.GUI_ICONS_LOCATION, blink);
        }

        HeartType(int textureIndexX, int textureIndexY, int hardcoreIndexY, ResourceLocation textureSheet, boolean blink) {
            this.textureIndexX = textureIndexX;
            this.textureIndexY = textureIndexY;
            this.hardcoreIndexY = hardcoreIndexY;
            this.textureSheet = textureSheet;
            this.canBlink = blink;
        }

        public int getX(boolean halfHeart, boolean blink) {
            int i;
            if (this == CONTAINER) {
                i = blink ? 1 : 0;
            } else {
                int j = halfHeart ? 1 : 0;
                int k = this.canBlink && blink ? 2 : 0;
                i = j + k;
            }
            return (this == ORANGE ? 0 : 16) + (this.textureIndexX * 2 + i) * 9;
        }

        public int getY(boolean hardcore) {
            return (hardcore ? this.hardcoreIndexY : this.textureIndexY) * 9;
        }

        public static HeartType forPlayer(EntityPlayer player, boolean absorbing, boolean orange) {
            if (player.isPotionActive(MobEffects.WITHER)) return WITHERED;
            if (player.isPotionActive(MobEffects.POISON)) return POISONED;
            boolean inverse = OverflowingBarsConfig.health.inverseColoring;
            if (orange) return absorbing || !inverse ? ORANGE : NORMAL;
            return absorbing ? ABSORBING : (inverse ? ORANGE : NORMAL);
        }
    }
}
