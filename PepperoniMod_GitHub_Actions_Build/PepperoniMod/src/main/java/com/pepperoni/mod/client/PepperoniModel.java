package com.pepperoni.mod.client;

import com.pepperoni.mod.entity.PepperoniEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class PepperoniModel extends HierarchicalModel<PepperoniEntity> {
    private final ModelPart root, body, head, rightArm, leftArm, rightLeg, leftLeg, armorBody;

    public PepperoniModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.rightArm = body.getChild("right_arm");
        this.leftArm = body.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.armorBody = body.getChild("armor_body");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild("body",
                net.minecraft.client.model.geom.builders.CubeListBuilder.create()
                        .texOffs(0, 32).addBox(-4, -12, -2, 8, 12, 4, new CubeDeformation(0.15F)),
                PartPose.offset(0, 12, 0));

        body.addOrReplaceChild("right_arm",
                net.minecraft.client.model.geom.builders.CubeListBuilder.create()
                        .texOffs(32, 32).addBox(-2, -2, -2, 4, 12, 4, new CubeDeformation(0.18F)),
                PartPose.offset(-5, -10, 0));
        body.addOrReplaceChild("left_arm",
                net.minecraft.client.model.geom.builders.CubeListBuilder.create()
                        .texOffs(48, 32).addBox(-2, -2, -2, 4, 12, 4, new CubeDeformation(0.18F)),
                PartPose.offset(5, -10, 0));
        body.addOrReplaceChild("armor_body",
                net.minecraft.client.model.geom.builders.CubeListBuilder.create()
                        .texOffs(0, 96).addBox(-4.5F, -12.5F, -2.5F, 9, 13, 5, new CubeDeformation(0.28F)),
                PartPose.ZERO);

        PartDefinition head = root.addOrReplaceChild("head",
                net.minecraft.client.model.geom.builders.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4, -8, -4, 8, 8, 8),
                PartPose.offset(0, 0, 0));

        // Oversized blocky curls: the distinctive Pepperoni silhouette.
        var hair = head.addOrReplaceChild("hair_cap",
                net.minecraft.client.model.geom.builders.CubeListBuilder.create()
                        .texOffs(64, 0).addBox(-6, -9, -5, 12, 5, 10, new CubeDeformation(0.35F)),
                PartPose.ZERO);
        int[][] curls = {
                {-7,-5,-4}, {4,-5,-4}, {-7,-1,-4}, {4,-1,-4},
                {-7,3,-3}, {4,3,-3}, {-6,6,-2}, {3,6,-2},
                {-5,-7,3}, {2,-7,3}, {-3,-9,-3}, {1,-9,-3}
        };
        for (int i = 0; i < curls.length; i++) {
            int[] c = curls[i];
            hair.addOrReplaceChild("curl_" + i,
                    net.minecraft.client.model.geom.builders.CubeListBuilder.create()
                            .texOffs(64, 16).addBox(c[0], c[1], c[2], 4, 4, 4, new CubeDeformation(0.12F)),
                    PartPose.ZERO);
        }
        // White bow and little golden bell.
        head.addOrReplaceChild("bow_left",
                net.minecraft.client.model.geom.builders.CubeListBuilder.create()
                        .texOffs(96, 32).addBox(-5, -11, -5, 4, 3, 1),
                PartPose.ZERO);
        head.addOrReplaceChild("bow_right",
                net.minecraft.client.model.geom.builders.CubeListBuilder.create()
                        .texOffs(96, 36).addBox(1, -11, -5, 4, 3, 1),
                PartPose.ZERO);
        head.addOrReplaceChild("bell",
                net.minecraft.client.model.geom.builders.CubeListBuilder.create()
                        .texOffs(112, 32).addBox(-1, -10, -6, 2, 2, 2),
                PartPose.ZERO);

        root.addOrReplaceChild("right_leg",
                net.minecraft.client.model.geom.builders.CubeListBuilder.create()
                        .texOffs(0, 48).addBox(-2, 0, -2, 4, 12, 4),
                PartPose.offset(-2, 12, 0));
        root.addOrReplaceChild("left_leg",
                net.minecraft.client.model.geom.builders.CubeListBuilder.create()
                        .texOffs(16, 48).addBox(-2, 0, -2, 4, 12, 4),
                PartPose.offset(2, 12, 0));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(PepperoniEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        head.yRot = netHeadYaw * ((float)Math.PI / 180F);
        head.xRot = headPitch * ((float)Math.PI / 180F);
        rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.35F * limbSwingAmount;
        leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.35F * limbSwingAmount;
        rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 0.8F * limbSwingAmount;
        leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 0.8F * limbSwingAmount;
        if (entity.isSleeping()) {
            head.xRot = 0.9F;
            body.xRot = 0.15F;
            rightArm.xRot = -1.2F;
            leftArm.xRot = -1.2F;
            rightLeg.xRot = -0.15F;
            leftLeg.xRot = -0.15F;
        } else if (entity.isOrderedToSit()) {
            body.xRot = -0.18F;
            rightLeg.xRot = -1.25F;
            leftLeg.xRot = -1.25F;
            rightArm.xRot = -0.65F;
            leftArm.xRot = -0.65F;
        } else {
            body.xRot = 0;
        }
        armorBody.visible = entity.getArmorTier() > 0;
        head.y += Mth.cos(ageInTicks * 0.08F) * 0.12F;
        if (entity.isWaving() && !entity.isSleeping() && !entity.isOrderedToSit()) {
            rightArm.xRot = -2.4F + Mth.sin(ageInTicks * 0.55F) * 0.22F;
            rightArm.zRot = -0.28F;
        }
        if (entity.isCuddling()) {
            body.xRot = -0.75F;
            rightArm.xRot = -1.4F;
            leftArm.xRot = -1.4F;
            head.xRot = 0.25F;
        }
    }

    @Override
    public ModelPart root() {
        return root;
    }
}
