package novamachina.exnihilosequentia.common.init;

import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.registries.ObjectHolder;
import novamachina.exnihilosequentia.api.ExNihiloRegistries;
import novamachina.exnihilosequentia.api.crafting.compost.CompostRecipe;
import novamachina.exnihilosequentia.api.crafting.crook.CrookRecipe;
import novamachina.exnihilosequentia.api.crafting.crucible.CrucibleRecipe;
import novamachina.exnihilosequentia.api.crafting.fluiditem.FluidItemRecipe;
import novamachina.exnihilosequentia.api.crafting.fluidontop.FluidOnTopRecipe;
import novamachina.exnihilosequentia.api.crafting.fluidtransform.FluidTransformRecipe;
import novamachina.exnihilosequentia.api.crafting.hammer.HammerRecipe;
import novamachina.exnihilosequentia.api.crafting.heat.HeatRecipe;
import novamachina.exnihilosequentia.api.crafting.sieve.SieveRecipe;
import novamachina.exnihilosequentia.common.compat.top.CompatTOP;
import novamachina.exnihilosequentia.common.item.ore.EnumOre;
import novamachina.exnihilosequentia.common.item.resources.EnumResource;
import novamachina.exnihilosequentia.common.item.seeds.EnumSeed;
import novamachina.exnihilosequentia.common.network.PacketHandler;
import novamachina.exnihilosequentia.common.tileentity.barrel.mode.BarrelModeRegistry;
import novamachina.exnihilosequentia.common.utility.Config;
import novamachina.exnihilosequentia.common.utility.ExNihiloConstants;
import novamachina.exnihilosequentia.common.utility.ExNihiloLogger;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static novamachina.exnihilosequentia.api.datagen.AbstractRecipeGenerator.createMCCompost;

