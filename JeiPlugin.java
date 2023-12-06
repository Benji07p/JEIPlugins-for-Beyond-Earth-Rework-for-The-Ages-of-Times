package net.mrscauthd.beyond_earth.jei;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.mrscauthd.beyond_earth.BeyondEarthMod;
import net.mrscauthd.beyond_earth.capabilities.oxygen.OxygenUtil;
import net.mrscauthd.beyond_earth.config.Config;
import net.mrscauthd.beyond_earth.crafting.BeyondEarthRecipeTypes;
import net.mrscauthd.beyond_earth.crafting.CompressingRecipe;
import net.mrscauthd.beyond_earth.crafting.FuelRefiningRecipe;
import net.mrscauthd.beyond_earth.crafting.GeneratingRecipe;
import net.mrscauthd.beyond_earth.crafting.IngredientStack;
import net.mrscauthd.beyond_earth.crafting.OxygenBubbleDistributorRecipe;
import net.mrscauthd.beyond_earth.crafting.OxygenLoaderRecipe;
import net.mrscauthd.beyond_earth.crafting.RocketPart;
import net.mrscauthd.beyond_earth.crafting.SpaceStationRecipe;
import net.mrscauthd.beyond_earth.crafting.WorkbenchingRecipe;
import net.mrscauthd.beyond_earth.events.Methods;
import net.mrscauthd.beyond_earth.fluids.FluidUtil2;
import net.mrscauthd.beyond_earth.gauge.GaugeTextHelper;
import net.mrscauthd.beyond_earth.gauge.GaugeValueHelper;
import net.mrscauthd.beyond_earth.guis.helper.GridPlacer;
import net.mrscauthd.beyond_earth.guis.helper.GuiHelper;
import net.mrscauthd.beyond_earth.guis.helper.IPlacer;
import net.mrscauthd.beyond_earth.guis.helper.RocketPartGridPlacer;
import net.mrscauthd.beyond_earth.guis.screens.coalgenerator.CoalGeneratorGui;
import net.mrscauthd.beyond_earth.guis.screens.coalgenerator.CoalGeneratorGuiWindow;
import net.mrscauthd.beyond_earth.guis.screens.compressor.CompressorGui;
import net.mrscauthd.beyond_earth.guis.screens.compressor.CompressorGuiWindow;
import net.mrscauthd.beyond_earth.guis.screens.fuelrefinery.FuelRefineryGui;
import net.mrscauthd.beyond_earth.guis.screens.fuelrefinery.FuelRefineryGuiWindow;
import net.mrscauthd.beyond_earth.guis.screens.nasaworkbench.NasaWorkbenchGui;
import net.mrscauthd.beyond_earth.guis.screens.nasaworkbench.NasaWorkbenchGuiWindow;
import net.mrscauthd.beyond_earth.guis.screens.oxygenbubbledistributor.OxygenBubbleDistributorGui;
import net.mrscauthd.beyond_earth.guis.screens.oxygenbubbledistributor.OxygenBubbleDistributorGuiWindow;
import net.mrscauthd.beyond_earth.guis.screens.oxygenloader.OxygenLoaderGui;
import net.mrscauthd.beyond_earth.guis.screens.oxygenloader.OxygenLoaderGuiWindow;
import net.mrscauthd.beyond_earth.guis.screens.rocket.RocketGui;
import net.mrscauthd.beyond_earth.guis.screens.rocket.RocketGuiWindow;
import net.mrscauthd.beyond_earth.guis.screens.rover.RoverGuiWindow;
import net.mrscauthd.beyond_earth.guis.screens.waterpump.WaterPumpGuiWindow;
import net.mrscauthd.beyond_earth.jei.jeiguihandlers.*;
import net.mrscauthd.beyond_earth.machines.tile.CoalGeneratorBlockEntity;
import net.mrscauthd.beyond_earth.machines.tile.CompressorBlockEntity;
import net.mrscauthd.beyond_earth.machines.tile.FuelRefineryBlockEntity;
import net.mrscauthd.beyond_earth.machines.tile.ItemStackToItemStackBlockEntity;
import net.mrscauthd.beyond_earth.machines.tile.NASAWorkbenchBlockEntity;
import net.mrscauthd.beyond_earth.machines.tile.OxygenBubbleDistributorBlockEntity;
import net.mrscauthd.beyond_earth.machines.tile.OxygenLoaderBlockEntity;
import net.mrscauthd.beyond_earth.registries.*;
import net.mrscauthd.beyond_earth.utils.Rectangle2d;

@mezz.jei.api.JeiPlugin
public class JeiPlugin implements IModPlugin {
	public static IJeiHelpers jeiHelper;

	private Map<Fluid, List<ItemStack>> fluidFullItemStacks;
	private List<ItemStack> oxygenFullItemStacks;
	private List<Fluid> fuelTagFluids;

	private ClientLevel getLevel() {
		Minecraft mc = Minecraft.getInstance();
		return mc.level;
	}

	public List<ItemStack> getFluidFullItemStacks(Fluid fluid) {
		return this.fluidFullItemStacks.computeIfAbsent(fluid, this::generateFluidFullIngredients);
	}

	public List<ItemStack> getFluidFullItemStacks(Collection<Fluid> fluids) {
		return fluids.stream().flatMap(f -> this.getFluidFullItemStacks(f).stream()).collect(Collectors.toList());
	}

	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(BeyondEarthMod.MODID, "default");
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
		int inventorySlotCount = 36;
		// Oxygen Loader
		registration.addRecipeTransferHandler(OxygenLoaderGui.GuiContainer.class, OxygenLoaderJeiCategory.recipeType, OxygenLoaderBlockEntity.SLOT_INPUT_SOURCE, 1, OxygenLoaderBlockEntity.SLOT_OUTPUT_SOURCE + 1, inventorySlotCount);
		// Oxygen Bubble Distributor
		registration.addRecipeTransferHandler(OxygenBubbleDistributorGui.GuiContainer.class, OxygenBubbleDistributorJeiCategory.recipeType, OxygenBubbleDistributorBlockEntity.SLOT_INPUT_SOURCE, 1, OxygenBubbleDistributorBlockEntity.SLOT_INPUT_SINK + 1, inventorySlotCount);
		// Coal Generator
		registration.addRecipeTransferHandler(CoalGeneratorGui.GuiContainer.class, CoalGeneratorJeiCategory.recipeType, CoalGeneratorBlockEntity.SLOT_FUEL, 1, CoalGeneratorBlockEntity.SLOT_FUEL + 1, inventorySlotCount);
		// Compressor
		registration.addRecipeTransferHandler(CompressorGui.GuiContainer.class, CompressorJeiCategory.recipeType, ItemStackToItemStackBlockEntity.SLOT_INGREDIENT, 1, ItemStackToItemStackBlockEntity.SLOT_OUTPUT + 1, inventorySlotCount);
		// NASA WorkBench
		int workbenchPartSlotStart = 1 + NASAWorkbenchBlockEntity.SLOT_PARTS;
		int workbenchPartSlotCount = NASAWorkbenchBlockEntity.getBasicPartSlots();
		registration.addRecipeTransferHandler(NasaWorkbenchGui.GuiContainer.class, NasaWorkbenchJeiCategory.recipeType, workbenchPartSlotStart, workbenchPartSlotCount, workbenchPartSlotStart + workbenchPartSlotCount, inventorySlotCount);
		// Fuel Refinery
		registration.addRecipeTransferHandler(FuelRefineryGui.GuiContainer.class, FuelRefineryJeiCategory.recipeType, FuelRefineryBlockEntity.SLOT_INPUT_SOURCE, 1, FuelRefineryBlockEntity.SLOT_OUTPUT_SOURCE + 1, inventorySlotCount);
		// Rocket tier 1
		registration.addRecipeTransferHandler(RocketGui.GuiContainer.class, RocketTier1JeiCategory.recipeType, 0, 1, 1, inventorySlotCount);
		// Rocket tier 2
		registration.addRecipeTransferHandler(RocketGui.GuiContainer.class, RocketTier2JeiCategory.recipeType, 0, 1, 1, inventorySlotCount);
		// Rocket tier 3
		registration.addRecipeTransferHandler(RocketGui.GuiContainer.class, RocketTier3JeiCategory.recipeType, 0, 1, 1, inventorySlotCount);
		// Rocket tier 4
		registration.addRecipeTransferHandler(RocketGui.GuiContainer.class, RocketTier4JeiCategory.recipeType, 0, 1, 1, inventorySlotCount);
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addRecipeClickArea(NasaWorkbenchGuiWindow.class, 108, 49, 14, 14, NasaWorkbenchJeiCategory.recipeType);
		registration.addRecipeClickArea(OxygenLoaderGuiWindow.class, OxygenLoaderGuiWindow.ARROW_LEFT, OxygenLoaderGuiWindow.ARROW_TOP, GuiHelper.ARROW_WIDTH, GuiHelper.ARROW_HEIGHT, OxygenLoaderJeiCategory.recipeType);
		registration.addRecipeClickArea(OxygenBubbleDistributorGuiWindow.class, OxygenBubbleDistributorGuiWindow.ARROW_LEFT, OxygenBubbleDistributorGuiWindow.ARROW_TOP, GuiHelper.ARROW_WIDTH, GuiHelper.ARROW_HEIGHT, OxygenBubbleDistributorJeiCategory.recipeType);
		registration.addRecipeClickArea(FuelRefineryGuiWindow.class, FuelRefineryGuiWindow.ARROW_LEFT, FuelRefineryGuiWindow.ARROW_TOP, GuiHelper.ARROW_WIDTH, GuiHelper.ARROW_HEIGHT, FuelRefineryJeiCategory.recipeType);

