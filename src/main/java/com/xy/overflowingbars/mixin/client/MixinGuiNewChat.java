package com.xy.overflowingbars.mixin.client;

import com.xy.overflowingbars.client.helper.ChatOffsetHelper;
import com.xy.overflowingbars.config.OverflowingBarsConfig;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Shifts the chat box (and its click mapping) upward so it clears the taller
 * stacked health/armor/absorption bars rendered by this mod. Mirrors the
 * original mod's ChatComponentMixin behaviour, adapted to the 1.12.2 GuiNewChat
 * (which has no {@code screenToChatY} — we wrap {@code drawChat} for the visual
 * shift and adjust {@code getChatComponent}'s mouse Y for click accuracy).
 */
@Mixin(GuiNewChat.class)
abstract class MixinGuiNewChat {

    @Inject(method = "drawChat", at = @At("HEAD"))
    private void overflowingbars$pushChatOffset(int updateCounter, CallbackInfo ci) {
        if (!OverflowingBarsConfig.moveChatAboveArmor) {
            return;
        }
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, -ChatOffsetHelper.getChatOffsetY(), 0.0F);
    }

    @Inject(method = "drawChat", at = @At("RETURN"))
    private void overflowingbars$popChatOffset(int updateCounter, CallbackInfo ci) {
        if (!OverflowingBarsConfig.moveChatAboveArmor) {
            return;
        }
        GlStateManager.popMatrix();
    }

    @ModifyVariable(method = "getChatComponent", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private int overflowingbars$offsetClickY(int mouseY) {
        if (!OverflowingBarsConfig.moveChatAboveArmor) {
            return mouseY;
        }
        return mouseY + ChatOffsetHelper.getChatOffsetY();
    }
}