@Mod.EventBusSubscriber(modid = ExNihiloConstants.ModIds.EX_NIHILO_SEQUENTIA, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ExNihiloInitialization {
    @Nonnull public static final ItemGroup ITEM_GROUP = new ItemGroup(ExNihiloConstants.ModIds.EX_NIHILO_SEQUENTIA) {
        @Nonnull
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ExNihiloBlocks.SIEVE_OAK.get());
        }
    };

    @SuppressWarnings("unused")
    @ObjectHolder(ExNihiloConstants.ModIds.EX_NIHILO_SEQUENTIA + ":use_hammer")
    @Nullable public static final GlobalLootModifierSerializer<?> hammerModifier = null;

    @SuppressWarnings("unused")
    @ObjectHolder(ExNihiloConstants.ModIds.EX_NIHILO_SEQUENTIA + ":use_crook")
    @Nullable public static final GlobalLootModifierSerializer<?> crookModifier = null;

    @Nonnull private static final ExNihiloLogger logger = new ExNihiloLogger(LogManager.getLogger());

    private ExNihiloInitialization() {
    }

    // MinecraftForge.EVENT_BUS
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void clearRegistries(@Nonnull final ClientPlayerNetworkEvent.LoggedOutEvent event) {
        logger.debug("Fired LoggedOutEvent");
        ExNihiloRegistries.clearRegistries();
    }

    public static void init(@Nonnull final IEventBus modEventBus) {
        logger.debug("Initializing modded items");
        ExNihiloBlocks.init(modEventBus);
        ExNihiloItems.init(modEventBus);
        ExNihiloTiles.init(modEventBus);
        ExNihiloFluids.init(modEventBus);
        ExNihiloSerializers.init(modEventBus);
    }

    // MinecraftForge.EVENT_BUS
    @SubscribeEvent
    public static void loadClientRecipes(@Nonnull final RecipesUpdatedEvent event) {
        ExNihiloRegistries.clearRegistries();
        loadRecipes(event.getRecipeManager());
    }

    // MinecraftForge.EVENT_BUS
    @SubscribeEvent
    public static void onPlayerLogin(@Nonnull final PlayerEvent.PlayerLoggedInEvent event) {
        logger.debug("Fired PlayerLoggedInEvent");
    }

    // MinecraftForge.EVENT_BUS
    @SubscribeEvent
    public static void onServerStart(@Nonnull final FMLServerStartingEvent event) {
        logger.debug("Fired FMLServerStartingEvent");
        registerOreCompat();
        overrideOres();
        if (event.getServer().isDedicatedServer()) {
            loadRecipes(event.getServer().getRecipeManager());
        }
    }

    // MinecraftForge.EVENT_BUS
    @SubscribeEvent
    public static void registerTOP(@Nonnull final InterModEnqueueEvent event) {
        logger.debug("The One Probe detected: " + ModList.get().isLoaded(ExNihiloConstants.ModIds.TOP));
        if (ModList.get().isLoaded(ExNihiloConstants.ModIds.TOP)) {
            CompatTOP.register();
        }
    }

    // MinecraftForge.EVENT_BUS
    @SubscribeEvent
    public static void setupNonTagBasedRegistries(@Nonnull final FMLCommonSetupEvent event) {
        logger.debug("Fired FMLCommonSetupEvent");
        ExNihiloItems.fillOreIngots();
        BarrelModeRegistry.initialize();
        PacketHandler.registerMessages();
        registerVanillaCompost();
        registerDispenserFluids();
        ExNihiloStats.register();
    }

    private static void registerDispenserFluids() {
        @Nonnull final IDispenseItemBehavior idispenseitembehavior = new DefaultDispenseItemBehavior() {
            @Nonnull private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

            @Nonnull
            public ItemStack execute(@Nonnull final IBlockSource p_82487_1_, @Nonnull final ItemStack p_82487_2_) {
                @Nonnull final BucketItem bucketitem = (BucketItem)p_82487_2_.getItem();
                @Nonnull final BlockPos blockpos = p_82487_1_.getPos().relative(p_82487_1_.getBlockState()
                        .getValue(DispenserBlock.FACING));
                @Nullable final World world = p_82487_1_.getLevel();
                if (bucketitem.emptyBucket(null, world, blockpos, null)) {
                    bucketitem.checkExtraContent(world, p_82487_2_, blockpos);
                    return new ItemStack(Items.BUCKET);
                } else {
                    return this.defaultDispenseItemBehavior.dispense(p_82487_1_, p_82487_2_);
                }
            }
        };
        DispenserBlock.registerBehavior(ExNihiloItems.SEA_WATER_BUCKET.get(), idispenseitembehavior);
        DispenserBlock.registerBehavior(ExNihiloItems.WITCH_WATER_BUCKET.get(), idispenseitembehavior);
    }

    private static void registerVanillaCompost() {
        for (@Nonnull final EnumSeed seed : EnumSeed.values()) {
            @Nullable final RegistryObject<Item> seedRegistryObject = seed.getRegistryObject();
            if (seedRegistryObject != null) {
                createMCCompost(seedRegistryObject.get().asItem(), 0.3F);
            }
        }
        @Nullable final RegistryObject<Item> grassSeedRegistryObject = EnumResource.GRASS_SEED.getRegistryObject();
        if (grassSeedRegistryObject != null) {
            createMCCompost(grassSeedRegistryObject.get().asItem(), 0.3F);
        }
        @Nullable final RegistryObject<Item> ancientSporeRegistryObject = EnumResource.ANCIENT_SPORE.getRegistryObject();
        if (ancientSporeRegistryObject != null) {
            createMCCompost(EnumResource.ANCIENT_SPORE.getRegistryObject().get().asItem(), 0.3F);
        }
		createMCCompost(ExNihiloItems.SILKWORM.get(), 0.3F);
        createMCCompost(ExNihiloItems.COOKED_SILKWORM.get(), 0.3F);
    }

    private static <R extends IRecipe<?>> List<R> filterRecipes(@Nonnull final Collection<IRecipe<?>> recipes,
                                                                @Nonnull final Class<R> recipeClass,
                                                                @Nonnull final IRecipeType<R> recipeType) {
        logger.debug("Filter Recipes, Class: " + recipeClass + ", Recipe Type: " + recipeType);
        return recipes.stream()
                .filter(iRecipe -> iRecipe.getType() == recipeType)
                .map(recipeClass::cast)
                .collect(Collectors.toList());
    }

    private static void loadRecipes(@Nonnull final RecipeManager manager) {
        logger.debug("Loading Recipes");
        @Nonnull final Collection<IRecipe<?>> recipes = manager.getRecipes();
        if (recipes.isEmpty()) {
            return;
        }

        ExNihiloRegistries.HAMMER_REGISTRY
                .setRecipes(filterRecipes(recipes, HammerRecipe.class, HammerRecipe.RECIPE_TYPE));
        ExNihiloRegistries.CROOK_REGISTRY
                .setRecipes(filterRecipes(recipes, CrookRecipe.class, CrookRecipe.RECIPE_TYPE));
        ExNihiloRegistries.COMPOST_REGISTRY
                .setRecipes(filterRecipes(recipes, CompostRecipe.class, CompostRecipe.RECIPE_TYPE));
        ExNihiloRegistries.FLUID_BLOCK_REGISTRY
                .setRecipes(filterRecipes(recipes, FluidItemRecipe.class, FluidItemRecipe.RECIPE_TYPE));
        ExNihiloRegistries.FLUID_ON_TOP_REGISTRY
                .setRecipes(filterRecipes(recipes, FluidOnTopRecipe.class, FluidOnTopRecipe.RECIPE_TYPE));
        ExNihiloRegistries.FLUID_TRANSFORM_REGISTRY
                .setRecipes(filterRecipes(recipes, FluidTransformRecipe.class, FluidTransformRecipe.RECIPE_TYPE));
        ExNihiloRegistries.CRUCIBLE_REGISTRY
                .setRecipes(filterRecipes(recipes, CrucibleRecipe.class, CrucibleRecipe.RECIPE_TYPE));
        ExNihiloRegistries.HEAT_REGISTRY.setRecipes(filterRecipes(recipes, HeatRecipe.class, HeatRecipe.RECIPE_TYPE));
        ExNihiloRegistries.SIEVE_REGISTRY.setRecipes(filterRecipes(recipes, SieveRecipe.class, SieveRecipe.RECIPE_TYPE));
    }

    private static void overrideOres() {
        if (Config.enableOreOverride()) {
            EnumOre.COPPER.setEnabled(Config.enableCopper());
            EnumOre.LEAD.setEnabled(Config.enableLead());
            EnumOre.NICKEL.setEnabled(Config.enableNickel());
            EnumOre.SILVER.setEnabled(Config.enableSilver());
            EnumOre.TIN.setEnabled(Config.enableTin());
            EnumOre.ALUMINUM.setEnabled(Config.enableAluminum());
            EnumOre.PLATINUM.setEnabled(Config.enablePlatinum());
            EnumOre.URANIUM.setEnabled(Config.enableUranium());
            EnumOre.ZINC.setEnabled(Config.enableZinc());
            EnumOre.IRON.setEnabled(Config.enableIron());
            EnumOre.GOLD.setEnabled(Config.enableGold());
        }
    }

    private static void registerOreCompat() {
        logger.debug("Register ore compatibility");

        EnumOre.IRON.setEnabled(true);
        EnumOre.GOLD.setEnabled(true);

        logger
                .debug("Immersive Engineering detected: " + ModList.get().isLoaded(ExNihiloConstants.ModIds.IMMERSIVE_ENGINEERING));
        if (ModList.get().isLoaded(ExNihiloConstants.ModIds.IMMERSIVE_ENGINEERING)) {
            logger.debug("Added Immersive Engineering");
            EnumOre.ALUMINUM.setEnabled(true);
            EnumOre.COPPER.setEnabled(true);
            EnumOre.SILVER.setEnabled(true);
            EnumOre.NICKEL.setEnabled(true);
            EnumOre.LEAD.setEnabled(true);
            EnumOre.URANIUM.setEnabled(true);
        }
        logger.debug("Create detected: " + ModList.get().isLoaded(ExNihiloConstants.ModIds.CREATE));
        if (ModList.get().isLoaded(ExNihiloConstants.ModIds.CREATE)) {
            logger.debug("Added Create");
            EnumOre.COPPER.setEnabled(true);
            EnumOre.ZINC.setEnabled(true);
        }
    }
}
