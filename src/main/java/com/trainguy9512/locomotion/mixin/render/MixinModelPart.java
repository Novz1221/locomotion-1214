package com.trainguy9512.locomotion.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.trainguy9512.locomotion.access.MatrixModelPart;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Mixin(ModelPart.class)
public class MixinModelPart implements MatrixModelPart {

    @Shadow
    @Final
    private Map<String, ModelPart> children;

    @Unique
    Matrix4f locomotion$matrix4f = null;

    @Unique
    @Override
    public Function<String, ModelPart> locomotion$createPartLookup() {
        Map<String, ModelPart> lookup = new HashMap<>();
        lookup.put("root", (ModelPart) (Object) this);
        locomotion$collectChildren((ModelPart) (Object) this, lookup);
        return lookup::get;
    }

    @Unique
    private static void locomotion$collectChildren(ModelPart part, Map<String, ModelPart> lookup) {
        Map<String, ModelPart> partChildren = ((MixinModelPart) (Object) part).children;
        partChildren.forEach(lookup::putIfAbsent);
        partChildren.values().forEach(child -> locomotion$collectChildren(child, lookup));
    }

    @Unique
    @Override
    public void locomotion$setMatrix(Matrix4f matrix4f) {
        this.locomotion$matrix4f = matrix4f;
    }

    @Unique
    @Override
    public Matrix4f locomotion$getMatrix() {
        return this.locomotion$matrix4f;
    }

    @Inject(method = "resetPose", at = @At("HEAD"))
    public void resetMatrix(CallbackInfo ci){
        this.locomotion$matrix4f = null;
    }

    @Inject(method = "translateAndRotate", at = @At("HEAD"), cancellable = true)
    public void multiplyPoseStackWithMatrix(PoseStack poseStack, CallbackInfo ci){
        if(this.locomotion$matrix4f != null){
            poseStack.mulPose(this.locomotion$matrix4f.setTranslation(this.locomotion$matrix4f.getTranslation(new Vector3f()).div(16f)));
            ci.cancel();
        }
    }

//    @Inject(method = "copyFrom", at = @At("HEAD"))
//    public void copyMatrixFrom(ModelPart modelPart, CallbackInfo ci){
//        this.locomotion$matrix4f = ((MatrixModelPart)(Object)modelPart).locomotion$getMatrix();
//    }
}
