package com.virtualsofia.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.virtualsofia.entity.custom.ShroomEntity;
import com.virtualsofia.mythicalcritters.MythicalCritters;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

import javax.swing.text.html.parser.Entity;

public class ShroomModel<T extends ShroomEntity> extends HierarchicalModel<T> {

        // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
        public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(MythicalCritters.MODID, "shroom"), "main");
        private final ModelPart root;
        private final ModelPart feet;
        private final ModelPart rightfoot;
        private final ModelPart leftfoot;
        private final ModelPart notfeet;
        private final ModelPart head;
        private final ModelPart body;

        public ShroomModel(ModelPart root) {
            this.root = root.getChild("root");
            this.feet = this.root.getChild("feet");
            this.rightfoot = this.feet.getChild("rightfoot");
            this.leftfoot = this.feet.getChild("leftfoot");
            this.notfeet = this.root.getChild("notfeet");
            this.head = this.notfeet.getChild("head");
            this.body = this.notfeet.getChild("body");
        }

        public static LayerDefinition createBodyLayer() {
            MeshDefinition meshdefinition = new MeshDefinition();
            PartDefinition partdefinition = meshdefinition.getRoot();

            PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 26.75F, 0.0F, 0.0F, 3.1416F, 0.0F));

            PartDefinition feet = root.addOrReplaceChild("feet", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -0.5F));

            PartDefinition rightfoot = feet.addOrReplaceChild("rightfoot", CubeListBuilder.create().texOffs(20, 30).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.25F, -4.75F, 0.0F));

            PartDefinition leftfoot = feet.addOrReplaceChild("leftfoot", CubeListBuilder.create().texOffs(28, 30).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.25F, -4.75F, 0.0F));

            PartDefinition notfeet = root.addOrReplaceChild("notfeet", CubeListBuilder.create(), PartPose.offset(0.0F, -2.75F, 0.0F));

            PartDefinition head = notfeet.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -7.0F, -7.5F, 12.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
                    .texOffs(0, 14).addBox(-5.0F, -8.0F, -6.0F, 10.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                    .texOffs(20, 24).addBox(-2.5F, -9.0F, -4.0F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.25F, 1.5F, 0.0873F, 0.0F, 0.0F));

            PartDefinition body = notfeet.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 24).addBox(-2.5F, -10.0F, -2.5F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.75F, 0.0F, 0.0873F, 0.0F, 0.0F));

            return LayerDefinition.create(meshdefinition, 64, 64);
        }

        @Override
        public void setupAnim(ShroomEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            this.root().getAllParts().forEach(ModelPart::resetPose);


            this.animateWalk(ShroomAnimations.ANIM_SHROOM_WALK, limbSwing, limbSwingAmount, 2f, 2.5f);
            this.animate(entity.idleAnimationState, ShroomAnimations.ANIM_SHROOM_IDLE, ageInTicks, 1f);
        }

        @Override
        public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
            root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        }

        public ModelPart root(){

        return root;

    }

}
