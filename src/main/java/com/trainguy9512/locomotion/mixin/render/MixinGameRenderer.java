package com.trainguy9512.locomotion.mixin.render;

import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.animator.JointAnimatorDispatcher;
import com.trainguy9512.locomotion.animation.animator.JointAnimatorRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {

    @Shadow @Final private Minecraft minecraft;

    /**
     * Computes and saves the interpolated animation pose prior to rendering.
     */
    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void computePosePriorToRendering(CallbackInfo ci) {
        DeltaTracker deltaTracker = this.minecraft.getDeltaTracker();
        if (LocomotionMain.CONFIG.data().firstPersonPlayer.enableRenderer) {
            JointAnimatorDispatcher jointAnimatorDispatcher = JointAnimatorDispatcher.getInstance();
            jointAnimatorDispatcher.getFirstPersonPlayerDataContainer().ifPresent(dataContainer ->
                    JointAnimatorRegistry.getFirstPersonPlayerJointAnimator().ifPresent(
                            jointAnimator -> jointAnimatorDispatcher.calculateInterpolatedFirstPersonPlayerPose(dataContainer, deltaTracker.getGameTimeDeltaPartialTick(true))
                    ));
        }
    }
}
