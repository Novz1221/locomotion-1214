package com.trainguy9512.locomotion.util;

import net.minecraft.world.item.ItemUseAnimation;

public class LocomotionMultiVersionWrappers {

    public static ItemUseAnimation getTridentUseAnimation() {
        return ItemUseAnimation.SPEAR;
    }

    public static ItemUseAnimation getSpearUseAnimation() {
        throw new RuntimeException("1.21.11 feature attempted to be used in older version");
    }

}
