package com.trainguy9512.locomotion.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.trainguy9512.locomotion.access.MatrixModelPart;
import com.trainguy9512.locomotion.animation.animator.JointAnimatorDispatcher;
import com.trainguy9512.locomotion.animation.animator.entity.firstperson.*;
import com.trainguy9512.locomotion.animation.animator.entity.firstperson.handpose.FirstPersonGenericItems;
import com.trainguy9512.locomotion.animation.animator.entity.firstperson.handpose.FirstPersonHandPoses;
import com.trainguy9512.locomotion.animation.data.AnimationDataContainer;
import com.trainguy9512.locomotion.animation.joint.JointChannel;
import com.trainguy9512.locomotion.animation.pose.ModelPartSpacePose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Objects;

/**
 * First person renderer, backported to the immediate-mode rendering of Minecraft 1.21.4 (MultiBufferSource instead of the 1.21.9+ submit node system).
 */
public class FirstPersonPlayerRenderer {

    private final Minecraft minecraft;
    private final EntityRenderDispatcher entityRenderDispatcher;
    private final ItemRenderer itemRenderer;
    private final JointAnimatorDispatcher jointAnimatorDispatcher;

    public static boolean IS_RENDERING_LOCOMOTION_FIRST_PERSON = false;
    public static boolean SHOULD_FLIP_ITEM_TRANSFORM = false;
    public static InteractionHand CURRENT_ITEM_INTERACTION_HAND = InteractionHand.MAIN_HAND;
    public static float CURRENT_PARTIAL_TICKS = 0;

    public FirstPersonPlayerRenderer(EntityRendererProvider.Context context) {
        this.minecraft = Minecraft.getInstance();
        this.entityRenderDispatcher = context.getEntityRenderDispatcher();
        this.itemRenderer = context.getItemRenderer();
        this.jointAnimatorDispatcher = JointAnimatorDispatcher.getInstance();
    }

    private PlayerModel getPlayerModel(AbstractClientPlayer player) {
        PlayerRenderer playerRenderer = (PlayerRenderer) this.entityRenderDispatcher.getRenderer(player);
        return playerRenderer.getModel();
    }

    private static ModelPart getArmPart(PlayerModel playerModel, HumanoidArm side) {
        return side == HumanoidArm.LEFT ? playerModel.leftArm : playerModel.rightArm;
    }

    public void renderLocomotionArmWithItem(float partialTick, PoseStack poseStack, MultiBufferSource buffer, AbstractClientPlayer player, int combinedLight, InteractionHand hand) {

        CURRENT_PARTIAL_TICKS = partialTick;
        JointAnimatorDispatcher jointAnimatorDispatcher = JointAnimatorDispatcher.getInstance();

        if (jointAnimatorDispatcher.getFirstPersonPlayerDataContainer().isEmpty()) {
            return;
        }
        if (jointAnimatorDispatcher.getInterpolatedFirstPersonPlayerPose().isEmpty()) {
            return;
        }
        // Getting the arm side
        boolean leftHanded = this.minecraft.options.mainHand().get() == HumanoidArm.LEFT;
        HumanoidArm side = hand == InteractionHand.MAIN_HAND ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
        if (leftHanded) {
            side = side.getOpposite();
        }

        AnimationDataContainer dataContainer = jointAnimatorDispatcher.getFirstPersonPlayerDataContainer().get();
        ModelPartSpacePose pose = jointAnimatorDispatcher.getInterpolatedFirstPersonPlayerPose().get();

        JointChannel armPose = pose.getJointChannel(FirstPersonJointAnimator.getArmJoint(side));
        JointChannel itemPose = pose.getJointChannel(FirstPersonJointAnimator.getItemJoint(side));

        // Posing the player model
        PlayerModel playerModel = this.getPlayerModel(player);
        ModelPart armModelPart = getArmPart(playerModel, side);
        ((MatrixModelPart) (Object) armModelPart).locomotion$setMatrix(armPose.getTransform());

        // Rendering the arm
        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        this.renderArm(player, playerModel, side, poseStack, buffer, combinedLight);

        ResourceLocation genericItemPoseIdentifier = dataContainer.getDriverValue(FirstPersonDrivers.getGenericItemPoseDriver(hand));
        FirstPersonGenericItems.GenericItemPoseDefinition genericItemPoseDefinition = FirstPersonGenericItems.getOrThrowFromIdentifier(genericItemPoseIdentifier);
        ResourceLocation handPoseIdentifier = dataContainer.getDriverValue(FirstPersonDrivers.getHandPoseDriver(hand));
        FirstPersonHandPoses.HandPoseDefinition handPose = FirstPersonHandPoses.getOrThrowFromIdentifier(handPoseIdentifier);
        ItemRenderType itemRenderType = handPoseIdentifier == FirstPersonHandPoses.GENERIC_ITEM ? genericItemPoseDefinition.itemRenderType() : handPose.itemRenderType();

        ItemStack item = getItemStackInHandToRender(dataContainer, player, hand);

        this.renderItem(
                player,
                item,
                poseStack,
                itemPose,
                buffer,
                combinedLight,
                side,
                hand,
                itemRenderType
        );

        poseStack.popPose();
    }

