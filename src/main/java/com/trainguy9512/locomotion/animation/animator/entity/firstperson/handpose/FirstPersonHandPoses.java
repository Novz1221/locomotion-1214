package com.trainguy9512.locomotion.animation.animator.entity.firstperson.handpose;

import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.animator.entity.firstperson.FirstPersonAnimationSequences;
import com.trainguy9512.locomotion.animation.animator.entity.firstperson.FirstPersonMining;
import com.trainguy9512.locomotion.animation.data.DriverGetter;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.data.PoseCalculationContext;
import com.trainguy9512.locomotion.animation.pose.function.PoseFunction;
import com.trainguy9512.locomotion.animation.pose.function.SequenceEvaluatorFunction;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.render.ItemRenderType;
import com.trainguy9512.locomotion.animation.util.Easing;
import com.trainguy9512.locomotion.util.LocomotionMultiVersionWrappers;
import com.trainguy9512.locomotion.animation.util.TimeSpan;
import com.trainguy9512.locomotion.animation.util.Transition;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ShieldItem;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class FirstPersonHandPoses {

    private static final Map<ResourceLocation, HandPoseDefinition> HAND_POSES_BY_IDENTIFIER = new HashMap<>();

    public static ResourceLocation register(ResourceLocation identifier, HandPoseDefinition configuration) {
        HAND_POSES_BY_IDENTIFIER.put(identifier, configuration);
        return identifier;
    }

    public static final ResourceLocation EMPTY_MAIN_HAND = register(LocomotionMain.makeIdentifier("empty_main_hand"), HandPoseDefinition.builder(
            "empty_main_hand",
            HandPoseFunctionSupplier::constructOnlyWithMiningAnimation,
            FirstPersonAnimationSequences.HAND_EMPTY_POSE,
            ItemStack::isEmpty,
            10)
            .setHandsToUsePoseIn(InteractionHand.MAIN_HAND)
            .setRaiseSequence(FirstPersonAnimationSequences.HAND_EMPTY_RAISE)
            .setLowerSequence(FirstPersonAnimationSequences.HAND_EMPTY_LOWER)
            .setMiningPoseFunctionSuppler(FirstPersonMining::constructEmptyHandMiningPoseFunction)
            .build());

    public static final ResourceLocation EMPTY_OFF_HAND = register(LocomotionMain.makeIdentifier("empty_off_hand"), HandPoseDefinition.builder(
            "empty_off_hand",
            HandPoseFunctionSupplier::constructOnlyWithMiningAnimation,
            FirstPersonAnimationSequences.HAND_EMPTY_LOWERED,
            ItemStack::isEmpty,
            10)
            .setRaiseSequence(FirstPersonAnimationSequences.HAND_EMPTY_LOWERED)
            .setLowerSequence(FirstPersonAnimationSequences.HAND_EMPTY_LOWERED)
            .setHandsToUsePoseIn(InteractionHand.OFF_HAND)
            .build());
    public static final ResourceLocation GENERIC_ITEM = register(LocomotionMain.makeIdentifier("generic_item"), HandPoseDefinition.builder(
            "generic_item",
            FirstPersonGenericItems::constructPoseFunction,
            FirstPersonGenericItems::getCurrentBasePose,
            itemStack -> true,
            0)
            .setMiningPoseFunctionSuppler(FirstPersonMining::constructEmptyHandMiningPoseFunction)
            .build());
    public static final ResourceLocation PICKAXE = register(LocomotionMain.makeIdentifier("pickaxe"), HandPoseDefinition.builder(
            "pickaxe",
            HandPoseFunctionSupplier::constructOnlyWithMiningAnimation,
            FirstPersonAnimationSequences.HAND_TOOL_POSE,
            itemStack -> itemStack.is(ItemTags.PICKAXES),
            60)
            .setRaiseSequence(FirstPersonAnimationSequences.HAND_TOOL_RAISE)
            .setLowerSequence(FirstPersonAnimationSequences.HAND_TOOL_LOWER)
            .build());
    public static final ResourceLocation AXE = register(LocomotionMain.makeIdentifier("axe"), HandPoseDefinition.builder(
            "axe",
            HandPoseFunctionSupplier::constructOnlyWithMiningAnimation,
            FirstPersonAnimationSequences.HAND_TOOL_POSE,
            itemStack -> itemStack.is(ItemTags.AXES),
            50)
            .setRaiseSequence(FirstPersonAnimationSequences.HAND_TOOL_RAISE)
            .setLowerSequence(FirstPersonAnimationSequences.HAND_TOOL_LOWER)
            .setMiningPoseFunctionSuppler(FirstPersonMining::constructAxeMiningPoseFunction)
            .build());
    public static final ResourceLocation SHOVEL = register(LocomotionMain.makeIdentifier("shovel"), HandPoseDefinition.builder(
            "shovel",
            HandPoseFunctionSupplier::constructOnlyWithMiningAnimation,
            FirstPersonAnimationSequences.HAND_TOOL_POSE,
            itemStack -> itemStack.is(ItemTags.SHOVELS),
            40)
            .setRaiseSequence(FirstPersonAnimationSequences.HAND_TOOL_RAISE)
            .setLowerSequence(FirstPersonAnimationSequences.HAND_TOOL_LOWER)
            .setMiningPoseFunctionSuppler(FirstPersonMining::constructShovelMiningPoseFunction)
            .build());
    public static final ResourceLocation HOE = register(LocomotionMain.makeIdentifier("hoe"), HandPoseDefinition.builder(
            "hoe",
            HandPoseFunctionSupplier::constructOnlyWithMiningAnimation,
            FirstPersonAnimationSequences.HAND_TOOL_POSE,
            itemStack -> itemStack.is(ItemTags.HOES),
            40)
            .setRaiseSequence(FirstPersonAnimationSequences.HAND_TOOL_RAISE)
            .setLowerSequence(FirstPersonAnimationSequences.HAND_TOOL_LOWER)
            .build());
    public static final ResourceLocation SWORD = register(LocomotionMain.makeIdentifier("sword"), HandPoseDefinition.builder(
            "sword",
            FirstPersonSword::handSwordPoseFunction,
            FirstPersonAnimationSequences.HAND_TOOL_POSE,
            itemStack -> itemStack.is(ItemTags.SWORDS),
            100)
            .setRaiseSequence(FirstPersonAnimationSequences.HAND_TOOL_SWORD_RAISE)
            .setLowerSequence(FirstPersonAnimationSequences.HAND_TOOL_LOWER)
            .build());
    public static final ResourceLocation SHIELD = register(LocomotionMain.makeIdentifier("shield"), HandPoseDefinition.builder(
            "shield",
            FirstPersonShield::constructShieldPoseFunction,
            FirstPersonAnimationSequences.HAND_SHIELD_POSE,
            itemStack -> itemStack.getItem() instanceof ShieldItem,
            90)
            .setRaiseSequence(FirstPersonAnimationSequences.HAND_TOOL_RAISE)
            .setLowerSequence(FirstPersonAnimationSequences.HAND_TOOL_LOWER)
            .setMiningPoseFunctionSuppler(() -> FirstPersonMining.constructPickaxeMiningPoseFunction(SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_SHIELD_POSE).build()))
            .build());
    public static final ResourceLocation BOW = register(LocomotionMain.makeIdentifier("bow"), HandPoseDefinition.builder(
            "bow",
            HandPoseFunctionSupplier::constructOnlyWithMiningAnimation,
            FirstPersonAnimationSequences.HAND_BOW_POSE,
            itemStack -> itemStack.getUseAnimation() == ItemUseAnimation.BOW,
            100)
            .setRaiseSequence(FirstPersonAnimationSequences.HAND_TOOL_RAISE)
            .setLowerSequence(FirstPersonAnimationSequences.HAND_TOOL_LOWER)
            .build());
    public static final ResourceLocation CROSSBOW = register(LocomotionMain.makeIdentifier("crossbow"), HandPoseDefinition.builder(
            "crossbow",
            HandPoseFunctionSupplier::constructOnlyWithMiningAnimation,
            FirstPersonAnimationSequences.HAND_CROSSBOW_POSE,
            itemStack -> itemStack.getUseAnimation() == ItemUseAnimation.CROSSBOW,
            100)
            .setRaiseSequence(FirstPersonAnimationSequences.HAND_CROSSBOW_RAISE)
            .setLowerSequence(FirstPersonAnimationSequences.HAND_TOOL_LOWER)
            .build());
    public static final ResourceLocation TRIDENT = register(LocomotionMain.makeIdentifier("trident"), HandPoseDefinition.builder(
            "trident",
            FirstPersonTrident::handTridentPoseFunction,
            FirstPersonAnimationSequences.HAND_TRIDENT_POSE,
            itemStack -> itemStack.getUseAnimation() == LocomotionMultiVersionWrappers.getTridentUseAnimation(),
            100)
            .setRaiseSequence(FirstPersonAnimationSequences.HAND_SPEAR_RAISE)
            .setLowerSequence(FirstPersonAnimationSequences.HAND_SPEAR_LOWER)
            .build());
    public static final ResourceLocation BRUSH = register(LocomotionMain.makeIdentifier("brush"), HandPoseDefinition.builder(
            "brush",
            FirstPersonBrush::constructBrushPoseFunction,
            FirstPersonAnimationSequences.HAND_BRUSH_POSE,
            itemStack -> itemStack.getUseAnimation() == ItemUseAnimation.BRUSH,
            100)
            .setRaiseSequence(FirstPersonAnimationSequences.HAND_TOOL_RAISE)
            .setLowerSequence(FirstPersonAnimationSequences.HAND_TOOL_LOWER)
            .build());
    public static final ResourceLocation MACE = register(LocomotionMain.makeIdentifier("mace"), HandPoseDefinition.builder(
            "mace",
            FirstPersonMace::handMacePoseFunction,
            FirstPersonAnimationSequences.HAND_MACE_POSE,
            itemStack -> itemStack.is(ItemTags.MACE_ENCHANTABLE),
            110)
            .setRaiseSequence(FirstPersonAnimationSequences.HAND_TOOL_RAISE)
            .setLowerSequence(FirstPersonAnimationSequences.HAND_TOOL_LOWER)
            .build());
    public static final ResourceLocation SPYGLASS = register(LocomotionMain.makeIdentifier("spyglass"), HandPoseDefinition.builder(
            "spyglass",
            FirstPersonSpyglass::handSpyglassPoseFunction,
            FirstPersonAnimationSequences.HAND_SPYGLASS_POSE,
            itemStack -> itemStack.getUseAnimation() == ItemUseAnimation.SPYGLASS,
            100)
            .setRaiseSequence(FirstPersonAnimationSequences.HAND_TOOL_RAISE)
            .setLowerSequence(FirstPersonAnimationSequences.HAND_TOOL_LOWER)
            .build());
    public static final ResourceLocation MAP = register(LocomotionMain.makeIdentifier("map"), HandPoseDefinition.builder(
            "map",
            HandPoseFunctionSupplier::constructOnlyWithMiningAnimation,
            FirstPersonAnimationSequences.HAND_MAP_SINGLE_HAND_POSE,
            itemStack -> itemStack.has(DataComponents.MAP_ID),
            100)
            .setRaiseSequence(FirstPersonAnimationSequences.HAND_TOOL_RAISE)
            .setLowerSequence(FirstPersonAnimationSequences.HAND_TOOL_LOWER)
            .setItemRenderType(ItemRenderType.MAP)
            .build());

    public static ResourceLocation getFallback() {
        return GENERIC_ITEM;
    }

    public static ResourceLocation getEmptyMainHand() {
        return EMPTY_MAIN_HAND;
    }

    public static ResourceLocation getEmptyOffHand() {
        return EMPTY_OFF_HAND;
    }

    public static ResourceLocation getEmptyHandPose(InteractionHand hand) {
        return switch (hand) {
            case MAIN_HAND -> getEmptyMainHand();
            case OFF_HAND -> getEmptyOffHand();
        };
    }

    public record HandPoseDefinition(
            String stateIdentifier,
            Predicate<ItemStack> choosePoseIfTrue,
            int evaluationPriority,
            HandPoseFunctionSupplier poseFunctionSupplier,
            Supplier<PoseFunction<LocalSpacePose>> miningPoseFunctionSupplier,
            BiFunction<DriverGetter, InteractionHand, ResourceLocation> currentBasePoseSupplier,
            ResourceLocation raiseSequence,
            ResourceLocation lowerSequence,
            Transition raiseToPoseTransition,
            Transition poseToLowerTransition,
            ItemRenderType itemRenderType,
            InteractionHand[] handsToUsePoseIn
    ) {

        public String getRaiseStateIdentifier() {
            return this.stateIdentifier + "_raise";
        }

        public String getLowerStateIdentifier() {
            return this.stateIdentifier + "_lower";
        }

        public PoseFunction<LocalSpacePose> constructBasePoseFunction(InteractionHand hand) {
            return SequenceEvaluatorFunction.builder(context -> this.currentBasePoseSupplier().apply(context, hand)).build();
        }

        public static Builder builder(
                String stateIdentifier,
                HandPoseFunctionSupplier poseFunctionSupplier,
                BiFunction<DriverGetter, InteractionHand, ResourceLocation> basePoseSequence,
                Predicate<ItemStack> choosePoseIfTrue,
                int chooseEvaluationPriority
        ) {
            return new Builder(stateIdentifier, poseFunctionSupplier, basePoseSequence, choosePoseIfTrue, chooseEvaluationPriority);
        }

        public static Builder builder(
                String stateIdentifier,
                HandPoseFunctionSupplier poseFunctionSupplier,
                ResourceLocation basePoseSequence,
                Predicate<ItemStack> choosePoseIfTrue,
                int chooseEvaluationPriority
        ) {
            return new Builder(stateIdentifier, poseFunctionSupplier, (context, hand) -> basePoseSequence, choosePoseIfTrue, chooseEvaluationPriority);
        }

        public static class Builder {
            private final String stateIdentifier;
            private final Predicate<ItemStack> choosePoseIfTrue;
            private final int evaluationPriority;
            private final HandPoseFunctionSupplier poseFunctionSupplier;
            private final BiFunction<DriverGetter, InteractionHand, ResourceLocation> basePoseSequenceSupplier;

            private Supplier<PoseFunction<LocalSpacePose>> miningPoseFunctionSupplier;
            private ResourceLocation raiseSequence;
            private ResourceLocation lowerSequence;
            private Transition raiseToPoseTransition;
            private Transition poseToLowerTransition;
            private ItemRenderType itemRenderType;
            private InteractionHand[] handsToUsePoseIn;

            private Builder(
                    String stateIdentifier,
                    HandPoseFunctionSupplier poseFunctionSupplier,
                    BiFunction<DriverGetter, InteractionHand, ResourceLocation> basePoseSequenceSupplier,
                    Predicate<ItemStack> choosePoseIfTrue,
                    int evaluationPriority
            ) {
                this.stateIdentifier = stateIdentifier;
                this.poseFunctionSupplier = poseFunctionSupplier;
                this.choosePoseIfTrue = choosePoseIfTrue;
                this.evaluationPriority = evaluationPriority;
                this.basePoseSequenceSupplier = basePoseSequenceSupplier;

                this.miningPoseFunctionSupplier = FirstPersonMining::constructPickaxeMiningPoseFunction;
                this.raiseSequence = FirstPersonAnimationSequences.HAND_GENERIC_ITEM_RAISE;
                this.lowerSequence = FirstPersonAnimationSequences.HAND_GENERIC_ITEM_LOWER;
                this.raiseToPoseTransition = Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build();
                this.poseToLowerTransition = Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build();
                this.itemRenderType = ItemRenderType.THIRD_PERSON_ITEM;
                this.handsToUsePoseIn = InteractionHand.values();
            }

            public Builder setRaiseSequence(ResourceLocation sequence) {
                this.raiseSequence = sequence;
                return this;
            }

            public Builder setLowerSequence(ResourceLocation sequence) {
                this.lowerSequence = sequence;
                return this;
            }

            public Builder setRaiseToPoseTransition(Transition transition) {
                this.raiseToPoseTransition = transition;
                return this;
            }

            public Builder setPoseToLowerTransition(Transition transition) {
                this.poseToLowerTransition = transition;
                return this;
            }

            public Builder setItemRenderType(ItemRenderType renderType) {
                this.itemRenderType = renderType;
                return this;
            }

            public Builder setHandsToUsePoseIn(InteractionHand... hands) {
                this.handsToUsePoseIn = hands;
                return this;
            }

            public Builder setMiningPoseFunctionSuppler(Supplier<PoseFunction<LocalSpacePose>> miningPoseFunctionSuppler) {
                this.miningPoseFunctionSupplier = miningPoseFunctionSuppler;
                return this;
            }

            public HandPoseDefinition build() {
                return new HandPoseDefinition(
                        this.stateIdentifier,
                        this.choosePoseIfTrue,
                        this.evaluationPriority,
                        this.poseFunctionSupplier,
                        this.miningPoseFunctionSupplier,
                        this.basePoseSequenceSupplier,
                        this.raiseSequence,
                        this.lowerSequence,
                        this.raiseToPoseTransition,
                        this.poseToLowerTransition,
                        this.itemRenderType,
                        this.handsToUsePoseIn
                );
            }
        }
    }

    @FunctionalInterface
    public interface HandPoseFunctionSupplier {
        PoseFunction<LocalSpacePose> constructHandPose(
                CachedPoseContainer cachedPoseContainer,
                InteractionHand hand,
                PoseFunction<LocalSpacePose> miningPoseFunction
        );

        static PoseFunction<LocalSpacePose> constructOnlyWithMiningAnimation(
                CachedPoseContainer cachedPoseContainer,
                InteractionHand hand,
                PoseFunction<LocalSpacePose> miningPoseFunction
        ) {
            return miningPoseFunction;
        }
    }

    public static HandPoseDefinition getOrThrowFromIdentifier(ResourceLocation identifier) {
        HandPoseDefinition definition = HAND_POSES_BY_IDENTIFIER.get(identifier);
        if (definition == null) {
            throw new RuntimeException("ResourceLocation " + identifier + " is not a registered hand pose.");
        }
        return definition;
    }

    public static Set<ResourceLocation> getRegisteredHandPoseDefinitions() {
        return HAND_POSES_BY_IDENTIFIER.keySet();
    }

    public static ResourceLocation testForNextHandPose(ItemStack itemStack, InteractionHand hand) {

        Map<ResourceLocation, HandPoseDefinition> handPosesSortedByPriority = HAND_POSES_BY_IDENTIFIER.entrySet()
                .stream()
                .sorted(Comparator.comparingInt(entry -> -entry.getValue().evaluationPriority()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));

        for (ResourceLocation key : handPosesSortedByPriority.keySet()) {
            HandPoseDefinition definition = HAND_POSES_BY_IDENTIFIER.get(key);
            boolean poseHasBeenChosen = definition.choosePoseIfTrue().test(itemStack);
            boolean poseCanPlayInCurrentHand = Arrays.asList(definition.handsToUsePoseIn()).contains(hand);
            if (poseHasBeenChosen && poseCanPlayInCurrentHand) {
                return key;
            }
        }
        return getFallback();
    }
}
