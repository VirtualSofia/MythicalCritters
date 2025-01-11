package com.virtualsofia.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.virtualsofia.entity.custom.ShroomEntity;
import com.virtualsofia.mythicalcritters.MythicalCritters;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ShroomRenderer extends MobRenderer<ShroomEntity, ShroomModel<ShroomEntity>> {

    public ShroomRenderer(EntityRendererProvider.Context context) {
        super(context, new ShroomModel<>(context.bakeLayer(ShroomModel.LAYER_LOCATION)), .5f);
    }

    @Override
    public ResourceLocation getTextureLocation(ShroomEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(MythicalCritters.MODID,"textures/entity/shroom.png");
    }


    @Override
    public void render(ShroomEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        if(entity.isBaby()){
            poseStack.scale(.45f,.45f,.45f);
        }
        else{
            poseStack.scale(1f,1f,1f);
        }
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}