    private static ItemStack getItemStackInHandToRender(AnimationDataContainer dataContainer, AbstractClientPlayer localPlayer, InteractionHand hand) {
        ItemStack driverRenderedItem = dataContainer.getDriverValue(FirstPersonDrivers.getRenderedItemDriver(hand));
        ItemStack playerItem = localPlayer.getItemInHand(hand);

        if (!ItemStack.isSameItem(playerItem, driverRenderedItem)) {
            return driverRenderedItem;
        }
        if (ItemStack.isSameItemSameComponents(playerItem, driverRenderedItem)) {
            return playerItem;
        }
        for (TypedDataComponent<?> dataComponent : playerItem.getComponents()) {
            if (dataComponent.type() == DataComponents.DAMAGE) {
                continue;
            }
            if (!driverRenderedItem.getComponents().has(dataComponent.type())) {
                return driverRenderedItem;
            }
            if (!Objects.equals(driverRenderedItem.get(dataComponent.type()), dataComponent.value())) {
                return driverRenderedItem;
            }
        }
        return playerItem;
    }

    private void renderArm(
            AbstractClientPlayer abstractClientPlayer,
            PlayerModel playerModel,
            HumanoidArm arm,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int combinedLight
    ) {
        PlayerSkin skin = abstractClientPlayer.getSkin();
        boolean isUsingSlimArms = skin.model() == PlayerSkin.Model.SLIM;
        boolean leftArm = arm == HumanoidArm.LEFT;
        ResourceLocation skinTextureLocation = skin.texture();

        // Getting the model parts
        ModelPart armModelPart = getArmPart(playerModel, arm);
        ModelPart sleeveModelPart = leftArm ? playerModel.leftSleeve : playerModel.rightSleeve;
        PlayerModelPart sleevePlayerModelPart = leftArm ? PlayerModelPart.LEFT_SLEEVE : PlayerModelPart.RIGHT_SLEEVE;

        // Translating the slim arm model part over by half a pixel to be in the middle
        poseStack.pushPose();
        if (isUsingSlimArms) {
            poseStack.translate(0.5f / 16f * (leftArm ? 1 : -1), 0, 0);
        }

        // Hiding the sleeve model part based on the player's settings and then rendering both the arm and its sleeve
        sleeveModelPart.visible = abstractClientPlayer.isModelPartShown(sleevePlayerModelPart);
        armModelPart.render(
                poseStack,
                buffer.getBuffer(RenderType.entityTranslucent(skinTextureLocation)),
                combinedLight,
                OverlayTexture.NO_OVERLAY
        );

        poseStack.popPose();
    }