		registration.addGuiContainerHandler(OxygenLoaderGuiWindow.class, new OxygenLoaderGuiContainerHandler());
		registration.addGuiContainerHandler(OxygenBubbleDistributorGuiWindow.class, new OxygenBubbleDistributorGuiContainerHandler());
		registration.addGuiContainerHandler(CoalGeneratorGuiWindow.class, new CoalGeneratorGuiContainerHandler());
		registration.addGuiContainerHandler(CompressorGuiWindow.class, new CompressorGuiContainerHandler());
		registration.addGuiContainerHandler(FuelRefineryGuiWindow.class, new FuelRefineryGuiContainerHandler());
		registration.addGuiContainerHandler(WaterPumpGuiWindow.class, new WaterPumpGuiContainerHandler());
		registration.addGuiContainerHandler(RocketGuiWindow.class, new RocketGuiContainerHandler());
		registration.addGuiContainerHandler(RoverGuiWindow.class, new RoverGuiContainerHandler());
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		jeiHelper = registration.getJeiHelpers();
		registration.addRecipeCategories(new OxygenLoaderJeiCategory(this, jeiHelper.getGuiHelper()));
		registration.addRecipeCategories(new OxygenBubbleDistributorJeiCategory(this, jeiHelper.getGuiHelper()));
		// Coal Generator
		registration.addRecipeCategories(new CoalGeneratorJeiCategory(jeiHelper.getGuiHelper()));
		// NASA Workbench
		registration.addRecipeCategories(new NasaWorkbenchJeiCategory(jeiHelper.getGuiHelper()));
		// Rocket Tier 1
		registration.addRecipeCategories(new RocketTier1JeiCategory(jeiHelper.getGuiHelper()));
		// Rocket Tier 2
		registration.addRecipeCategories(new RocketTier2JeiCategory(jeiHelper.getGuiHelper()));
		// Rocket Tier 3
		registration.addRecipeCategories(new RocketTier3JeiCategory(jeiHelper.getGuiHelper()));
		// Rocket Tier 4
		registration.addRecipeCategories(new RocketTier4JeiCategory(jeiHelper.getGuiHelper()));
		// Compressor
		registration.addRecipeCategories(new CompressorJeiCategory(jeiHelper.getGuiHelper()));
		// Fuel Maker
		registration.addRecipeCategories(new FuelRefineryJeiCategory(this, jeiHelper.getGuiHelper()));
		// Rover
		registration.addRecipeCategories(new RoverJeiCategory(jeiHelper.getGuiHelper()));
		// Space Station
		registration.addRecipeCategories(new SpaceStationJeiCategory(jeiHelper.getGuiHelper()));
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		this.fluidFullItemStacks = new HashMap<>();
		this.oxygenFullItemStacks = this.generateOxygenLoadingItems();
		this.fuelTagFluids = this.generateFuelTagFluids();

		// Oxygen Loader
		registration.addRecipes(OxygenLoaderJeiCategory.recipeType, generateOxygenLoaderRecipes());
		// Oxygen Bubble Distributor
		registration.addRecipes(OxygenBubbleDistributorJeiCategory.recipeType, generateOxygenBubbleDistributorRecipes());
		// Coal Generator
		registration.addRecipes(CoalGeneratorJeiCategory.recipeType, generateGeneratorRecipes());
		// NASA Workbench
		registration.addRecipes(NasaWorkbenchJeiCategory.recipeType, generateWorkbenchRecipes());
		// Rocket Tier 1
		registration.addRecipes(RocketTier1JeiCategory.recipeType, generateFuelLoadingRecipes());
		// Rocket Tier 2
		registration.addRecipes(RocketTier2JeiCategory.recipeType, generateFuelLoadingRecipes());
		// Rocket Tier 3
		registration.addRecipes(RocketTier3JeiCategory.recipeType, generateFuelLoadingRecipes());
		// Rocket Tier 4
		registration.addRecipes(RocketTier4JeiCategory.recipeType, generateFuelLoadingRecipes());
		// Rover
		registration.addRecipes(RoverJeiCategory.recipeType, generateFuelLoadingRecipes());
		// Compressor
		registration.addRecipes(CompressorJeiCategory.recipeType, generateCompressingRecipes());
		// Fuel Maker
		registration.addRecipes(FuelRefineryJeiCategory.recipeType, generateFuelMakerRecipes());
		// Space Station
		registration.addRecipes(SpaceStationJeiCategory.recipeType, generateSpaceStationRecipes());
		// Oil
		Component oilDescriptionKey = new TranslatableComponent("jei.tooltip." + BeyondEarthMod.MODID + ".oil");
		registration.addIngredientInfo(new ItemStack(ItemsRegistry.OIL_BUCKET.get(), 1), VanillaTypes.ITEM_STACK, oilDescriptionKey);
		registration.addIngredientInfo(new FluidStack(FluidsRegistry.OIL_STILL.get(), 1000), ForgeTypes.FLUID_STACK, oilDescriptionKey);

