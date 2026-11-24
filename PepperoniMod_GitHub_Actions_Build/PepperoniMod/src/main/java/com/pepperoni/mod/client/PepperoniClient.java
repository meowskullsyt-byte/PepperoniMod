package com.pepperoni.mod.client;

import com.pepperoni.mod.PepperoniMod;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.renderer.entity.EntityRenderers;

@Mod.EventBusSubscriber(modid = PepperoniMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PepperoniClient {
    public static void openScreen(int entityId, int bond, String mode) {
        net.minecraft.client.Minecraft.getInstance().setScreen(new PepperoniScreen(entityId, bond, mode));
    }
    public static final ModelLayerLocation PEPPERONI_LAYER =
            new ModelLayerLocation(new ResourceLocation(PepperoniMod.MODID, "pepperoni"), "main");

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(PEPPERONI_LAYER, PepperoniModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(PepperoniMod.PEPPERONI.get(), PepperoniRenderer::new);
    }
}