    public void renderItem(
            LivingEntity entity,
            ItemStack itemStack,
            PoseStack poseStack,
            JointChannel jointChannel,
            MultiBufferSource buffer,
            int combinedLight,
            HumanoidArm side,
            InteractionHand hand,
            ItemRenderType renderType
    ) {
        if (!itemStack.isEmpty()) {
            IS_RENDERING_LOCOMOTION_FIRST_PERSON = true;
            CURRENT_ITEM_INTERACTION_HAND = hand;

            poseStack.pushPose();
            jointChannel.transformPoseStack(poseStack, 16f);

            if (renderType.isMirrored() && side == HumanoidArm.LEFT) {
                SHOULD_FLIP_ITEM_TRANSFORM = true;
            }
            switch (renderType) {
                case MAP -> this.renderMap(buffer, poseStack, itemStack, combinedLight);
                case THIRD_PERSON_ITEM, MIRRORED_THIRD_PERSON_ITEM, ON_SHELF -> {
                    ItemDisplayContext displayContext = renderType.getItemDisplayContext(side);
                    this.itemRenderer.renderStatic(entity, itemStack, displayContext, side == HumanoidArm.LEFT, poseStack, buffer, entity.level(), combinedLight, OverlayTexture.NO_OVERLAY, entity.getId() + displayContext.ordinal());
                }
            }
            SHOULD_FLIP_ITEM_TRANSFORM = false;
            IS_RENDERING_LOCOMOTION_FIRST_PERSON = false;
            poseStack.popPose();
        }
    }

    private static final RenderType MAP_BACKGROUND = RenderType.text(ResourceLocation.withDefaultNamespace("textures/map/map_background.png"));
    private static final RenderType MAP_BACKGROUND_CHECKERBOARD = RenderType.text(
            ResourceLocation.withDefaultNamespace("textures/map/map_background_checkerboard.png")
    );

    private void renderMap(MultiBufferSource buffer, PoseStack poseStack, ItemStack itemStack, int combinedLight) {
        MapId mapId = itemStack.get(DataComponents.MAP_ID);
        assert this.minecraft.level != null;
        MapItemSavedData mapItemSavedData = MapItem.getSavedData(mapId, this.minecraft.level);
        RenderType renderType = mapItemSavedData == null ? MAP_BACKGROUND : MAP_BACKGROUND_CHECKERBOARD;

        poseStack.scale(-1, 1, -1);

        poseStack.scale(1f / 16f, 1f / 16f, 1f / 16f);
        poseStack.translate(-2, -4, -1);
        poseStack.scale(1f / 8f, 1f / 8f, 1f / 8f);
        poseStack.scale(1 / 4f, 1 / 4f, 1 / 4f);

        Matrix4f matrix = poseStack.last().pose();
        var vertexConsumer = buffer.getBuffer(renderType);
        vertexConsumer.addVertex(matrix, -7.0F, 135.0F, 0.0F).setColor(-1).setUv(0.0F, 1.0F).setLight(combinedLight);
        vertexConsumer.addVertex(matrix, 135.0F, 135.0F, 0.0F).setColor(-1).setUv(1.0F, 1.0F).setLight(combinedLight);
        vertexConsumer.addVertex(matrix, 135.0F, -7.0F, 0.0F).setColor(-1).setUv(1.0F, 0.0F).setLight(combinedLight);
        vertexConsumer.addVertex(matrix, -7.0F, -7.0F, 0.0F).setColor(-1).setUv(0.0F, 0.0F).setLight(combinedLight);

        if (mapItemSavedData != null && mapId != null) {
            MapRenderer mapRenderer = this.minecraft.gameRenderer.getMapRenderer();
            mapRenderer.render(poseStack, buffer, mapId, mapItemSavedData, false, combinedLight);
        }
    }

    public void transformCamera(PoseStack poseStack) {
        if (this.minecraft.options.getCameraType().isFirstPerson()) {
            this.jointAnimatorDispatcher.getInterpolatedFirstPersonPlayerPose().ifPresent(animationPose -> {
                JointChannel cameraPose = animationPose.getJointChannel(FirstPersonJointAnimator.CAMERA_JOINT);

                Vector3f cameraRot = cameraPose.getEulerRotationZYX();
                cameraRot.z *= -1;
                cameraPose.rotate(cameraRot, JointChannel.TransformSpace.LOCAL, JointChannel.TransformType.REPLACE);
                cameraPose.translate(cameraPose.getTranslation().mul(1, 1, -1), JointChannel.TransformSpace.COMPONENT, JointChannel.TransformType.REPLACE);

                cameraPose.transformPoseStack(poseStack, 16f);
            });
        }
    }
}
