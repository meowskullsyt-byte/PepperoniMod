
package com.pepperoni.mod;

import com.pepperoni.mod.entity.PepperoniEntity;
import com.pepperoni.mod.network.PepperoniNetwork;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(PepperoniMod.MODID)
public class PepperoniMod {

    public static final String MODID = "pepperoni";

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<EntityType<PepperoniEntity>> PEPPERONI =
            ENTITIES.register("pepperoni", () ->
                    EntityType.Builder
                            .of(PepperoniEntity::new, MobCategory.CREATURE)
                            .sized(0.72F, 1.92F)
                            .clientTrackingRange(10)
                            .build("pepperoni")
            );

    public static final RegistryObject<Item> PEPPERONI_SPAWN_EGG =
            ITEMS.register("pepperoni_spawn_egg", () ->
                    new ForgeSpawnEggItem(
                            PEPPERONI,
                            0xD7194F,
                            0xF6C8E2,
                            new Item.Properties()
                    )
            );

    public PepperoniMod() {

        IEventBus modBus =
                FMLJavaModLoadingContext.get().getModEventBus();

        // Register networking
        PepperoniNetwork.register();

        // Register entities and items
        ENTITIES.register(modBus);
        ITEMS.register(modBus);

        // Register creative inventory entry
        modBus.addListener(this::addCreative);

        // Register entity attributes
        modBus.addListener(this::registerAttributes);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {

        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(PEPPERONI_SPAWN_EGG);
        }
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {

        event.put(
                PEPPERONI.get(),
                PepperoniEntity.createAttributes().build()
        );
    }
}
