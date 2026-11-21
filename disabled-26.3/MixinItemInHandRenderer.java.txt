package com.trainguy9512.locomotion.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.access.FirstPersonPlayerRendererGetter;
import com.trainguy9512.locomotion.render.FirstPersonPlayerRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class MixinItemInHandRenderer {

    @Shadow @Final private Minecraft minecraft;

    @Inject(
            method = "renderHandsWithItems",
            at = @At("HEAD")
    )
    public void locomotion$overrideFirstPersonRendering(
            float partialTick, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, LocalPlayer player, int packedLight, CallbackInfo ci
    ) {
        if (!LocomotionMain.CONFIG.data().firstPersonPlayer.enableRenderer) {
            return;
        }
        var getter = (FirstPersonPlayerRendererGetter) this.minecraft.getEntityRenderDispatcher();
        if (getter.locomotion$getFirstPersonPlayerRenderer().isEmpty()) {
            return;
        }
        FirstPersonPlayerRenderer renderer = getter.locomotion$getFirstPersonPlayerRenderer().get();
        renderer.renderLocomotionArmWithItem(partialTick, poseStack, bufferSource, player, packedLight, InteractionHand.OFF_HAND);
        renderer.renderLocomotionArmWithItem(partialTick, poseStack, bufferSource, player, packedLight, InteractionHand.MAIN_HAND);
    }

    // Disables camera bob rotation if Locomotion's first person animations are enabled.
    @Redirect(
            method = "renderHandsWithItems",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V")
    )
    public void removeVanillaCameraBob(PoseStack instance, Quaternionf pose) {
        if (!LocomotionMain.CONFIG.data().firstPersonPlayer.enableRenderer) {
            instance.mulPose(pose);
        }
    }

    // Cancel rendering the vanilla hand with item if Locomotion's first person animations are enabled.
    @Inject(
            method = "renderArmWithItem",
            at = @At("HEAD"),
            cancellable = true
    )
    public void locomotion$renderLocomotionArmsWithItems(
            AbstractClientPlayer player, float partialTick, float pitch, InteractionHand hand, float swingProgress, ItemStack item, float equippedProgress, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo ci
    ) {
        if (LocomotionMain.CONFIG.data().firstPersonPlayer.enableRenderer) {
            ci.cancel();
        }
    }
}
