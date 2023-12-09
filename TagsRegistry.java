package net.mrscauthd.beyond_earth.registries;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.material.Fluid;
import net.mrscauthd.beyond_earth.BeyondEarthMod;

public class TagsRegistry {

    /** ENTITIES */
    public static final TagKey<EntityType<?>> OXYGEN_TAG = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(BeyondEarthMod.MODID, "entities/oxygen"));
    public static final TagKey<EntityType<?>> PLANET_FIRE_TAG = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(BeyondEarthMod.MODID, "entities/planet_fire"));
    public static final TagKey<EntityType<?>> VENUS_RAIN_TAG = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(BeyondEarthMod.MODID, "entities/venus_rain"));

    /** FLUIDS */
    public static final TagKey<Fluid> FLUID_VEHICLE_FUEL_TAG = TagKey.create(Registry.FLUID_REGISTRY, new ResourceLocation(BeyondEarthMod.MODID, "vehicle_fuel"));
    public static final TagKey<Fluid> FLUID_FUEL_TAG = TagKey.create(Registry.FLUID_REGISTRY, new ResourceLocation(BeyondEarthMod.MODID, "fuel"));
    public static final TagKey<Fluid> OIL_FLUID_TAG = TagKey.create(Registry.FLUID_REGISTRY, new ResourceLocation(BeyondEarthMod.MODID, "oil"));
    public static final TagKey<Fluid> WATER_FLUID_TAG = TagKey.create(Registry.FLUID_REGISTRY, new ResourceLocation(BeyondEarthMod.MODID, "water"));
    public static final TagKey<Fluid> LAVA_FLUID_TAG = TagKey.create(Registry.FLUID_REGISTRY, new ResourceLocation(BeyondEarthMod.MODID, "lava"));
    public static final TagKey<Fluid> HYDROGEN_FLUID_TAG = TagKey.create(Registry.FLUID_REGISTRY, new ResourceLocation(BeyondEarthMod.MODID, "hydrogen"));
    public static final TagKey<Fluid> OXYGENE_FLUID_TAG = TagKey.create(Registry.FLUID_REGISTRY, new ResourceLocation(BeyondEarthMod.MODID, "oxygene"));
}
