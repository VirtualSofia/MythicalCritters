package com.virtualsofia.entity.custom;

import com.mojang.logging.LogUtils;
import com.virtualsofia.entity.ModEntities;
import com.virtualsofia.mythicalcritters.MythicalCritters;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ShroomEntity extends TamableAnimal {
    //Entity DAta stuff
    private static final EntityDataAccessor<Integer> DATA_DUPLICATE_COOLDOWN = SynchedEntityData.defineId(ShroomEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> AGE = SynchedEntityData.defineId(ShroomEntity.class, EntityDataSerializers.INT);
    //DECLARES LOGGER for debugging
    private static final Logger LOGGER = LogUtils.getLogger();

    //DECLARE ANIMATION STATES
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState sitAnimationState = new AnimationState();
    public final AnimationState satAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;
    private boolean sitAnimationPlayed = false;
    private boolean satAnimationPlayed = false;

    //DECLARE ENTITY
    public ShroomEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    //DECLARE FOOD
    private static final TagKey<Item> SHROOM_FOOD = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MythicalCritters.MODID, "shroom_food"));
    private static final TagKey<Block> SHROOM_BLOCKS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MythicalCritters.MODID, "shroom_blocks"));

    //Entity Data Builder
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);

        builder.define(DATA_DUPLICATE_COOLDOWN, 0);
        builder.define(AGE, this.age);
    }

    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("DuplicateCooldown", this.duplicateCooldown());
        compound.putInt("Age", this.age);
    }

    private int duplicateCooldown() {
        return this.entityData.get(DATA_DUPLICATE_COOLDOWN);
    }
    private int age(){
        return this.entityData.get(AGE);
    }
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        this.entityData.set(DATA_DUPLICATE_COOLDOWN, compound.getInt("DuplicateCooldown"));
    }



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
        return false;
    }


    //SETS BABY
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return ModEntities.SHROOM.get().create(level);
    }

    // ANIMATION LOGIC
    private void setupAnimationStates(){
        //Idle
        if(this.idleAnimationTimeout <= 0 && !this.sitAnimationPlayed) {
            this.idleAnimationTimeout = 20;
            this.idleAnimationState.start(this.tickCount);
          //  LOGGER.info("Idle Play");
        } else {
            --this.idleAnimationTimeout;
        }
        //sit
        if (isInSittingPose()) {
            if(!this.sitAnimationPlayed) {
                this.idleAnimationState.stop();
                // not sure if this is needed but is needed in unsit
                this.satAnimationState.stop();
                //plays the animation
                this.sitAnimationState.start(this.tickCount);
                //so doesnt play aniamtion again
                sitAnimationPlayed = true;
            }
            //so sat animation can be played again
            satAnimationPlayed = false;
        }
        //unsit
        if (!isInSittingPose()) {
            if(!this.satAnimationPlayed) {
                //not sure why this is needed but sat animation doesn't play right without it
                this.sitAnimationState.stop();
                this.satAnimationState.start(this.tickCount);
                satAnimationPlayed = true;
            }
            sitAnimationPlayed = false;
        }
    }

    //TICK
    @Override
    public void tick() {
        super.tick();
        //Server Logic
        if(!this.level().isClientSide){
            if (duplicateCooldown() > 0){
                int down = duplicateCooldown() - 1;
                this.entityData.set(DATA_DUPLICATE_COOLDOWN, down);
            }

        }
        if(this.level().isClientSide()) {
            this.setupAnimationStates();
            this.age = this.entityData.get(AGE);
            LOGGER.info(String.valueOf(this.entityData.get(AGE)));

        }
       // LOGGER.info(String.valueOf(isOnReproductonBlock()));

    }


    //tame logic (direct from wolf, values should be tweaked)
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
        //Taming                                                           //checks if can duplicate
        if (itemstack.is(SHROOM_FOOD) && !this.isOwnedBy(player) && (duplicateCooldown() > 0 || (this.age < 0))) {
            itemstack.consume(1, player );
            this.tryToTame(player);

            return InteractionResult.SUCCESS;
        }
        //Duplicate
        else if(itemstack.is(SHROOM_FOOD) && isOnReproductonBlock() && duplicateCooldown() <= 0 && this.age == 0){
            this.duplicate();
            return InteractionResult.SUCCESS;
        }

        //Sitting
        else if (!itemstack.is(Items.BONE) && !itemstack.is(SHROOM_FOOD) && this.isOwnedBy(player)) {

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

    //GETS IF SITTING, no idea how this works, stolen directly from wolf
    public boolean isInSittingPose() {
        return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
    }


    //Duplication

    private boolean isOnReproductonBlock(){
        BlockPos pos = this.blockPosition();
        BlockPos groundPos = pos.offset(0, -1,0);
        BlockState groundState = this.level().getBlockState(groundPos);
        return groundState.is(SHROOM_BLOCKS);

    }

     public void duplicate(){
        this.entityData.set(DATA_DUPLICATE_COOLDOWN, 6000);
         ShroomEntity shroom = ModEntities.SHROOM.get().create(this.level());
         if (shroom != null) {
             shroom.moveTo(this.position());
             this.level().addFreshEntity(shroom);
             shroom.setAge(-24000);
         }
    }

//SPAWN CONDITIONS
    public static boolean shroomSpawnRules(
            EntityType<? extends Animal> animal, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random
    ) {
        boolean spawnBlock = level.getBlockState(pos.below()).is(Blocks.MYCELIUM);
        boolean flag = MobSpawnType.ignoresLightRequirements(spawnType)|| isBrightEnoughToSpawn(level, pos);
        return (level.getBlockState(pos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON) && flag) || (spawnBlock && flag);
    }

    protected static boolean isBrightEnoughToSpawn(BlockAndTintGetter level, BlockPos pos) {
        return level.getRawBrightness(pos, 0) > 8;
    }
}

