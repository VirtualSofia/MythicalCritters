package com.virtualsofia.event;

import com.virtualsofia.entity.ModEntities;
import com.virtualsofia.entity.client.ShroomModel;
import com.virtualsofia.entity.custom.ShroomEntity;
import com.virtualsofia.mythicalcritters.MythicalCritters;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = MythicalCritters.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ShroomModel.LAYER_LOCATION, ShroomModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.SHROOM.get(), ShroomEntity.createAttributes().build());
    }
}
