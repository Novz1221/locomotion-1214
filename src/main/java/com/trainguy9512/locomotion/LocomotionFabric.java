package com.trainguy9512.locomotion;

import com.trainguy9512.locomotion.resource.LocomotionResources;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;

public class LocomotionFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        LocomotionMain.initialize();
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new LocomotionResources());
    }
}
