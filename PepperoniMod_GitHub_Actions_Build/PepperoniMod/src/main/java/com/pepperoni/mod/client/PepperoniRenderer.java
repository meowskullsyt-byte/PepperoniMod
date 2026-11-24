package com.pepperoni.mod.client;

import com.pepperoni.mod.PepperoniMod;
import com.pepperoni.mod.entity.PepperoniEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class PepperoniRenderer extends MobRenderer<PepperoniEntity, PepperoniModel> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(PepperoniMod.MODID, "textures/entity/pepperoni.png");

    public PepperoniRenderer(EntityRendererProvider.Context context) {
        super(context, new PepperoniModel(context.bakeLayer(PepperoniClient.PEPPERONI_LAYER)), 0.42F);
    }

    @Override
    public ResourceLocation getTextureLocation(PepperoniEntity entity) {
        return TEXTURE;
    }
}
