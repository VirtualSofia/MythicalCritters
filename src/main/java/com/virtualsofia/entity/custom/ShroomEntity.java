package com.virtualsofia.entity.custom;

import com.mojang.logging.LogUtils;
import com.virtualsofia.entity.ModEntities;
import com.virtualsofia.mythicalcritters.MythicalCritters;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ShroomEntity extends TamableAnimal {
    //DECLARES LOGGER for debugging
    private static final Logger LOGGER = LogUtils.getLogger();

    //DECLARE ANIMATION STATES
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState sitAnimationState = new AnimationState();
    public final AnimationState satAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;

    //DECLARE ENTITY
    public ShroomEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    //DECLARE FOOD
    private static final TagKey<Item> SHROOM_FOOD = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MythicalCritters.MODID, "shroom_food"));


    //GOALS/MAIN AI
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this,2.0));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new BreedGoal(this,0));
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.5D, 10.0F, 2.0F));
        this.goalSelector.addGoal(5, new TemptGoal(this, 1.5, Ingredient.of(SHROOM_FOOD), false));
        this.goalSelector.addGoal(6, new FollowParentGoal(this, 1.5));

        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this,1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

    }

    //ATTRIBUTES
    public static AttributeSupplier.Builder createAttributes(){
        return TamableAnimal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 10d)
                .add(Attributes.MOVEMENT_SPEED, 0.25d)
                .add(Attributes.FOLLOW_RANGE, 24d);
    }
    // SETS BREEDING FOOD
    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(SHROOM_FOOD);
    }

    //SETS BABY
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return ModEntities.SHROOM.get().create(level);
    }

    //IDLE ANIM LOGIC
    private void setupAnimationStates(){
        if(this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = 20;
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
        }
    }

    //TICK
    @Override
    public void tick() {
        super.tick();

        if(this.level().isClientSide()) {
            this.setupAnimationStates();
        }
        //LOGGER.info(String.valueOf(isInSittingPose()));
    }


    //tame logic (direct from wolf, should be tweaked)
    private void tryToTame(Player player) {
        if (this.random.nextInt(3) == 0  && !net.neoforged.neoforge.event.EventHooks.onAnimalTame(this, player)) {
            this.tame(player);
            this.navigation.stop();
            this.setTarget(null);
            this.setOrderedToSit(true);
            this.level().broadcastEntityEvent(this, (byte)7);
        } else {
            this.level().broadcastEntityEvent(this, (byte)6);
        }
    }

    //INTERACTIONS
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        //gets interacted item
        ItemStack itemstack = player.getItemInHand(hand);
        Item item = itemstack.getItem();
        //Taming
        if (itemstack.is(Items.BONE) && !this.isOwnedBy(player)) {
            itemstack.consume(1, player);
            this.tryToTame(player);

            return InteractionResult.SUCCESS;
        }
        //Sitting
    else if (!itemstack.is(Items.BONE) && !itemstack.is(SHROOM_FOOD) && this.isOwnedBy(player)) {
            //plays sit animation
            if(!this.isOrderedToSit()) {
                this.sitAnimationState.start(this.tickCount);
            }else {
                this.sitAnimationState.stop();
                this.satAnimationState.start(this.tickCount);}
            //sit/unsit logic
            this.setOrderedToSit(!this.isOrderedToSit());
            this.jumping = false;
            this.navigation.stop();
            this.setTarget(null);
            return InteractionResult.SUCCESS_NO_ITEM_USED;
        }
        else {
            return super.mobInteract(player, hand);
        }

    }

    public boolean isInSittingPose() {
        return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
    }
}

