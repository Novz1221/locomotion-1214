package com.trainguy9512.locomotion.util;

import net.minecraft.world.item.UseAnim;

public class LocomotionMultiVersionWrappers {

    public static UseAnim getTridentUseAnimation() {
        return UseAnim.SPEAR;
    }

    public static UseAnim getSpearUseAnimation() {
        throw new RuntimeException("1.21.11 feature attempted to be used in older version");
    }

}
