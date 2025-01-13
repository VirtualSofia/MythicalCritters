package com.virtualsofia.item;

import com.virtualsofia.entity.ModEntities;
import com.virtualsofia.mythicalcritters.MythicalCritters;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MythicalCritters.MODID);



    public static final DeferredItem<Item> SHROOM_SPAWN_EGG = ITEMS.register("shroom_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.SHROOM, 0x918f94, 0x8f100e,
                    new Item.Properties()));



    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
