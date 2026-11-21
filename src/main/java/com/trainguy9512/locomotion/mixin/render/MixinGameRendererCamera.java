package com.trainguy9512.locomotion.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.access.FirstPersonPlayerRendererGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Terpisah dari MixinGameRenderer supaya kalau signature berubah, hanya mixin ini yang gagal.
 * Di 26.3 bobHurt menerima (CameraRenderState, PoseStack) -- sesuai pesan error di log.
 */
@Mixin(GameRenderer.class)
public abstract class MixinGameRendererCamera {

    @Shadow @Final private Minecraft minecraft;

    /**
     * Transform the camera pose stack based on the first person player's camera joint, prior to bobHurt.
     */
    @Inject(method = "bobHurt", at = @At("HEAD"))
    private void addCameraRotation(CameraRenderState cameraRenderState, PoseStack poseStack, CallbackInfo ci) {
        if (LocomotionMain.CONFIG.data().firstPersonPlayer.enableRenderer) {
            ((FirstPersonPlayerRendererGetter) this.minecraft.getEntityRenderDispatcher())
                    .locomotion$getFirstPersonPlayerRenderer()
                    .ifPresent(renderer -> renderer.transformCamera(poseStack));
        }
    }
}
