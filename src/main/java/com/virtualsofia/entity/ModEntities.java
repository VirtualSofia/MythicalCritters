package com.virtualsofia.entity;

import com.virtualsofia.entity.custom.ShroomEntity;
import com.virtualsofia.mythicalcritters.MythicalCritters;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, MythicalCritters.MODID);

    public static void register(IEventBus eventBus){
        ENTITY_TYPES.register(eventBus);
    }


    //Register Entities
    public static final Supplier<EntityType<ShroomEntity>> SHROOM =
            ENTITY_TYPES.register("shroom", () -> EntityType.Builder.of(ShroomEntity::new, MobCategory.CREATURE)
                    .sized(.5f,.5f).build("shroom"));


}