		if (Config.STEEL_MANAGEMENT.get() >= 2) {
			List<ItemStack> list = new ArrayList<>();
			list.add(new ItemStack(ItemsRegistry.STEEL_BLOCK_ITEM.get()));
			list.add(new ItemStack(ItemsRegistry.STEEL_INGOT.get()));
			list.add(new ItemStack(ItemsRegistry.STEEL_NUGGET.get()));
			registration.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, list);
		}
	}

	// Oxygen Loading
	private List<ItemStack> generateOxygenLoadingItems() {
		return ForgeRegistries.ITEMS.getValues().stream().map(ItemStack::new).filter(OxygenUtil::canReceive).map(OxygenUtil::makeFull).collect(Collectors.toList());
	}

	// Oxygen Loader
	private List<OxygenLoaderRecipe> generateOxygenLoaderRecipes() {
		return BeyondEarthRecipeTypes.OXYGEN_LOADING.getRecipes(this.getLevel());
	}

	// Oxygen Bubble Distributor
	private List<OxygenBubbleDistributorRecipe> generateOxygenBubbleDistributorRecipes() {
		return BeyondEarthRecipeTypes.OXYGEN_BUBBLE_DISTRIBUTING.getRecipes(this.getLevel());
	}

	// Generator
	private List<GeneratingRecipe> generateGeneratorRecipes() {
		return BeyondEarthRecipeTypes.COAL_GENERATING.getRecipes(this.getLevel());
	}

	// Workbench
	private List<WorkbenchingRecipe> generateWorkbenchRecipes() {
		return BeyondEarthRecipeTypes.NASA_WORKBENCHING.getRecipes(this.getLevel());
	}

	// Compressor
	private List<CompressingRecipe> generateCompressingRecipes() {
		return BeyondEarthRecipeTypes.COMPRESSING.getRecipes(this.getLevel());
	}

	// Fuel Maker
	private List<ItemStack> generateFluidFullIngredients(Fluid fluid) {
		return ForgeRegistries.ITEMS.getValues().stream().map(i -> new ItemStack(i)).filter(is -> FluidUtil2.canFill(is, fluid)).map(is -> FluidUtil2.makeFull(is, fluid)).collect(Collectors.toList());
	}

	private List<FuelRefiningRecipe> generateFuelMakerRecipes() {
		return BeyondEarthRecipeTypes.FUEL_REFINING.getRecipes(this.getLevel());
	}

	// Fuel Loading
	private List<Fluid> generateFuelTagFluids() {
		return ForgeRegistries.FLUIDS.getValues().stream().filter(f -> f.isSource(f.defaultFluidState()) && Methods.tagCheck(f, TagsRegistry.FLUID_VEHICLE_FUEL_TAG)).collect(Collectors.toList());
	}

	private List<FuelLoadingRecipe> generateFuelLoadingRecipes() {
		List<ItemStack> fuelTagBuckets = new ArrayList<>();

		for (Fluid fluid : this.fuelTagFluids) {
			fuelTagBuckets.add(new ItemStack(fluid.getBucket()));
		}

		FuelLoadingRecipe recipe = new FuelLoadingRecipe(fuelTagBuckets, this.fuelTagFluids);

		List<FuelLoadingRecipe> recipes = new ArrayList<>();
		recipes.add(recipe);
		return recipes;
	}

	// Space Station
	private List<SpaceStationRecipe> generateSpaceStationRecipes() {
		List<SpaceStationRecipe> recipes = new ArrayList<>();
		this.getLevel().getRecipeManager().byKey(SpaceStationRecipe.KEY).ifPresent(r -> recipes.add((SpaceStationRecipe) r));

		return recipes;
	}

	public static class FuelLoadingRecipe {
		private final List<ItemStack> fuelTagBuckets;
		private final List<Fluid> fluids;

		public FuelLoadingRecipe(List<ItemStack> fuelTagBuckets, List<Fluid> fluids) {
			this.fuelTagBuckets = Collections.unmodifiableList(fuelTagBuckets);
			this.fluids = Collections.unmodifiableList(fluids);
		}

		public List<ItemStack> getFuelTagBuckets() {
			return this.fuelTagBuckets;
		}

		public List<FluidStack> getFluidStacks(int amount) {
			return this.getFluid().stream().map(f -> new FluidStack(f, amount)).collect(Collectors.toList());
		}

		public List<Fluid> getFluid() {
			return this.fluids;
		}
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		// Oxygen Loader
		registration.addRecipeCatalyst(new ItemStack(BlocksRegistry.OXYGEN_LOADER_BLOCK.get()), OxygenLoaderJeiCategory.recipeType);
		// Oxygen Bubble Distributor
		registration.addRecipeCatalyst(new ItemStack(BlocksRegistry.OXYGEN_BUBBLE_DISTRIBUTOR_BLOCK.get()), OxygenBubbleDistributorJeiCategory.recipeType);
		// Coal Generator
		registration.addRecipeCatalyst(new ItemStack(BlocksRegistry.COAL_GENERATOR_BLOCK.get()), CoalGeneratorJeiCategory.recipeType);
		// workbench
		registration.addRecipeCatalyst(new ItemStack(ItemsRegistry.NASA_WORKBENCH_ITEM.get()), NasaWorkbenchJeiCategory.recipeType);
		// Compressor
		registration.addRecipeCatalyst(new ItemStack(BlocksRegistry.COMPRESSOR_BLOCK.get()), CompressorJeiCategory.recipeType);
		// FuelMaker
		registration.addRecipeCatalyst(new ItemStack(BlocksRegistry.FUEL_REFINERY_BLOCK.get()), FuelRefineryJeiCategory.recipeType);
		// Rocket Tier 1
		registration.addRecipeCatalyst(new ItemStack(ItemsRegistry.TIER_1_ROCKET_ITEM.get()), RocketTier1JeiCategory.recipeType, SpaceStationJeiCategory.recipeType);
		// Rocket Tier 2
		registration.addRecipeCatalyst(new ItemStack(ItemsRegistry.TIER_2_ROCKET_ITEM.get()), RocketTier2JeiCategory.recipeType, SpaceStationJeiCategory.recipeType);
		// Rocket Tier 3
		registration.addRecipeCatalyst(new ItemStack(ItemsRegistry.TIER_3_ROCKET_ITEM.get()), RocketTier3JeiCategory.recipeType, SpaceStationJeiCategory.recipeType);
		// Rocket Tier 4
		registration.addRecipeCatalyst(new ItemStack(ItemsRegistry.TIER_4_ROCKET_ITEM.get()), RocketTier4JeiCategory.recipeType, SpaceStationJeiCategory.recipeType);
		// Rover
		registration.addRecipeCatalyst(new ItemStack(ItemsRegistry.ROVER_ITEM.get()), RoverJeiCategory.recipeType);
	}

	public static class OxygenLoaderJeiCategory implements IRecipeCategory<OxygenLoaderRecipe> {
		public static final ResourceLocation Uid = new ResourceLocation(BeyondEarthMod.MODID, "oxygen_loader");
		public static final RecipeType recipeType = new RecipeType<>(new ResourceLocation(BeyondEarthMod.MODID, "oxygen_loader"), OxygenLoaderRecipe.class);

		public static final ResourceLocation BACKGROUND = new ResourceLocation(BeyondEarthMod.MODID, "textures/jei/oxygen_loader.png");
		private static final Component title = new TranslatableComponent("container." + BeyondEarthMod.MODID + ".oxygen_loader");

		public static final int INPUT_TANK_LEFT = 8;
		public static final int INPUT_TANK_TOP = 8;
		public static final int OUTPUT_TANK_LEFT = 74;
		public static final int OUTPUT_TANK_TOP = 8;
		public static final int ENERGY_LEFT = 114;
		public static final int ENERGY_TOP = 8;

		private final JeiPlugin plugin;
		private final IDrawableStatic background;
		private final IDrawable fluidOverlay;
		private final LoadingCache<Integer, IDrawableAnimated> cachedEnergies;

		public OxygenLoaderJeiCategory(JeiPlugin plugin, IGuiHelper guiHelper) {
			this.plugin = plugin;
			this.background = guiHelper.drawableBuilder(BACKGROUND, 0, 0, 147, 64).setTextureSize(147, 64).build();
			this.fluidOverlay = guiHelper.drawableBuilder(GuiHelper.FLUID_TANK_PATH, 0, 0, GuiHelper.FLUID_TANK_WIDTH, GuiHelper.FLUID_TANK_HEIGHT).setTextureSize(GuiHelper.FLUID_TANK_WIDTH, GuiHelper.FLUID_TANK_HEIGHT).build();
			this.cachedEnergies = createUsingEnergies(guiHelper);
		}

		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, OxygenLoaderRecipe recipe, IFocusGroup focuses) {
			IRecipeCategory.super.setRecipe(builder, recipe, focuses);

			IRecipeSlotBuilder input = builder.addSlot(RecipeIngredientRole.INPUT, 25, 9);
			input.addItemStacks(this.plugin.getFluidFullItemStacks(recipe.getInput().getFluids()));

			IRecipeSlotBuilder output = builder.addSlot(RecipeIngredientRole.OUTPUT, 91, 39);
			output.addItemStacks(this.plugin.oxygenFullItemStacks);

			IRecipeSlotBuilder tank = builder.addSlot(RecipeIngredientRole.CATALYST, INPUT_TANK_LEFT, INPUT_TANK_TOP);
			tank.setFluidRenderer(1, false, GuiHelper.FLUID_TANK_WIDTH, GuiHelper.FLUID_TANK_HEIGHT).setOverlay(fluidOverlay, 0, 0);
			tank.addIngredients(ForgeTypes.FLUID_STACK, recipe.getInput().toStacks());
		}

		@Override
		public void draw(OxygenLoaderRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
			IRecipeCategory.super.draw(recipe, recipeSlotsView, stack, mouseX, mouseY);

			this.cachedEnergies.getUnchecked(200).draw(stack, ENERGY_LEFT, ENERGY_TOP);
			GuiHelper.drawOxygenTank(stack, OUTPUT_TANK_LEFT, OUTPUT_TANK_TOP, 1.0D);
		}

		@Override
		public List<Component> getTooltipStrings(OxygenLoaderRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
			if (GuiHelper.isHover(this.getEnergyBounds(), mouseX, mouseY)) {
				return Collections.singletonList(GaugeTextHelper.getUsingPerTickText(GaugeValueHelper.getEnergy(Config.FUEL_REFINERY_ENERGY_USAGE.get())).build());
			} else if (GuiHelper.isHover(this.getOutputTankBounds(), mouseX, mouseY)) {
				return Collections.singletonList(GaugeTextHelper.getValueText(GaugeValueHelper.getOxygen(recipe.getOxygen())).build());
			}

			return Collections.emptyList();
		}

		@Override
		public RecipeType<OxygenLoaderRecipe> getRecipeType() {
			return recipeType;
		}

		@Override
		public ResourceLocation getUid() {
			return Uid;
		}

		@Override
		public Class<? extends OxygenLoaderRecipe> getRecipeClass() {
			return OxygenLoaderRecipe.class;
		}

		@Override
		public Component getTitle() {
			return this.title;
		}

		@Override
		public IDrawable getBackground() {
			return this.background;
		}

		@Override
		public IDrawable getIcon() {
			return null;
		}

		public Rectangle2d getInputTankBounds() {
			return GuiHelper.getFluidTankBounds(INPUT_TANK_LEFT, INPUT_TANK_TOP);
		}

		public Rectangle2d getOutputTankBounds() {
			return GuiHelper.getFluidTankBounds(OUTPUT_TANK_LEFT, OUTPUT_TANK_TOP);
		}

		public Rectangle2d getEnergyBounds() {
			return GuiHelper.getEnergyBounds(ENERGY_LEFT, ENERGY_TOP);
		}
	}

	public static class OxygenBubbleDistributorJeiCategory implements IRecipeCategory<OxygenBubbleDistributorRecipe> {
		public static final ResourceLocation Uid = new ResourceLocation(BeyondEarthMod.MODID, "oxygen_bubble_distributor");
		public static final RecipeType recipeType = new RecipeType<>(new ResourceLocation(BeyondEarthMod.MODID, "oxygen_bubble_distributor"), OxygenBubbleDistributorRecipe.class);

		public static final ResourceLocation BACKGROUND = new ResourceLocation(BeyondEarthMod.MODID, "textures/jei/oxygen_bubble_distributor.png");
		private static final Component title = new TranslatableComponent("container." + BeyondEarthMod.MODID + ".oxygen_bubble_distributor");

		public static final int INPUT_TANK_LEFT = 8;
		public static final int INPUT_TANK_TOP = 8;
		public static final int OUTPUT_TANK_LEFT = 74;
		public static final int OUTPUT_TANK_TOP = 8;
		public static final int ENERGY_LEFT = 114;
		public static final int ENERGY_TOP = 8;

		private final JeiPlugin plugin;
		private final IDrawableStatic background;
		private final IDrawable fluidOverlay;
		private final LoadingCache<Integer, IDrawableAnimated> cachedEnergies;

		public OxygenBubbleDistributorJeiCategory(JeiPlugin plugin, IGuiHelper guiHelper) {
			this.plugin = plugin;
			this.background = guiHelper.drawableBuilder(BACKGROUND, 0, 0, 147, 64).setTextureSize(147, 64).build();
			this.fluidOverlay = guiHelper.drawableBuilder(GuiHelper.FLUID_TANK_PATH, 0, 0, GuiHelper.FLUID_TANK_WIDTH, GuiHelper.FLUID_TANK_HEIGHT).setTextureSize(GuiHelper.FLUID_TANK_WIDTH, GuiHelper.FLUID_TANK_HEIGHT).build();
			this.cachedEnergies = createUsingEnergies(guiHelper);
		}

		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, OxygenBubbleDistributorRecipe recipe, IFocusGroup focuses) {
			IRecipeCategory.super.setRecipe(builder, recipe, focuses);

			IRecipeSlotBuilder input = builder.addSlot(RecipeIngredientRole.INPUT, 25, 9);
			input.addItemStacks(this.plugin.getFluidFullItemStacks(recipe.getInput().getFluids()));

			IRecipeSlotBuilder tank = builder.addSlot(RecipeIngredientRole.CATALYST, INPUT_TANK_LEFT, INPUT_TANK_TOP);
			tank.setFluidRenderer(1, false, GuiHelper.FLUID_TANK_WIDTH, GuiHelper.FLUID_TANK_HEIGHT).setOverlay(fluidOverlay, 0, 0);
			tank.addIngredients(ForgeTypes.FLUID_STACK, recipe.getInput().toStacks());
		}

		@Override
		public void draw(OxygenBubbleDistributorRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
			IRecipeCategory.super.draw(recipe, recipeSlotsView, stack, mouseX, mouseY);

			this.cachedEnergies.getUnchecked(200).draw(stack, ENERGY_LEFT, ENERGY_TOP);
			GuiHelper.drawOxygenTank(stack, OUTPUT_TANK_LEFT, OUTPUT_TANK_TOP, 1.0D);
		}

		@Override
		public List<Component> getTooltipStrings(OxygenBubbleDistributorRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
			if (GuiHelper.isHover(this.getEnergyBounds(), mouseX, mouseY)) {
				return Collections.singletonList(GaugeTextHelper.getUsingPerTickText(GaugeValueHelper.getEnergy(Config.FUEL_REFINERY_ENERGY_USAGE.get())).build());
			} else if (GuiHelper.isHover(this.getOutputTankBounds(), mouseX, mouseY)) {
				return Collections.singletonList(GaugeTextHelper.getValueText(GaugeValueHelper.getOxygen(recipe.getOxygen())).build());
			}

			return Collections.emptyList();
		}

		@Override
		public RecipeType<OxygenBubbleDistributorRecipe> getRecipeType() {
			return recipeType;
		}

		@Override
		public ResourceLocation getUid() {
			return Uid;
		}

		@Override
		public Class<? extends OxygenBubbleDistributorRecipe> getRecipeClass() {
			return OxygenBubbleDistributorRecipe.class;
		}

		@Override
		public Component getTitle() {
			return this.title;
		}

		@Override
		public IDrawable getBackground() {
			return this.background;
		}

		@Override
		public IDrawable getIcon() {
			return null;
		}

		public Rectangle2d getInputTankBounds() {
			return GuiHelper.getFluidTankBounds(INPUT_TANK_LEFT, INPUT_TANK_TOP);
		}

		public Rectangle2d getOutputTankBounds() {
			return GuiHelper.getFluidTankBounds(OUTPUT_TANK_LEFT, OUTPUT_TANK_TOP);
		}

		public Rectangle2d getEnergyBounds() {
			return GuiHelper.getEnergyBounds(ENERGY_LEFT, ENERGY_TOP);
		}
	}

	public static class CoalGeneratorJeiCategory implements IRecipeCategory<GeneratingRecipe> {
		public static final ResourceLocation Uid = new ResourceLocation(BeyondEarthMod.MODID, "coal_generator");
		public static final RecipeType recipeType = new RecipeType<>(new ResourceLocation(BeyondEarthMod.MODID, "coal_generator"), GeneratingRecipe.class);

		public static final ResourceLocation BACKGROUND = new ResourceLocation(BeyondEarthMod.MODID, "textures/jei/coal_generator.png");
		private static final Component title = new TranslatableComponent("container." + BeyondEarthMod.MODID + ".coal_generator");

		public static final int FIRE_LEFT = 45;
		public static final int FIRE_TOP = 45;
		public static final int ENERGY_LEFT = 103;
		public static final int ENERGY_TOP = 15;

		private final IDrawableStatic background;
		private final LoadingCache<Integer, IDrawableAnimated> fires;
		private final LoadingCache<Integer, IDrawableAnimated> energies;

		public CoalGeneratorJeiCategory(IGuiHelper guiHelper) {
			this.background = guiHelper.drawableBuilder(BACKGROUND, 0, 0, 144, 84).setTextureSize(144, 84).build();
			this.fires = createFires(guiHelper);
			this.energies = createGeneratingEnergies(guiHelper);
		}

		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, GeneratingRecipe recipe, IFocusGroup focuses) {
			IRecipeCategory.super.setRecipe(builder, recipe, focuses);

			IRecipeSlotBuilder input = builder.addSlot(RecipeIngredientRole.INPUT, 45, 26);
			input.addIngredients(recipe.getInput());
		}

		@Override
		public void draw(GeneratingRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
			IRecipeCategory.super.draw(recipe, recipeSlotsView, stack, mouseX, mouseY);

			int burnTime = recipe.getBurnTime();
			this.fires.getUnchecked(burnTime).draw(stack, FIRE_LEFT, FIRE_TOP);
			this.energies.getUnchecked(200).draw(stack, ENERGY_LEFT, ENERGY_TOP);
			drawTextTime(stack, this.getBackground(), burnTime);
		}

		@Override
		public List<Component> getTooltipStrings(GeneratingRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
			if (GuiHelper.isHover(this.getFireBounds(), mouseX, mouseY)) {
				return Collections.singletonList(GaugeTextHelper.getValueText(GaugeValueHelper.getBurnTime(recipe.getBurnTime())).build());
			} else if (GuiHelper.isHover(this.getEnergyBounds(), mouseX, mouseY)) {
				return Collections.singletonList(GaugeTextHelper.getGeneratingPerTickText(GaugeValueHelper.getEnergy(Config.COAL_GENERATOR_ENERGY_GENERATION.get())).build());
			}
			return Collections.emptyList();
		}

		@Override
		public RecipeType<GeneratingRecipe> getRecipeType() {
			return recipeType;
		}

		@Override
		public ResourceLocation getUid() {
			return Uid;
		}

		@Override
		public Class<? extends GeneratingRecipe> getRecipeClass() {
			return GeneratingRecipe.class;
		}

		@Override
		public Component getTitle() {
			return this.title;
		}

		public Rectangle2d getFireBounds() {
			return GuiHelper.getFireBounds(FIRE_LEFT, FIRE_TOP);
		}

		public Rectangle2d getEnergyBounds() {
			return GuiHelper.getEnergyBounds(ENERGY_LEFT, ENERGY_TOP);
		}

		@Override
		public IDrawable getBackground() {
			return this.background;
		}

		@Override
		public IDrawable getIcon() {
			return null;
		}
	}

	public static class NasaWorkbenchJeiCategory implements IRecipeCategory<WorkbenchingRecipe> {
		public static final ResourceLocation Uid = new ResourceLocation(BeyondEarthMod.MODID, "nasa_workbench");
		public static final RecipeType recipeType = new RecipeType<>(new ResourceLocation(BeyondEarthMod.MODID, "nasa_workbench"), WorkbenchingRecipe.class);

		public static final ResourceLocation BACKGROUND = new ResourceLocation(BeyondEarthMod.MODID, "textures/jei/nasa_workbench.png");
		private static final Component title = new TranslatableComponent("container." + BeyondEarthMod.MODID + ".nasa_workbench");

		private final IDrawableStatic background;

		public NasaWorkbenchJeiCategory(IGuiHelper guiHelper) {
			this.background = guiHelper.drawableBuilder(BACKGROUND, 0, 0, 176, 122).setTextureSize(176, 122).build();
		}

		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, WorkbenchingRecipe recipe, IFocusGroup focuses) {
			IRecipeCategory.super.setRecipe(builder, recipe, focuses);

			builder.moveRecipeTransferButton(background.getWidth() - 20, background.getHeight() - 20);

			GridPlacer placer = new GridPlacer();
			placeRocketParts(39, 8, 1, placer::placeBottom, RocketPartsRegistry.ROCKET_PART_NOSE.get(), builder, recipe);
			placeRocketParts(30, 26, 2, placer::placeBottom, RocketPartsRegistry.ROCKET_PART_BODY.get(), builder, recipe);
			placeRocketParts(30, 80, 1, placer::placeRight, RocketPartsRegistry.ROCKET_PART_TANK.get(), builder, recipe);
			placeRocketParts(12, 80, 1, placer::placeBottom, RocketPartsRegistry.ROCKET_PART_FIN_LEFT.get(), builder, recipe);
			placeRocketParts(66, 80, 1, placer::placeBottom, RocketPartsRegistry.ROCKET_PART_FIN_RIGHT.get(), builder, recipe);
			placeRocketParts(39, 98, 1, placer::placeBottom, RocketPartsRegistry.ROCKET_PART_ENGINE.get(), builder, recipe);

			IRecipeSlotBuilder output = builder.addSlot(RecipeIngredientRole.OUTPUT, 127, 73);
			output.addItemStack(recipe.getOutput());
		}

		@Override
		public RecipeType<WorkbenchingRecipe> getRecipeType() {
			return recipeType;
		}

		@Override
		public ResourceLocation getUid() {
			return Uid;
		}

		@Override
		public Class<? extends WorkbenchingRecipe> getRecipeClass() {
			return WorkbenchingRecipe.class;
		}

		@Override
		public Component getTitle() {
			return title;
		}

		@Override
		public IDrawable getBackground() {
			return background;
		}

		@Override
		public IDrawable getIcon() {
			return null;
		}
	}

	public static void placeRocketParts(int left, int top, int mod, IPlacer placer, RocketPart part, IRecipeLayoutBuilder builder, WorkbenchingRecipe recipe) {
		List<Ingredient> ingredients = recipe.getParts().get(part);

		RocketPartGridPlacer.place(left, top, mod, placer, part, (i, bounds) -> {
			Ingredient ingredient = (ingredients != null && i < ingredients.size()) ? ingredients.get(i) : Ingredient.EMPTY;
			IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.INPUT, bounds.getX(), bounds.getY());
			slot.addIngredients(ingredient);
		});
	}

	public static IDrawableStatic createFireStatic(IGuiHelper guiHelper) {
		return drawableBuilder(guiHelper, GuiHelper.FIRE_PATH, GuiHelper.FIRE_WIDTH, GuiHelper.FIRE_HEIGHT).build();
	}

	public static IDrawableAnimated createFireAnimated(IGuiHelper guiHelper) {
		return createFireAnimated(guiHelper, 200);
	}

	public static IDrawableAnimated createFireAnimated(IGuiHelper guiHelper, int ticks) {
		return createFireAnimated(guiHelper, createFireStatic(guiHelper), ticks);
	}

	public static IDrawableAnimated createFireAnimated(IGuiHelper guiHelper, IDrawableStatic fireStatic, int ticks) {
		return guiHelper.createAnimatedDrawable(fireStatic, ticks, IDrawableAnimated.StartDirection.TOP, true);
	}

	public static IDrawableBuilder drawableBuilder(IGuiHelper guiHelper, ResourceLocation path, int width, int height) {
		return guiHelper.drawableBuilder(path, 0, 0, width, height).setTextureSize(width, height);
	}

	public static LoadingCache<Integer, IDrawableAnimated> createFires(IGuiHelper guiHelper) {
		return CacheBuilder.newBuilder().build(new CacheLoader<Integer, IDrawableAnimated>() {
			@Override
			public IDrawableAnimated load(Integer time) {
				return drawableBuilder(guiHelper, GuiHelper.FIRE_PATH, GuiHelper.FIRE_WIDTH, GuiHelper.FIRE_HEIGHT).buildAnimated(time, IDrawableAnimated.StartDirection.TOP, true);
			}
		});
	}

	public static LoadingCache<Integer, IDrawableAnimated> createArrows(IGuiHelper guiHelper) {
		return CacheBuilder.newBuilder().build(new CacheLoader<Integer, IDrawableAnimated>() {
			@Override
			public IDrawableAnimated load(Integer time) {
				return drawableBuilder(guiHelper, GuiHelper.ARROW_PATH, GuiHelper.ARROW_WIDTH, GuiHelper.ARROW_HEIGHT).buildAnimated(time, IDrawableAnimated.StartDirection.LEFT, false);
			}
		});
	}

	public static LoadingCache<Integer, IDrawableAnimated> createEnergies(IGuiHelper guiHelper, boolean inverted) {
		return CacheBuilder.newBuilder().build(new CacheLoader<Integer, IDrawableAnimated>() {
			@Override
			public IDrawableAnimated load(Integer time) {
				return drawableBuilder(guiHelper, GuiHelper.ENERGY_PATH, GuiHelper.ENERGY_WIDTH, GuiHelper.ENERGY_HEIGHT).buildAnimated(time, inverted ? IDrawableAnimated.StartDirection.TOP : IDrawableAnimated.StartDirection.BOTTOM, inverted);
			}
		});

	}

	public static LoadingCache<Integer, IDrawableAnimated> createUsingEnergies(IGuiHelper guiHelper) {
		return createEnergies(guiHelper, true);
	}

	public static LoadingCache<Integer, IDrawableAnimated> createGeneratingEnergies(IGuiHelper guiHelper) {
		return createEnergies(guiHelper, false);
	}

	public static void drawText(PoseStack stack, IDrawable background, String text) {
		Minecraft mc = Minecraft.getInstance();
		Font font = mc.font;
		int stringWidth = font.width(text);
		font.draw(stack, text, background.getWidth() - 5 - stringWidth, background.getHeight() - font.lineHeight - 5, 0x808080);
	}

	public static void drawTextTime(PoseStack stack, IDrawable background, int ticks) {
		NumberFormat numberInstance = NumberFormat.getNumberInstance();
		numberInstance.setMaximumFractionDigits(2);
		String text = numberInstance.format(ticks / 20.0F) + "s";

		drawText(stack, background, text);
	}

	/** TIER 1 ROCKET */
	public static class RocketTier1JeiCategory implements IRecipeCategory<FuelLoadingRecipe> {
		public static final ResourceLocation Uid = new ResourceLocation(BeyondEarthMod.MODID, "rocket_t1");
		public static final RecipeType recipeType = new RecipeType<>(new ResourceLocation(BeyondEarthMod.MODID, "rocket_t1"), FuelLoadingRecipe.class);

		public static final ResourceLocation BACKGROUND = new ResourceLocation(BeyondEarthMod.MODID, "textures/jei/rocket_gui.png");
		private static final Component title = EntitiesRegistry.TIER_1_ROCKET.get().getDescription();

		private final IDrawableStatic background;
		private final IDrawable icon;

		public RocketTier1JeiCategory(IGuiHelper guiHelper) {
			this.background = guiHelper.drawableBuilder(BACKGROUND, 0, 0, 128, 71).setTextureSize(128, 71).build();
			this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ItemsRegistry.TIER_1_ROCKET_ITEM.get()));
		}

		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, FuelLoadingRecipe recipe, IFocusGroup focuses) {
			IRecipeCategory.super.setRecipe(builder, recipe, focuses);

			IRecipeSlotBuilder input = builder.addSlot(RecipeIngredientRole.INPUT, 14, 19);
			input.addItemStacks(recipe.getFuelTagBuckets());

			int capacity = FluidUtil2.BUCKET_SIZE * Config.ROCKET_TIER_1_FUEL_BUCKETS.get();
			IRecipeSlotBuilder tank = builder.addSlot(RecipeIngredientRole.CATALYST, 66, 12);
			tank.setFluidRenderer(capacity, true, 46, 46);
			tank.addIngredients(ForgeTypes.FLUID_STACK, recipe.getFluidStacks(capacity));
		}

		@Override
		public ResourceLocation getUid() {
			return Uid;
		}

		@Override
		public Class<? extends FuelLoadingRecipe> getRecipeClass() {
			return FuelLoadingRecipe.class;
		}

		@Override
		public RecipeType getRecipeType() {
			return recipeType;
		}

		@Override
		public Component getTitle() {
			return this.title;
		}

		@Override
		public IDrawable getBackground() {
			return background;
		}

		@Override
		public IDrawable getIcon() {
			return this.icon;
		}
	}

	public static class RocketTier2JeiCategory implements IRecipeCategory<FuelLoadingRecipe> {
		public static final ResourceLocation Uid = new ResourceLocation(BeyondEarthMod.MODID, "rocket_t2");
		public static final RecipeType recipeType = new RecipeType<>(new ResourceLocation(BeyondEarthMod.MODID, "rocket_t2"), FuelLoadingRecipe.class);

		public static final ResourceLocation BACKGROUND = new ResourceLocation(BeyondEarthMod.MODID, "textures/jei/rocket_gui.png");
		private static final Component title = EntitiesRegistry.TIER_2_ROCKET.get().getDescription();

		private final IDrawableStatic background;
		private final IDrawable icon;

		public RocketTier2JeiCategory(IGuiHelper guiHelper) {
			this.background = guiHelper.drawableBuilder(BACKGROUND, 0, 0, 128, 71).setTextureSize(128, 71).build();
			this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ItemsRegistry.TIER_2_ROCKET_ITEM.get()));
		}

		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, FuelLoadingRecipe recipe, IFocusGroup focuses) {
			IRecipeCategory.super.setRecipe(builder, recipe, focuses);

			IRecipeSlotBuilder input = builder.addSlot(RecipeIngredientRole.INPUT, 14, 19);
			input.addItemStacks(recipe.getFuelTagBuckets());

			int capacity = FluidUtil2.BUCKET_SIZE * Config.ROCKET_TIER_2_FUEL_BUCKETS.get();
			IRecipeSlotBuilder tank = builder.addSlot(RecipeIngredientRole.CATALYST, 66, 12);
			tank.setFluidRenderer(capacity, true, 46, 46);
			tank.addIngredients(ForgeTypes.FLUID_STACK, recipe.getFluidStacks(capacity));
		}

		@Override
		public RecipeType<FuelLoadingRecipe> getRecipeType() {
			return recipeType;
		}

		@Override
		public ResourceLocation getUid() {
			return Uid;
		}

		@Override
		public Class<? extends FuelLoadingRecipe> getRecipeClass() {
			return FuelLoadingRecipe.class;
		}

		@Override
		public Component getTitle() {
			return this.title;
		}

		@Override
		public IDrawable getBackground() {
			return background;
		}

		@Override
		public IDrawable getIcon() {
			return this.icon;
		}
	}

	public static class RocketTier3JeiCategory implements IRecipeCategory<FuelLoadingRecipe> {
		public static final ResourceLocation Uid = new ResourceLocation(BeyondEarthMod.MODID, "rocket_t3");
		public static final RecipeType recipeType = new RecipeType<>(new ResourceLocation(BeyondEarthMod.MODID, "rocket_t3"), FuelLoadingRecipe.class);

		public static final ResourceLocation BACKGROUND = new ResourceLocation(BeyondEarthMod.MODID, "textures/jei/rocket_gui.png");
		private static final Component title = EntitiesRegistry.TIER_3_ROCKET.get().getDescription();

		private final IDrawableStatic background;
		private final IDrawable icon;

		public RocketTier3JeiCategory(IGuiHelper guiHelper) {
			this.background = guiHelper.drawableBuilder(BACKGROUND, 0, 0, 128, 71).setTextureSize(128, 71).build();
			this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ItemsRegistry.TIER_3_ROCKET_ITEM.get()));
		}

		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, FuelLoadingRecipe recipe, IFocusGroup focuses) {
			IRecipeCategory.super.setRecipe(builder, recipe, focuses);

			IRecipeSlotBuilder input = builder.addSlot(RecipeIngredientRole.INPUT, 14, 19);
			input.addItemStacks(recipe.getFuelTagBuckets());

			int capacity = FluidUtil2.BUCKET_SIZE * Config.ROCKET_TIER_3_FUEL_BUCKETS.get();
			IRecipeSlotBuilder tank = builder.addSlot(RecipeIngredientRole.CATALYST, 66, 12);
			tank.setFluidRenderer(capacity, true, 46, 46);
			tank.addIngredients(ForgeTypes.FLUID_STACK, recipe.getFluidStacks(capacity));
		}

		@Override
		public RecipeType<FuelLoadingRecipe> getRecipeType() {
			return recipeType;
		}

		@Override
		public ResourceLocation getUid() {
			return Uid;
		}

		@Override
		public Class<? extends FuelLoadingRecipe> getRecipeClass() {
			return FuelLoadingRecipe.class;
		}

		@Override
		public Component getTitle() {
			return this.title;
		}

		@Override
		public IDrawable getBackground() {
			return background;
		}

		@Override
		public IDrawable getIcon() {
			return this.icon;
		}
	}

	public static class RocketTier4JeiCategory implements IRecipeCategory<FuelLoadingRecipe> {
		public static final ResourceLocation Uid = new ResourceLocation(BeyondEarthMod.MODID, "rocket_t4");
		public static final RecipeType recipeType = new RecipeType<>(new ResourceLocation(BeyondEarthMod.MODID, "rocket_t4"), FuelLoadingRecipe.class);

		public static final ResourceLocation BACKGROUND = new ResourceLocation(BeyondEarthMod.MODID, "textures/jei/rocket_gui.png");
		private static final Component title = EntitiesRegistry.TIER_4_ROCKET.get().getDescription();

		private final IDrawableStatic background;
		private final IDrawable icon;

		public RocketTier4JeiCategory(IGuiHelper guiHelper) {
			this.background = guiHelper.drawableBuilder(BACKGROUND, 0, 0, 128, 71).setTextureSize(128, 71).build();
			this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ItemsRegistry.TIER_4_ROCKET_ITEM.get()));
		}

		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, FuelLoadingRecipe recipe, IFocusGroup focuses) {
			IRecipeCategory.super.setRecipe(builder, recipe, focuses);

			IRecipeSlotBuilder input = builder.addSlot(RecipeIngredientRole.INPUT, 14, 19);
			input.addItemStacks(recipe.getFuelTagBuckets());

			int capacity = FluidUtil2.BUCKET_SIZE * Config.ROCKET_TIER_4_FUEL_BUCKETS.get();
			IRecipeSlotBuilder tank = builder.addSlot(RecipeIngredientRole.CATALYST, 66, 12);
			tank.setFluidRenderer(capacity, true, 46, 46);
			tank.addIngredients(ForgeTypes.FLUID_STACK, recipe.getFluidStacks(capacity));
		}

		@Override
		public RecipeType<FuelLoadingRecipe> getRecipeType() {
			return recipeType;
		}

		@Override
		public ResourceLocation getUid() {
			return Uid;
		}

		@Override
		public Class<? extends FuelLoadingRecipe> getRecipeClass() {
			return FuelLoadingRecipe.class;
		}

		@Override
		public Component getTitle() {
			return this.title;
		}

		@Override
		public IDrawable getBackground() {
			return background;
		}

		@Override
		public IDrawable getIcon() {
			return this.icon;
		}
	}

	public static class CompressorJeiCategory implements IRecipeCategory<CompressingRecipe> {
		public static final ResourceLocation Uid = new ResourceLocation(BeyondEarthMod.MODID, "compressor");
		public static final RecipeType recipeType = new RecipeType<>(new ResourceLocation(BeyondEarthMod.MODID, "compressor"), CompressingRecipe.class);

		public static final ResourceLocation BACKGROUND = new ResourceLocation(BeyondEarthMod.MODID, "textures/jei/compressor.png");
		private static final Component title = new TranslatableComponent("container." + BeyondEarthMod.MODID + ".compressor");

		public static final int ARROW_LEFT = 36;
		public static final int ARROW_TOP = 29;
		public static final int ENERGY_LEFT = 103;
		public static final int ENERGY_TOP = 15;

		private final IDrawableStatic background;
		private final LoadingCache<Integer, IDrawableAnimated> cachedArrows;
		private final LoadingCache<Integer, IDrawableAnimated> cachedEnergies;

		public CompressorJeiCategory(IGuiHelper guiHelper) {
			this.background = guiHelper.drawableBuilder(BACKGROUND, 0, 0, 144, 84).setTextureSize(144, 84).build();
			this.cachedArrows = createArrows(guiHelper);
			this.cachedEnergies = createUsingEnergies(guiHelper);
		}

		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, CompressingRecipe recipe, IFocusGroup focuses) {
			IRecipeCategory.super.setRecipe(builder, recipe, focuses);

			IRecipeSlotBuilder input = builder.addSlot(RecipeIngredientRole.INPUT, 15, 30);
			input.addIngredients(recipe.getInput());

			IRecipeSlotBuilder output = builder.addSlot(RecipeIngredientRole.OUTPUT, 70, 29);
			output.addItemStack(recipe.getOutput());
		}

		@Override
		public void draw(CompressingRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
			IRecipeCategory.super.draw(recipe, recipeSlotsView, stack, mouseX, mouseY);

			int cookTime = recipe.getCookTime();
			this.cachedArrows.getUnchecked(cookTime).draw(stack, ARROW_LEFT, ARROW_TOP);
			this.cachedEnergies.getUnchecked(cookTime).draw(stack, ENERGY_LEFT, ENERGY_TOP);
			drawTextTime(stack, this.getBackground(), cookTime);
		}

		@Override
		public List<Component> getTooltipStrings(CompressingRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
			if (GuiHelper.isHover(this.getEnergyBounds(), mouseX, mouseY)) {
				return Collections.singletonList((GaugeTextHelper.getUsingPerTickText(GaugeValueHelper.getEnergy(Config.COMPRESSOR_ENERGY_USAGE.get())).build()));
			} else {
				return Collections.emptyList();
			}
		}

		private Rectangle2d getEnergyBounds() {
			return GuiHelper.getEnergyBounds(ENERGY_LEFT, ENERGY_TOP);
		}

		@Override
		public RecipeType<CompressingRecipe> getRecipeType() {
			return recipeType;
		}

		@Override
		public ResourceLocation getUid() {
			return Uid;
		}

		@Override
		public Class<? extends CompressingRecipe> getRecipeClass() {
			return CompressingRecipe.class;
		}

		@Override
		public Component getTitle() {
			return this.title;
		}

		@Override
		public IDrawable getBackground() {
			return this.background;
		}

		@Override
		public IDrawable getIcon() {
			return null;
		}
	}

	public static class FuelRefineryJeiCategory implements IRecipeCategory<FuelRefiningRecipe> {
		public static final ResourceLocation Uid = new ResourceLocation(BeyondEarthMod.MODID, "fuel_refinery");
		public static final RecipeType recipeType = new RecipeType<>(new ResourceLocation(BeyondEarthMod.MODID, "fuel_refinery"), FuelRefiningRecipe.class);

		public static final ResourceLocation BACKGROUND = new ResourceLocation(BeyondEarthMod.MODID, "textures/jei/fuel_refinery.png");
		private static final Component title = new TranslatableComponent("container." + BeyondEarthMod.MODID + ".fuel_refinery");

		public static final int INPUT_TANK_LEFT = 8;
		public static final int INPUT_TANK_TOP = 8;
		public static final int OUTPUT_TANK_LEFT = 74;
		public static final int OUTPUT_TANK_TOP = 8;
		public static final int ENERGY_LEFT = 114;
		public static final int ENERGY_TOP = 8;

		private final JeiPlugin plugin;
		private final IDrawableStatic background;
		private final IDrawable fluidOverlay;
		private final LoadingCache<Integer, IDrawableAnimated> cachedEnergies;

		public FuelRefineryJeiCategory(JeiPlugin plugin, IGuiHelper guiHelper) {
			this.plugin = plugin;
			this.background = guiHelper.drawableBuilder(BACKGROUND, 0, 0, 147, 64).setTextureSize(147, 64).build();
			this.fluidOverlay = guiHelper.drawableBuilder(GuiHelper.FLUID_TANK_PATH, 0, 0, GuiHelper.FLUID_TANK_WIDTH, GuiHelper.FLUID_TANK_HEIGHT).setTextureSize(GuiHelper.FLUID_TANK_WIDTH, GuiHelper.FLUID_TANK_HEIGHT).build();
			this.cachedEnergies = createUsingEnergies(guiHelper);
		}

		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, FuelRefiningRecipe recipe, IFocusGroup focuses) {
			IRecipeCategory.super.setRecipe(builder, recipe, focuses);

			IRecipeSlotBuilder inputItem = builder.addSlot(RecipeIngredientRole.INPUT, 25, 9);
			inputItem.addItemStacks(this.plugin.getFluidFullItemStacks(recipe.getInput().getFluids()));

			IRecipeSlotBuilder outputItem = builder.addSlot(RecipeIngredientRole.OUTPUT, 91, 39);
			outputItem.addItemStacks(this.plugin.getFluidFullItemStacks(recipe.getOutput().getFluids()));

			IRecipeSlotBuilder inputTank = builder.addSlot(RecipeIngredientRole.CATALYST, INPUT_TANK_LEFT, INPUT_TANK_TOP);
			inputTank.setFluidRenderer(1, false, GuiHelper.FLUID_TANK_WIDTH, GuiHelper.FLUID_TANK_HEIGHT).setOverlay(fluidOverlay, 0, 0);
			inputTank.addIngredients(ForgeTypes.FLUID_STACK, recipe.getInput().toStacks());

			IRecipeSlotBuilder outputTank = builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_TANK_LEFT, OUTPUT_TANK_TOP);
			outputTank.setFluidRenderer(1, false, GuiHelper.FLUID_TANK_WIDTH, GuiHelper.FLUID_TANK_HEIGHT).setOverlay(fluidOverlay, 0, 0);
			outputTank.addIngredients(ForgeTypes.FLUID_STACK, recipe.getOutput().toStacks());
		}

		@Override
		public void draw(FuelRefiningRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
			IRecipeCategory.super.draw(recipe, recipeSlotsView, stack, mouseX, mouseY);

			this.cachedEnergies.getUnchecked(200).draw(stack, ENERGY_LEFT, ENERGY_TOP);
		}

		@Override
		public List<Component> getTooltipStrings(FuelRefiningRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
			if (GuiHelper.isHover(this.getEnergyBounds(), mouseX, mouseY)) {
				return Collections.singletonList(GaugeTextHelper.getUsingPerTickText(GaugeValueHelper.getEnergy(Config.FUEL_REFINERY_ENERGY_USAGE.get())).build());
			} else {
				return Collections.emptyList();
			}
		}

		@Override
		public RecipeType<FuelRefiningRecipe> getRecipeType() {
			return recipeType;
		}

		@Override
		public ResourceLocation getUid() {
			return Uid;
		}

		@Override
		public Class<? extends FuelRefiningRecipe> getRecipeClass() {
			return FuelRefiningRecipe.class;
		}

		@Override
		public Component getTitle() {
			return this.title;
		}

		@Override
		public IDrawable getBackground() {
			return this.background;
		}

		@Override
		public IDrawable getIcon() {
			return null;
		}

		public Rectangle2d getInputTankBounds() {
			return GuiHelper.getFluidTankBounds(INPUT_TANK_LEFT, INPUT_TANK_TOP);
		}

		public Rectangle2d getOutputTankBounds() {
			return GuiHelper.getFluidTankBounds(OUTPUT_TANK_LEFT, OUTPUT_TANK_TOP);
		}

		public Rectangle2d getEnergyBounds() {
			return GuiHelper.getEnergyBounds(ENERGY_LEFT, ENERGY_TOP);
		}
	}

	public static class RoverJeiCategory implements IRecipeCategory<FuelLoadingRecipe> {
		public static final ResourceLocation Uid = new ResourceLocation(BeyondEarthMod.MODID, "rover");
		public static final RecipeType recipeType = new RecipeType<>(new ResourceLocation(BeyondEarthMod.MODID, "rover"), FuelLoadingRecipe.class);

		public static final ResourceLocation BACKGROUND = new ResourceLocation(BeyondEarthMod.MODID, "textures/jei/rover.png");
		private static final Component title = EntitiesRegistry.ROVER.get().getDescription();

		private final IDrawableStatic background;
		private final IDrawable fluidOverlay;

		public RoverJeiCategory(IGuiHelper guiHelper) {
			this.background = guiHelper.drawableBuilder(BACKGROUND, 0, 0, 144, 84).setTextureSize(144,84).build();
			this.fluidOverlay = guiHelper.drawableBuilder(GuiHelper.FLUID_TANK_PATH, 0, 0, GuiHelper.FLUID_TANK_WIDTH, GuiHelper.FLUID_TANK_HEIGHT).setTextureSize(GuiHelper.FLUID_TANK_WIDTH, GuiHelper.FLUID_TANK_HEIGHT).build();
		}

		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, FuelLoadingRecipe recipe, IFocusGroup focuses) {
			IRecipeCategory.super.setRecipe(builder, recipe, focuses);

			IRecipeSlotBuilder input = builder.addSlot(RecipeIngredientRole.INPUT, 8, 60);
			input.addItemStacks(recipe.getFuelTagBuckets());

			int capacity = FluidUtil2.BUCKET_SIZE * Config.ROVER_FUEL_BUCKETS.get();
			IRecipeSlotBuilder tank = builder.addSlot(RecipeIngredientRole.INPUT, 9, 8);
			tank.setFluidRenderer(capacity, true, GuiHelper.FLUID_TANK_WIDTH, GuiHelper.FLUID_TANK_HEIGHT).setOverlay(fluidOverlay, 0, 0);
			tank.addIngredients(ForgeTypes.FLUID_STACK, recipe.getFluidStacks(capacity));
		}

		@Override
		public RecipeType<FuelLoadingRecipe> getRecipeType() {
			return recipeType;
		}

		@Override
		public ResourceLocation getUid() {
			return Uid;
		}

		@Override
		public Class<? extends FuelLoadingRecipe> getRecipeClass() {
			return FuelLoadingRecipe.class;
		}

		@Override
		public Component getTitle() {
			return title;
		}

		@Override
		public IDrawable getBackground() {
			return background;
		}

		@Override
		public IDrawable getIcon() {
			return null;
		}
	}

	public static class SpaceStationJeiCategory implements IRecipeCategory<SpaceStationRecipe> {
		public static final ResourceLocation Uid = new ResourceLocation(BeyondEarthMod.MODID, "space_station");
		public static final RecipeType recipeType = new RecipeType<>(new ResourceLocation(BeyondEarthMod.MODID, "space_station"), SpaceStationRecipe.class);

		public static final ResourceLocation BACKGROUND = new ResourceLocation(BeyondEarthMod.MODID, "textures/jei/space_station.png");

		private static final String path = BeyondEarthMod.MODID + ".space_station";
		private static final Component title = new TranslatableComponent("jei.category." + path);
		private static final Component tooltip = new TranslatableComponent("jei.tooltip." + path);

		public static final int SLOTS_X_CENTER = 72;
		public static final int SLOTS_Y_TOP = 6;
		public static final int SLOTS_X_OFFSET = 18;
		public static final int SLOTS_Y_OFFSET = 18;

		private final Component[] tooltips;
		private final IDrawableStatic background;
		private final IDrawable icon;
		private final IDrawable slot;

		public SpaceStationJeiCategory(IGuiHelper guiHelper) {
			this.tooltips = Arrays.stream(tooltip.getString().split("\n")).map(TextComponent::new).toArray(Component[]::new);
			this.background = guiHelper.drawableBuilder(BACKGROUND, 0, 0, 144, 51).setTextureSize(144, 51).build();
			this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ItemsRegistry.SPACE_STATION_ICON.get()));
			this.slot = guiHelper.getSlotDrawable();
		}

		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, SpaceStationRecipe recipe, IFocusGroup focuses) {
			IRecipeCategory.super.setRecipe(builder, recipe, focuses);

			NonNullList<IngredientStack> ingredientStacks = recipe.getIngredientStacks();
			int count = ingredientStacks.size();

			for (int i = 0; i < count; i++) {
				int[] pos = this.getSpaceStationItemPosition(i, count);

				IngredientStack ingredientStack = ingredientStacks.get(i);
				IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.INPUT, pos[0] + 1, pos[1] + 1);
				slot.addItemStacks(Arrays.asList(ingredientStack.getItems()));
			}
		}

		public int[] getSpaceStationItemPosition(int index, int count) {
			int xIndex = index % count;
			int yIndex = index / count;
			int slots_width = count * SLOTS_X_OFFSET;
			int xPosition = SLOTS_X_CENTER + (xIndex * SLOTS_X_OFFSET) - (slots_width / 2);
			int yPosition = SLOTS_Y_TOP + (yIndex * SLOTS_Y_OFFSET);
			return new int[] { xPosition, yPosition };
		}

		@Override
		public void draw(SpaceStationRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
			IRecipeCategory.super.draw(recipe, recipeSlotsView, stack, mouseX, mouseY);
			NonNullList<IngredientStack> ingredientStacks = recipe.getIngredientStacks();
			int count = ingredientStacks.size();

			for (int i = 0; i < count; i++) {
				int[] pos = this.getSpaceStationItemPosition(i, count);
				this.slot.draw(stack, pos[0], pos[1]);
			}

			Minecraft minecraft = Minecraft.getInstance();
			Font font = minecraft.font;
			int tooltipYOffset = this.getSpaceStationItemPosition(ingredientStacks.size() - 1, count)[1] + SLOTS_Y_OFFSET + 4;
			Component[] tooltips = this.getTooltip();

			for (int i = 0; i < tooltips.length; i++) {
				Component tooltip = tooltips[i];
				int tooltipWidth = font.width(tooltip);
				font.draw(stack, tooltip, SLOTS_X_CENTER - (tooltipWidth / 2), tooltipYOffset + font.lineHeight * i, 0xFF404040);
			}
		}

		@Override
		public RecipeType<SpaceStationRecipe> getRecipeType() {
			return recipeType;
		}

		@Override
		public ResourceLocation getUid() {
			return Uid;
		}

		@Override
		public Class<? extends SpaceStationRecipe> getRecipeClass() {
			return SpaceStationRecipe.class;
		}

		@Override
		public Component getTitle() {
			return title;
		}

		public Component[] getTooltip() {
			return this.tooltips;
		}

		@Override
		public IDrawable getBackground() {
			return this.background;
		}

		@Override
		public IDrawable getIcon() {
			return this.icon;
		}
	}
}