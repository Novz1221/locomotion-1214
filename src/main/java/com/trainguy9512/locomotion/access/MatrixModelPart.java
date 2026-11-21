package com.trainguy9512.locomotion.access;

import net.minecraft.client.model.geom.ModelPart;
import org.joml.Matrix4f;
import java.util.function.Function;

public interface MatrixModelPart {
    void locomotion$setMatrix(Matrix4f matrix4f);
    Matrix4f locomotion$getMatrix();
    Function<String, ModelPart> locomotion$createPartLookup();
}
