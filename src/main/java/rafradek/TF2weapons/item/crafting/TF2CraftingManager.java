package rafradek.TF2weapons.item.crafting;

import net.minecraft.tags.ItemTags;
import rafradek.TF2weapons.util.TF2Util;

import com.google.common.collect.Lists;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.util.TF2Class;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import java.util.function.Predicate;

public class TF2CraftingManager {
	public static final ShapelessOreRecipe[] AMMO_RECIPES = new ShapelessOreRecipe[14];
	public static final TF2CraftingManager INSTANCE = new TF2CraftingManager();
	private final List<TF2Recipe> recipes = Lists.<TF2Recipe>newArrayList();
	private final Map<TF2Recipe, Predicate<Player>> recipeConditions = new HashMap<>();

	public TF2CraftingManager() {

		TF2Ingredient sentrybuilding = TF2Ingredient.fromStacks(TF2Util.metaStack(TF2weapons.itemBuildingBox, 1, 18),
				TF2Util.metaStack(TF2weapons.itemBuildingBox, 1, 19));
		TF2Ingredient dispenserbuilding = TF2Ingredient.fromStacks(TF2Util.metaStack(TF2weapons.itemBuildingBox, 1, 20),
				TF2Util.metaStack(TF2weapons.itemBuildingBox, 1, 21));
		TF2Ingredient teleporterbuilding = TF2Ingredient.fromStacks(TF2Util.metaStack(TF2weapons.itemBuildingBox, 1, 22),
				TF2Util.metaStack(TF2weapons.itemBuildingBox, 1, 23));

		ItemStack bonk = ItemFromData.getNewStack("bonk");
		bonk.setCount(2);
		ItemStack cola = ItemFromData.getNewStack("critcola");
		cola.setCount(2);
		addRecipe(TF2CraftingManager.AMMO_RECIPES[1] = new ShapelessOreRecipe(null,
				TF2Util.metaStack(TF2weapons.itemAmmo, 16, 1), new Object[] { "ingotCopper", "ingotLead", "gunpowder" }));
		addRecipe(/* TF2CraftingManager.AMMO_RECIPES[2] = */new ShapelessOreRecipe(null,
				TF2Util.metaStack(TF2weapons.itemAmmoMinigun, 1, 0), new Object[] { "ingotCopper", "ingotLead", "gunpowder",
						"ingotCopper", "ingotLead", "gunpowder", "gunpowder" }));
		addRecipe(/* TF2CraftingManager.AMMO_RECIPES[3] = */new ShapelessOreRecipe(null,
				TF2Util.metaStack(TF2weapons.itemAmmoPistol, 5, 0),
				new Object[] { "ingotCopper", "ingotLead", "gunpowder" }));
		addRecipe(TF2CraftingManager.AMMO_RECIPES[4] = new ShapelessOreRecipe(null,
				TF2Util.metaStack(TF2weapons.itemAmmo, 20, 4), new Object[] { "ingotCopper", "ingotLead", "gunpowder" }));
		addRecipe(TF2CraftingManager.AMMO_RECIPES[5] = new ShapelessOreRecipe(null,
				TF2Util.metaStack(TF2weapons.itemAmmoSMG, 4, 0), new Object[] { "ingotCopper", "ingotLead", "gunpowder" }));
		addRecipe(TF2CraftingManager.AMMO_RECIPES[6] = new ShapelessOreRecipe(null,
				TF2Util.metaStack(TF2weapons.itemAmmo, 5, 6), new Object[] { "ingotCopper", "ingotLead", "gunpowder" }));
		addRecipe(TF2CraftingManager.AMMO_RECIPES[7] = new ShapelessOreRecipe(null,
				TF2Util.metaStack(TF2weapons.itemAmmo, 22, 7), new Object[] { "ingotIron", "ingotIron", Blocks.TNT }));
		addRecipe(TF2CraftingManager.AMMO_RECIPES[8] = new ShapelessOreRecipe(null,
				TF2Util.metaStack(TF2weapons.itemAmmo, 22, 8), new Object[] { "ingotIron", "ingotIron", Blocks.TNT }));
		addRecipe(TF2CraftingManager.AMMO_RECIPES[11] = new ShapelessOreRecipe(null,
				TF2Util.metaStack(TF2weapons.itemAmmo, 18, 11), new Object[] { "ingotIron", "ingotIron", Blocks.TNT }));
		addRecipe(new ShapedOreRecipe(null, TF2Util.metaStack(TF2weapons.itemAmmo, 11, 13),
				new Object[] { " R ", "RIR", " R ", 'I', "ingotIron", 'R', "dustRedstone" }));
		addRecipe(new ShapedOreRecipe(null, TF2Util.metaStack(TF2weapons.itemAmmo, 8, 14),
				new Object[] { " S ", "SLS", " S ", 'S', "string", 'L', "leather" }));
		addShapelessRecipe(new ItemStack(TF2weapons.itemAmmoMedigun, 1),
				new Object[] { Items.GLISTERING_MELON_SLICE, Items.GHAST_TEAR, new ItemStack(Items.BONE_MEAL) });
		addRecipe(new ShapelessOreRecipe(null, new ItemStack(TF2weapons.itemAmmoFire, 1),
				new Object[] { "ingotIron", Items.MAGMA_CREAM, "ingotIron" }));
		addRecipe(TF2CraftingManager.AMMO_RECIPES[9] = new ShapelessOreRecipe(null,
				TF2Util.metaStack(TF2weapons.itemAmmoSyringe, 2, 0), new Object[] { "ingotIron", "paper", "paper", "paper",
						"paper", "paper", "paper", "paneGlass", "paneGlass" }));
		ItemStack cleaver = ItemFromData.getNewStack("cleaver");
		cleaver.setCount(1);
		addRecipe(new ShapedOreRecipe(null, cleaver, new Object[] { "I", "W", 'I', "ingotIron", 'W', "stickWood" }));
		addRecipe(new AustraliumRecipe());
		addRecipe(new RecipeApplyEffect(TF2weapons.itemStrangifier));
		addRecipe(new RecipeApplyEffect(TF2weapons.itemKillstreak));
		addRecipe(new JumperRecipe("rocketlauncher", "rocketjumper"));
		addRecipe(new JumperRecipe("stickybomblauncher", "stickyjumper"));
		addRecipe(new ShapedOreRecipe(null, ItemFromData.getNewStack("cloak"),
				new Object[] { "AAA", "LGL", "AAA", 'A', "ingotAustralium", 'G', "blockGlass", 'L', "leather" }));
		addRecipe(new ShapedOreRecipe(null, ItemFromData.getNewStack("deadringer"),
				new Object[] { " A ", "AGA", " A ", 'A', "ingotAustralium", 'G', "blockGlass" }));
		addRecipe(new ShapedOreRecipe(null, TF2weapons.itemDisguiseKit, new Object[] { "I I", "PAG", "I I", 'A',
				"ingotAustralium", 'I', "ingotIron", 'G', "blockGlass", 'P', "paper" }));
		addRecipe(new ShapedOreRecipe(null, ItemFromData.getNewStack("sapper"),
				new Object[] { " R ", "IRI", " R ", 'I', "ingotIron", 'R', "dustRedstone" }));
		// addRecipe(new ShapedOreRecipe(null,new ItemStack(TF2weapons.itemHorn),
		// new Object[] { "CLC", "C C", " C ", 'C', "ingotCopper", 'L', "leather" }));
		// addRecipe(new ShapedOreRecipe(null,ItemFromData.getNewStack("trigger"),
		// new Object[] { "I ", "I ", "IS", 'I', "ingotIron", 'S', "stickWood" }));
		addRecipe(new ShapedOreRecipe(null, TF2Util.metaStack(TF2weapons.itemBuildingBox, 1, 18),
				new Object[] { "IDI", "GRG", "III", 'D', new ItemStack(Blocks.DISPENSER), 'I', "ingotIron", 'G',
						"gunpowder", 'R', TF2Util.metaStack(TF2weapons.itemTF2, 1, 11) }));
		addRecipe(new ShapedOreRecipe(null, TF2Util.metaStack(TF2weapons.itemBuildingBox, 1, 20),
				new Object[] { "MDR", "SIm", "rIG", 'D', new ItemStack(Blocks.DISPENSER), 'I', "ingotIron", 'M',
						new ItemStack(TF2weapons.itemAmmoMedigun), 'G', TF2Util.metaStack(TF2weapons.itemAmmo, 1, 8), 'R',
						TF2Util.metaStack(TF2weapons.itemTF2, 1, 11), 'r', TF2Util.metaStack(TF2weapons.itemAmmo, 1, 7), 'S',
						TF2Util.metaStack(TF2weapons.itemAmmo, 1, 1), 'm', TF2Util.metaStack(TF2weapons.itemAmmo, 1, 6) }));
		addRecipe(new ShapedOreRecipe(null, TF2Util.metaStack(TF2weapons.itemBuildingBox, 1, 22),
				new Object[] { "IAI", "ARA", "IAI", 'I', "ingotIron", 'A', TF2Util.metaStack(TF2weapons.itemTF2, 1, 6), 'R',
						TF2Util.metaStack(TF2weapons.itemTF2, 1, 11) }));
		addRecipe(new ShapedOreRecipe(null, ItemFromData.getNewStack("pda"),
				new Object[] { "SDT", "AGA", "BRB", 'G', "paneGlass", 'A', TF2Util.metaStack(TF2weapons.itemTF2, 1, 2), 'R',
						TF2Util.metaStack(TF2weapons.itemTF2, 1, 3), 'B', new ItemStack(Blocks.STONE_BUTTON), 'S',
						sentrybuilding, 'D', dispenserbuilding, 'T', teleporterbuilding }));
		addRecipe(new ShapedOreRecipe(null,new ItemStack(TF2weapons.blockAmmoFurnace), new Object[] { "RIG", "SFr", "sIM",
						'F', new ItemStack(Blocks.FURNACE), 'I', "ingotIron", 'M', new ItemStack(TF2weapons.itemAmmoMinigun), 'G', TF2Util.metaStack(TF2weapons.itemAmmo,
								1, 8), 'R', TF2Util.metaStack(TF2weapons.itemAmmo, 1, 7), 'r', TF2Util.metaStack(TF2weapons.itemAmmo, 1, 6), 's', TF2Util.metaStack(TF2weapons.itemAmmo,
								1, 1), 'S', TF2Util.metaStack(TF2weapons.itemAmmo, 1, 11) }));

		addRecipe(new ShapedOreRecipe(null, new ItemStack(TF2weapons.itemAmmoBelt),
				new Object[] { " IL", "IL ", "L  ", 'I', "ingotIron", 'L', "leather" }));
		addRecipe(new ShapedOreRecipe(null, bonk,
				new Object[] { "SDS", "IWI", "SAS", 'I', "ingotIron", 'A', TF2Util.metaStack(TF2weapons.itemTF2, 1, 6), 'W',
						new ItemStack(Items.WATER_BUCKET), 'S', new ItemStack(Items.SUGAR), 'D', "dyeYellow" }));
		addRecipe(new ShapedOreRecipe(null, cola,
				new Object[] { "SDS", "IWI", "SAS", 'I', "ingotIron", 'A', TF2Util.metaStack(TF2weapons.itemTF2, 1, 6), 'W',
						new ItemStack(Items.WATER_BUCKET), 'S', new ItemStack(Items.SUGAR), 'D', "dyeMagenta" }));
		addRecipe(new ShapedOreRecipe(null, new ItemStack(TF2weapons.itemSandvich),
				new Object[] { " B ", "LHL", " B ", 'B', new ItemStack(Items.BREAD), 'L',
						new ItemStack(Blocks.GRASS), 'H', new ItemStack(Items.PORKCHOP) }));
		addRecipe(new ShapedOreRecipe(null, new ItemStack(TF2weapons.itemChocolate, 2), new Object[] { "CCC", "CCC",
				"MII", 'C', new ItemStack(Items.COCOA_BEANS), 'M', new ItemStack(Items.MILK_BUCKET), 'I', "paper" }));
		addRecipe(new ShapedOreRecipe(null, new ItemStack(TF2weapons.itemScoutBoots), new Object[] { "FFF", "FBF",
				"FFF", 'F', new ItemStack(Items.FEATHER), 'B', new ItemStack(Items.LEATHER_BOOTS) }));
		addRecipe(new ShapedOreRecipe(null, new ItemStack(TF2weapons.itemMantreads),
				new Object[] { "LBL", " I ", 'I', "ingotIron", 'B', new ItemStack(Items.IRON_BOOTS), 'L', "leather" }));
		addRecipe(new ShapedOreRecipe(null, new ItemStack(TF2weapons.itemGunboats),
				new Object[] { " B ", "III", 'I', "ingotIron", 'B', new ItemStack(Items.IRON_BOOTS) }));
		addRecipe(new ShapedOreRecipe(null, TF2Util.metaStack(TF2weapons.itemTF2, 1, 11), new Object[] { "C r", "CRr",
				"C r", 'C', Items.COMPARATOR, 'R', "blockRedstone", 'r', Items.REPEATER }));

		ItemStack jarate = ItemFromData.getNewStack("jarate");
		jarate.getTag().putBoolean("IsEmpty", true);
		addRecipe(new ShapedOreRecipe(null, jarate, new Object[] { " G ", "G G", "GGG", 'G', "paneGlass" }));
		ItemStack madmilk = ItemFromData.getNewStack("madmilk");
		madmilk.getTag().putBoolean("IsEmpty", true);
		addRecipe(new ShapedOreRecipe(null, madmilk, new Object[] { " G ", "G G", "GGG", 'G', "paneGlass" }));
		addRecipe(new ShapedOreRecipe(null, ItemFromData.getNewStack("basejumper"), new Object[] { "LLL", "S S", " s ",
				'L', "leather", 'S', "string", 's', TF2Util.metaStack(TF2weapons.itemTF2, 1, 3) }));

		ItemStack banner = new ItemStack(Items.RED_BANNER);
		banner.getOrCreateTagElement("BlockEntityTag").put("Patterns", new ListTag());
		banner.setCount(2);
		addRecipe(new ShapedOreRecipe(null, banner, new Object[] { "WWW", "WWW", "AS ", 'W', ItemTags.WOOL,
				'A', TF2Util.metaStack(TF2weapons.itemTF2, 1, 2), 'S', Items.STICK }));

		ItemStack bannern = new ItemStack(Items.GRAY_BANNER);
		bannern.getOrCreateTagElement("BlockEntityTag").put("Patterns", new ListTag());
		bannern.setCount(4);

		CompoundTag pattern = new CompoundTag();

		pattern.putString("Pattern", "nb");
		pattern.putInt("Color", 15);
		bannern.getOrCreateTagElement("BlockEntityTag").getList("Patterns", 10).add(pattern);
		addRecipe(new ShapedOreRecipe(null, bannern, new Object[] { "WWW", "WWW", " SA", 'W',
				ItemTags.WOOL, 'A', TF2Util.metaStack(TF2weapons.itemTF2, 1, 2), 'S', Items.STICK }));

		/*
		 * addRecipe(new ShapedOreRecipe(null,new
		 * ItemStack(TF2weapons.blockOverheadDoor), new Object[] { "BI", "B ", "B ",
		 * 'B', Blocks.IRON_BARS, 'I',"ingotIron" })); addRecipe(new
		 * ShapedOreRecipe(null,TF2Util.metaStack(TF2weapons.itemDoorController,1,0), new
		 * Object[] { "RIR", "IOI", "RIR", 'R', "dustRedstone", 'I',"ingotIron", 'O',
		 * Blocks.OBSERVER })); addRecipe(new ShapedOreRecipe(null,new
		 * ItemStack(TF2weapons.itemDoorController,1,1), new Object[] { "SIS", "IOI",
		 * "SIS", 'S', "gunpowder", 'I',"ingotIron", 'O', Blocks.OBSERVER }));
		 * addRecipe(new ShapedOreRecipe(null,new
		 * ItemStack(TF2weapons.itemDoorController,1,2), new Object[] { "RIR", "IOI",
		 * "RIR", 'R', "dyeRed", 'I',"ingotIron", 'O', Blocks.OBSERVER }));
		 * addRecipe(new ShapedOreRecipe(null,new
		 * ItemStack(TF2weapons.itemDoorController,1,3), new Object[] { "RIR", "IOI",
		 * "RIR", 'R', "dyeBlue", 'I',"ingotIron", 'O', Blocks.OBSERVER }));
		 */

		addRecipe(new ShapedOreRecipe(null, ItemFromData.getNewStack("startwrench"),
				new Object[] { " II", " S ", "I  ", 'I', "ingotIron", 'S', TF2Util.metaStack(TF2weapons.itemTF2, 1, 3) }));
		if (!TF2ConfigVars.disableBossSpawnItems) {
			addRecipe(new ShapedOreRecipe(null, TF2Util.metaStack(TF2weapons.itemBossSpawn, 1, 0), new Object[] { "II",
					"SA", "S ", 'I', "ingotIron", 'S', "stickWood", 'A', TF2Util.metaStack(TF2weapons.itemTF2, 1, 2) }));
			addRecipe(new ShapedOreRecipe(null, TF2Util.metaStack(TF2weapons.itemBossSpawn, 1, 1),
					new Object[] { "EAE", "ESE", "EAE", 'E', Items.SPIDER_EYE, 'A',
							TF2Util.metaStack(TF2weapons.itemTF2, 1, 6), 'S', new IngredientWeapon("bottle") }));
			addRecipe(new ShapedOreRecipe(null, TF2Util.metaStack(TF2weapons.itemBossSpawn, 1, 2),
					new Object[] { "BEG", "NAN", "GEB", 'E', "gemEmerald", 'A', TF2Util.metaStack(TF2weapons.itemTF2, 1, 2),
							'B', Items.BOOK, 'G', Items.GLASS_BOTTLE, 'N', "cropNetherWart" }));
		}
		addRecipe(new ShapelessOreRecipe(null, new ItemStack(TF2weapons.blockUpgradeStation),
				new Object[] { new IngredientWeapon("headtaker"), new IngredientWeapon("monoculus"),
						new IngredientWeapon("merasmushat") }));
		// addRecipe(new ShapedOreRecipe(null,new
		// ItemStack(TF2weapons.blockRobotDeploy), new Object[] { "LLL", "G G", "AIA",
		// 'L', TF2Util.metaStack(TF2weapons.itemTF2, 1, 11), 'G', "blockGlass", 'I',
		// "blockIron","A",TF2Util.metaStack(TF2weapons.itemTF2, 1, 2)}));
		addShapelessRecipe(TF2Util.metaStack(TF2weapons.itemTF2, 1, 4), TF2Util.metaStack(TF2weapons.itemTF2, 1, 3),
				TF2Util.metaStack(TF2weapons.itemTF2, 1, 3), TF2Util.metaStack(TF2weapons.itemTF2, 1, 3));
		addShapelessRecipe(TF2Util.metaStack(TF2weapons.itemTF2, 1, 5), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				TF2Util.metaStack(TF2weapons.itemTF2, 1, 4), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4));
		addShapelessRecipe(TF2Util.metaStack(TF2weapons.itemTF2, 3, 4), TF2Util.metaStack(TF2weapons.itemTF2, 1, 5));
		addShapelessRecipe(TF2Util.metaStack(TF2weapons.itemTF2, 3, 3), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4));
		addShapelessRecipe(TF2Util.metaStack(TF2weapons.itemTF2, 1, 9), TF2Util.metaStack(TF2weapons.itemTF2, 1, 3),
				TF2Util.metaStack(TF2weapons.itemTF2, 1, 3));
		addShapelessRecipe(TF2Util.metaStack(TF2weapons.itemTF2, 1, 10), TF2Util.metaStack(TF2weapons.itemTF2, 1, 5),
				TF2Util.metaStack(TF2weapons.itemTF2, 1, 5), TF2Util.metaStack(TF2weapons.itemTF2, 1, 5));

		addRecipe(new OpenCrateRecipe());
		addRecipe(new RecipeToScrap(-1));
		for (int i = 0; i < 9; i++)
			addRecipe(new RecipeToScrap(i));
		addTokenRecipe(null, TF2Util.metaStack(TF2weapons.itemTF2, 1, 9), TF2Util.metaStack(TF2weapons.itemTF2, 1, 3));

		//TF2 Blueprint Recipes
		addShapelessRecipe(ItemFromData.getNewStack("cleaver"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("madmilk"), ItemFromData.getNewStack("madmilk"), ItemFromData.getNewStack("madmilk"));
		addShapelessRecipe(ItemFromData.getNewStack("babyfaceblaster"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("shortstop"), ItemFromData.getNewStack("shortstop"));
		addShapelessRecipe(ItemFromData.getNewStack("prettyboyspocketpistol"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("madmilk"), ItemFromData.getNewStack("winger"));
		/*addShapelessRecipe(ItemFromData.getNewStack("wrapassassin"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("southernhospitality"));*/
		addShapelessRecipe(ItemFromData.getNewStack("sodapopper"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("bonk"), ItemFromData.getNewStack("forceanature"));
		addShapelessRecipe(ItemFromData.getNewStack("winger"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("shortstop"));
		addShapelessRecipe(ItemFromData.getNewStack("atomizer"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("bonk"), ItemFromData.getNewStack("sandman"));
		/*addShapelessRecipe(ItemFromData.getNewStack("fanowar"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 3),
				ItemFromData.getNewStack("madmilk"));*/
		addShapelessRecipe(ItemFromData.getNewStack("sunonastick"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				TF2Util.metaStack(TF2weapons.itemTF2, 1, 4), ItemFromData.getNewStack("bostonbasher"));
		addShapelessRecipe(ItemFromData.getNewStack("candycane"), ItemFromData.getNewStack("kritzkrieg"),
				ItemFromData.getNewStack("paintrain"));
		addShapelessRecipe(ItemFromData.getNewStack("shortstop"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("forceanature"));
		addShapelessRecipe(ItemFromData.getNewStack("madmilk"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("jarate"));
		addShapelessRecipe(ItemFromData.getNewStack("holymackerel"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("sandman"));
		addShapelessRecipe(ItemFromData.getNewStack("critcola"), ItemFromData.getNewStack("kritzkrieg"),
				ItemFromData.getNewStack("bonk"));
		/*addShapelessRecipe(ItemFromData.getNewStack("backscatter"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("shortstop"), ItemFromData.getNewStack("critcola"));*/
		addShapelessRecipe(ItemFromData.getNewStack("beggarbazooka"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("directhit"), ItemFromData.getNewStack("directhit"), ItemFromData.getNewStack("directhit"));
		addShapelessRecipe(ItemFromData.getNewStack("escapeplan"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 3),
				ItemFromData.getNewStack("disciplinary"));
		addShapelessRecipe(ItemFromData.getNewStack("original"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 3),
				TF2Util.metaStack(TF2weapons.itemTF2, 1, 3), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4));
		addShapelessRecipe(ItemFromData.getNewStack("cowmangler"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("blackbox"));
		addShapelessRecipe(new ItemStack(TF2weapons.itemMantreads), TF2Util.metaStack(TF2weapons.itemTF2, 1, 5),
				new ItemStack(TF2weapons.itemGunboats));
		/*addShapelessRecipe(ItemFromData.getNewStack("reserveshooter"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 5),
				TF2Util.metaStack(TF2weapons.itemTF2, 1, 5), ItemFromData.getNewStack("frontierjustice"));*/
		addShapelessRecipe(ItemFromData.getNewStack("disciplinary"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				TF2Util.metaStack(TF2weapons.itemTF2, 1, 4), ItemFromData.getNewStack("paintrain"));
		addShapelessRecipe(ItemFromData.getNewStack("marketgarden"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				new ItemStack(TF2weapons.itemGunboats), ItemFromData.getNewStack("paintrain"));
		addShapelessRecipe(ItemFromData.getNewStack("halfzatoichi"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("eyelander"), ItemFromData.getNewStack("eyelander"));
		addShapelessRecipe(ItemFromData.getNewStack("concheror"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 3),
				ItemFromData.getNewStack("battalionbackup"));
		addShapelessRecipe(ItemFromData.getNewStack("blackbox"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("directhit"));
		addShapelessRecipe(ItemFromData.getNewStack("battalionbackup"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("buffbanner"));
		addShapelessRecipe(new ItemStack(TF2weapons.itemGunboats), ItemFromData.getNewStack("razorback"),
				ItemFromData.getNewStack("chargintarge"));
		addShapelessRecipe(ItemFromData.getNewStack("paintrain"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 3),
				ItemFromData.getNewStack("sandman"));
		addShapelessRecipe(ItemFromData.getNewStack("rocketjumper"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				new ItemStack(TF2weapons.itemMantreads), new ItemStack(TF2weapons.itemMantreads), new ItemStack(TF2weapons.itemMantreads));
		addShapelessRecipe(ItemFromData.getNewStack("airstrike"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("beggarbazooka"), new ItemStack(TF2weapons.itemGunboats));
		addShapelessRecipe(ItemFromData.getNewStack("basejumper"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("buffbanner"), ItemFromData.getNewStack("stickybomblauncher"));
		/*addShapelessRecipe(ItemFromData.getNewStack("panicattack"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				TF2Util.metaStack(TF2weapons.itemTF2, 1, 4), ItemFromData.getNewStack("backscatter"));*/
		/*addShapelessRecipe(ItemFromData.getNewStack("neonannihilator"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("thirddegree"), ItemFromData.getNewStack("thirddegree"));*/
		/*addShapelessRecipe(ItemFromData.getNewStack("scorchshot"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("flaregun"), ItemFromData.getNewStack("degreaser"));*/
		/*addShapelessRecipe(ItemFromData.getNewStack("phlog"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("sodapopper"), ItemFromData.getNewStack("backburner"));*/
		addShapelessRecipe(ItemFromData.getNewStack("manmelter"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("detonator"));
		addShapelessRecipe(ItemFromData.getNewStack("thirddegree"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("axtinguisher"), ItemFromData.getNewStack("powerjack"));
		/*addShapelessRecipe(ItemFromData.getNewStack("reserveshooter"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				TF2Util.metaStack(TF2weapons.itemTF2, 1, 4), ItemFromData.getNewStack("frontierjustice"));*/
		addShapelessRecipe(ItemFromData.getNewStack("flaregun"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				TF2Util.metaStack(TF2weapons.itemTF2, 1, 4), ItemFromData.getNewStack("detonator"));
		addShapelessRecipe(ItemFromData.getNewStack("sharpenedvolcanofragment"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				TF2Util.metaStack(TF2weapons.itemTF2, 1, 4), ItemFromData.getNewStack("axtinguisher"));
		/*addShapelessRecipe(ItemFromData.getNewStack("backscratcher"), ItemFromData.getNewStack("axtinguisher"),
				ItemFromData.getNewStack("scotsmansskullcutter"));*/
		/*addShapelessRecipe(ItemFromData.getNewStack("degreaser"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("backburner"));*/
		addShapelessRecipe(ItemFromData.getNewStack("powerjack"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("axtinguisher"));
		/*addShapelessRecipe(ItemFromData.getNewStack("homewrecker"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("equalizer"));*/
		/*addShapelessRecipe(new ItemStack(TF2weapons.itemWeeBooties), new ItemStack(TF2weapons.itemMantreads),
				ItemFromData.getNewStack("gru"));*/
		addShapelessRecipe(ItemFromData.getNewStack("splendidscreen"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				TF2Util.metaStack(TF2weapons.itemTF2, 1, 4), ItemFromData.getNewStack("chargintarge"));
		/*addShapelessRecipe(ItemFromData.getNewStack("persianpursuader"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 3),
				ItemFromData.getNewStack("halfzatoichi"), ItemFromData.getNewStack("halfzatoichi"));*/
		addShapelessRecipe(ItemFromData.getNewStack("halfzatoichi"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("eyelander"), ItemFromData.getNewStack("eyelander"));
		addShapelessRecipe(ItemFromData.getNewStack("lochnload"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("scottishresistance"));
		/*addShapelessRecipe(ItemFromData.getNewStack("caber"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 3),
				TF2Util.metaStack(TF2weapons.itemTF2, 1, 3), ItemFromData.getNewStack("paintrain"));*/
		/*addShapelessRecipe(ItemFromData.getNewStack("claidheamhmor"), ItemFromData.getNewStack("homewrecker"),
				ItemFromData.getNewStack("chargintarge"));*/
		/*addShapelessRecipe(ItemFromData.getNewStack("headtaker"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 5),
				TF2Util.metaStack(TF2weapons.itemTF2, 1, 5), TF2Util.metaStack(TF2weapons.itemTF2, 1, 8),
				ItemFromData.getNewStack("scotsmansskullcutter"));*/
		addShapelessRecipe(ItemFromData.getNewStack("scotsmansskullcutter"), ItemFromData.getNewStack("axtinguisher"),
				ItemFromData.getNewStack("jarate"));
		addShapelessRecipe(ItemFromData.getNewStack("paintrain"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 3),
				ItemFromData.getNewStack("sandman"));
		addShapelessRecipe(ItemFromData.getNewStack("loosecannon"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("lochnload"), ItemFromData.getNewStack("lochnload"), ItemFromData.getNewStack("lochnload"));
		addShapelessRecipe(ItemFromData.getNewStack("stickyjumper"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("caber"), ItemFromData.getNewStack("caber"), ItemFromData.getNewStack("caber"));
		/*addShapelessRecipe(ItemFromData.getNewStack("tideturner"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				new ItemStack(TF2weapons.itemWeeBooties), ItemFromData.getNewStack("chargintarge"));*/
		/*addShapelessRecipe(ItemFromData.getNewStack("ironbomber"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				TF2Util.metaStack(TF2weapons.itemTF2, 1, 4), ItemFromData.getNewStack("airstrike"));*/
		/*addShapelessRecipe(ItemFromData.getNewStack("quickiebomblauncher"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				TF2Util.metaStack(TF2weapons.itemTF2, 1, 4), ItemFromData.getNewStack("tideturner"));*/
		addShapelessRecipe(ItemFromData.getNewStack("heater"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("familybusiness"), ItemFromData.getNewStack("familybusiness"),
				ItemFromData.getNewStack("familybusiness"));
		/*addShapelessRecipe(ItemFromData.getNewStack("holidaypunch"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("holymackerel"), ItemFromData.getNewStack("gru"));*/
		addShapelessRecipe(ItemFromData.getNewStack("tomislav"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				TF2Util.metaStack(TF2weapons.itemTF2, 1, 4), ItemFromData.getNewStack("brassbeast"));
		addShapelessRecipe(ItemFromData.getNewStack("familybusiness"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("frontierjustice"), ItemFromData.getNewStack("homewrecker"));
		addShapelessRecipe(ItemFromData.getNewStack("evictionnotice"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				TF2Util.metaStack(TF2weapons.itemTF2, 1, 4), ItemFromData.getNewStack("fistofsteel"));
		addShapelessRecipe(ItemFromData.getNewStack("brassbeast"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("natascha"));
		/*addShapelessRecipe(new ItemStack(TF2weapons.itemBuffaloSandvich), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				new ItemStack(TF2weapons.itemSandvich));*/
		addShapelessRecipe(ItemFromData.getNewStack("warriorsspirit"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 3),
				ItemFromData.getNewStack("gru"));
		addShapelessRecipe(ItemFromData.getNewStack("fistofsteel"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("gru"));
		addShapelessRecipe(new ItemStack(TF2weapons.itemChocolate), TF2Util.metaStack(TF2weapons.itemTF2, 1, 3),
				new ItemStack(TF2weapons.itemSandvich));
		addShapelessRecipe(ItemFromData.getNewStack("pomson"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("righterousbison"), ItemFromData.getNewStack("shortcircuit"));
		addShapelessRecipe(ItemFromData.getNewStack("eurekaeffect"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("jag"), ItemFromData.getNewStack("jag"));
		addShapelessRecipe(ItemFromData.getNewStack("widowmaker"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				TF2Util.metaStack(TF2weapons.itemTF2, 1, 4), ItemFromData.getNewStack("letranger"));
		addShapelessRecipe(ItemFromData.getNewStack("shortcircuit"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				TF2Util.metaStack(TF2weapons.itemTF2, 1, 3), ItemFromData.getNewStack("gunslinger"));
		addShapelessRecipe(ItemFromData.getNewStack("jag"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 3),
				ItemFromData.getNewStack("southernhospitality"));
		addShapelessRecipe(ItemFromData.getNewStack("southernhospitality"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 3),
				ItemFromData.getNewStack("ambassador"));
		addShapelessRecipe(ItemFromData.getNewStack("rescueranger"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("eurekaeffect"), ItemFromData.getNewStack("eurekaeffect"),
				ItemFromData.getNewStack("eurekaeffect"));
		addShapelessRecipe(ItemFromData.getNewStack("overdose"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("gru"), ItemFromData.getNewStack("crossbow"));
		addShapelessRecipe(ItemFromData.getNewStack("quickfix"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("madmilk"), ItemFromData.getNewStack("kritzkrieg"));
		/*addShapelessRecipe(ItemFromData.getNewStack("solemnvow"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("jarate"), ItemFromData.getNewStack("jarate"),
				ItemFromData.getNewStack("jarate"), ItemFromData.getNewStack("jarate"),
				ItemFromData.getNewStack("jarate"), ItemFromData.getNewStack("jarate"),
				ItemFromData.getNewStack("jarate"), ItemFromData.getNewStack("jarate"));*/
		addShapelessRecipe(ItemFromData.getNewStack("crossbow"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 3),
				TF2Util.metaStack(TF2weapons.itemTF2, 1, 3), ItemFromData.getNewStack("huntsman"));
		/*addShapelessRecipe(ItemFromData.getNewStack("amputator"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 3),
				ItemFromData.getNewStack("vitasaw"));*/
		/*addShapelessRecipe(ItemFromData.getNewStack("vitasaw"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 3),
				TF2Util.metaStack(TF2weapons.itemTF2, 1, 3), ItemFromData.getNewStack("ubersaw"));*/
		addShapelessRecipe(ItemFromData.getNewStack("vaccinator"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("quickfix"), ItemFromData.getNewStack("quickfix"), ItemFromData.getNewStack("quickfix"));
		/*addShapelessRecipe(ItemFromData.getNewStack("heatmaker"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("machina"), ItemFromData.getNewStack("bazaarbargain"));*/
		addShapelessRecipe(ItemFromData.getNewStack("cleanerscarbine"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("bushwacka"), ItemFromData.getNewStack("bushwacka"), ItemFromData.getNewStack("bushwacka"));
		addShapelessRecipe(ItemFromData.getNewStack("cozycamper"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("jarate"), ItemFromData.getNewStack("razorback"));
		addShapelessRecipe(ItemFromData.getNewStack("machina"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("righterousbison"), ItemFromData.getNewStack("sydneysleeper"));
		/*addShapelessRecipe(ItemFromData.getNewStack("bazaarbargain"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("sydneysleeper"), ItemFromData.getNewStack("eyelander"));*/
		/*addShapelessRecipe(ItemFromData.getNewStack("shahanshah"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("amputator"), ItemFromData.getNewStack("bushwacka"));*/
		addShapelessRecipe(ItemFromData.getNewStack("sydneysleeper"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("huntsman"));
		addShapelessRecipe(ItemFromData.getNewStack("dangershield"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("razorback"));
		addShapelessRecipe(ItemFromData.getNewStack("bushwacka"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("eyelander"));
		addShapelessRecipe(ItemFromData.getNewStack("tribalmansshiv"), ItemFromData.getNewStack("deadringer"),
				ItemFromData.getNewStack("huntsman"));
		/*addShapelessRecipe(ItemFromData.getNewStack("classic"),  TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("huntsman"), ItemFromData.getNewStack("bazaarbargain"));*/
		/*addShapelessRecipe(ItemFromData.getNewStack("recorder"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("spycicle"), ItemFromData.getNewStack("spycicle"),
				ItemFromData.getNewStack("spycicle"));*/
		/*addShapelessRecipe(ItemFromData.getNewStack("spycicle"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("eternalreward"));*/
		/*addShapelessRecipe(ItemFromData.getNewStack("diamondback"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("frontierjustice"), ItemFromData.getNewStack("deadringer"));*/
		/*addShapelessRecipe(ItemFromData.getNewStack("enforcer"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				TF2Util.metaStack(TF2weapons.itemTF2, 1, 3), ItemFromData.getNewStack("lestranger"));*/
		/*addShapelessRecipe(ItemFromData.getNewStack("bigearner"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("kunai"), ItemFromData.getNewStack("lestranger"));*/
		addShapelessRecipe(ItemFromData.getNewStack("kunai"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 3),
				ItemFromData.getNewStack("eternalreward"));
		/*addShapelessRecipe(ItemFromData.getNewStack("lestranger"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("deadringer"));*/
		/*addShapelessRecipe(ItemFromData.getNewStack("eternalreward"), TF2Util.metaStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("cloakanddagger"));*/
	}

	public TF2Recipe addShapelessRecipe(ItemStack stack, Object... recipeComponents) {
		return addRecipe(new ShapelessOreRecipe(null, stack, recipeComponents));
	}

	/**
	 * Adds an IRecipe to the list of crafting recipes.
	 */
	public TF2Recipe addRecipe(TF2Recipe recipe) {
		this.recipes.add(recipe);
		return recipe;
	}

	public void addTokenRecipe(ResourceLocation group, ItemStack result, ItemStack... inputs) {
		for (TF2Class clazz : TF2Class.getClasses()) recipes.add(new RecipeToken(group, result, inputs, clazz));
	}

	public void addRecipeCondition(TF2Recipe recipe, Predicate<Player> predicate) {
		this.recipeConditions.put(recipe, predicate);
	}

	/**
	 * Retrieves an ItemStack that has multiple recipes for it.
	 */
	@Nullable
	public ItemStack findMatchingRecipe(CraftingContainer craftMatrix, Level world, Player player) {
		for (TF2Recipe irecipe : this.recipes)
			if (!(this.recipeConditions.containsKey(irecipe) && !this.recipeConditions.get(irecipe).test(player))
					&& irecipe.matches(craftMatrix, world))
				return irecipe.getCraftingResult(craftMatrix);

		return ItemStack.EMPTY;
	}

	public NonNullList<ItemStack> getRemainingItems(CraftingContainer craftMatrix, Level world) {
		for (TF2Recipe irecipe : this.recipes)
			if (irecipe.matches(craftMatrix, world))
				return irecipe.getRemainingItems(craftMatrix);

		NonNullList<ItemStack> aitemstack = NonNullList.withSize(craftMatrix.getContainerSize(), ItemStack.EMPTY);

		for (int i = 0; i < aitemstack.size(); ++i)
			aitemstack.set(i, craftMatrix.getItem(i));

		return aitemstack;
	}

	public List<TF2Recipe> getRecipeList() {
		return this.recipes;
	}
}
