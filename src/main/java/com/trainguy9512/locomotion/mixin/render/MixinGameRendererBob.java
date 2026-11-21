package com.trainguy9512.locomotion.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.trainguy9512.locomotion.LocomotionMain;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class MixinGameRendererBob {

    /**
     * Remove the vanilla view bobbing, the animation pose provides its own.
     * Signature ditebak sama dengan bobHurt; kalau log bilang "Invalid descriptor", salin "Expected (...)" dari log.
     */
    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void removeViewBobbing(CameraRenderState cameraRenderState, PoseStack poseStack, CallbackInfo ci) {
        if (LocomotionMain.CONFIG.data().firstPersonPlayer.enableRenderer) {
            ci.cancel();
        }
    }
}
