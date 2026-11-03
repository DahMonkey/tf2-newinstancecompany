package rafradek.TF2weapons.entity.building;

import net.minecraftforge.common.capabilities.ForgeCapabilities;
import rafradek.TF2weapons.client.compat.LegacyGui;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import rafradek.TF2weapons.client.compat.Tessellator;
import rafradek.TF2weapons.client.compat.BufferBuilder;
import rafradek.TF2weapons.client.compat.DefaultVertexFormats;

import rafradek.TF2weapons.client.compat.GlStateManager;
import rafradek.TF2weapons.client.compat.GL11;

import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.client.audio.TF2Sounds;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.mercenary.EntityEngineer;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.item.ItemCloak;
import rafradek.TF2weapons.item.ItemDisguiseKit;
import rafradek.TF2weapons.util.TF2Util;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.function.Predicate;

public class EntityDispenser extends EntityBuilding {

	public int reloadTimer;
	public int giveAmmoTimer;
	public List<LivingEntity> dispenserTarget = new ArrayList<>();
	private static final EntityDataAccessor<Integer> METAL = SynchedEntityData.defineId(EntityDispenser.class,
			EntityDataSerializers.INT);
	private static final EntityDataAccessor<Float> RANGE = SynchedEntityData.defineId(EntityDispenser.class,
			EntityDataSerializers.FLOAT);
	public ItemStackHandler items = new ItemStackHandler(9);

	public HashMap<ItemStack, Float> fillMeter = new HashMap<>();
	public int food;
	private ItemStack currRepairItem;

	public static final int MAX_METAL = 400;

	public EntityDispenser(Level world) {
		this(TF2weapons.DISPENSER, world);
	}

	public EntityDispenser(EntityType<? extends EntityDispenser> type, Level world) {
		super(type, world);
		this.setSize(1f, 1.1f);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.isDisabled()) {
			this.dispenserTarget.clear();
			return;
		}

		List<LivingEntity> targetList = this.level.getEntitiesOfClass(LivingEntity.class,
				this.getBoundingBox().inflate(2f * this.getRange(), 1.5 * this.getRange(), 2 * this.getRange()),
				input -> !(input instanceof EntityBuilding) && EntityDispenser.this != input
						&& ((getOwner() != null && WeaponsCapability.get(getOwner()).dispenserPlayer
								&& input instanceof Player && input.getTeam() == null)
								|| TF2Util.isAlliedTo(EntityDispenser.this, input)));
		if (!this.level.isClientSide) {
			this.reloadTimer--;

			if (this.reloadTimer <= 0 && this.getMetal() < MAX_METAL) {
				int metalAmount = TF2ConfigVars.fastMetalProduction ? 30 : 21;
				metalAmount = Math.min(MAX_METAL - this.getMetal(), metalAmount + this.getBuildingLevel() * (metalAmount / 3));
				if (this.consumeEnergy(metalAmount * this.getMinEnergy())) {
					this.setMetal(this.getMetal() + metalAmount);
					this.playSound(TF2Sounds.MOB_DISPENSER_GENERATE_METAL, 1.55f, 1f);
					this.reloadTimer = TF2ConfigVars.fastMetalProduction ? 100 : 200;
				}
			}
			this.giveAmmoTimer--;
			if (this.food <= 8 && this.giveAmmoTimer == 0) {
				ItemStack foodItem = TF2Util.getFirstItem(this.items, stack -> stack.isEdible());

				if (!foodItem.isEmpty()) {
					this.food += foodItem.getItem().getFoodProperties(foodItem, this).getNutrition();
					foodItem.shrink(1);
				}
			}
			for (LivingEntity living : targetList) {
				int level = this.getBuildingLevel();
				if (living.getHealth() < living.getMaxHealth() && this.consumeEnergy(this.getMinEnergy()))
					living.heal(0.025f + 0.025f * level);

				if (this.giveAmmoTimer == 0) {
					if (living instanceof Player) {
						net.minecraft.world.food.FoodData stats = ((Player) living).getFoodData();
						if (stats.getFoodLevel() >= 20)
							stats.eat(1, living.getHealth() >= living.getMaxHealth() ? 8 : 1);
						else {
							int foodAmount = (int) (living.getHealth() / living.getMaxHealth() * 8);
							if (this.food > 0) {
								stats.eat(Math.min(foodAmount, this.food), 1f);
								this.food -= Math.min(foodAmount, this.food);
							}
						}
					}

					if (living instanceof EntityEngineer || living instanceof Player) {
						int metal = living.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).getMetal();
						int metalUse = Math.min(30 + this.getBuildingLevel() * 10,
								Math.min(WeaponsCapability.get(living).getMaxMetal() - metal, this.getMetal()));
						this.setMetal(this.getMetal() - metalUse);
						living.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).setMetal(metal + metalUse);
					}
					if (living instanceof EntityTF2Character) {
						((EntityTF2Character) living).restoreAmmo(0.1f + this.getBuildingLevel() * 0.1f);

					}
					Predicate<ItemStack> test = stack -> (!stack.isEmpty()
							&& (stack.isRepairable()
									|| (stack.isDamageableItem() && !TF2ConfigVars.oldDispenser))
							&& stack.getDamageValue() != 0
							&& !TF2ConfigVars.repairBlacklist.contains(net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem())));
					ItemStack heldItem = TF2Util.getFirstItem(items, test);

					if (heldItem.isEmpty() && test.test(living.getMainHandItem()))
						heldItem = living.getMainHandItem();
					if (!heldItem.isEmpty()) {

						float repairMult = TF2ConfigVars.dispenserRepair;
						ListTag list = heldItem.getEnchantmentTags();
						float enchantCost = 1f;
						if (list != null) {
							for (int i = 0; i < list.size(); i++)
								enchantCost -= list.getCompound(i).getShort("lvl") / 15f;
							if (enchantCost <= 1f / 3f)
								enchantCost = 1f / 3f;
						}
						repairMult *= enchantCost;
						int metalUse = Math.min(15 + this.getBuildingLevel() * 10,
								Math.min((int) (heldItem.getDamageValue() / repairMult) + 1, this.getMetal()));

						// System.out.println("use: "+metalUse);
						int repairUses = this.getRepairMaterialUses(heldItem,
								Math.min(heldItem.getDamageValue(), (int) (metalUse * repairMult)), 1f / enchantCost);
						// System.out.println("repair use: "+repairUses);
						if (repairUses != 0) {
							if (this.consumeEnergy(metalUse * this.getMinEnergy())) {
								this.setMetal(this.getMetal() - metalUse);
								heldItem.setDamageValue(heldItem.getDamageValue() - repairUses);

							}
						}
					}
				}
				Tuple<Integer, ItemStack> cloak = ItemCloak.searchForWatches(living);
				if (!cloak.getB().isEmpty() || (!living.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()
						&& living.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof ItemCloak)) {
					if (cloak.getB().isEmpty())
						cloak = new Tuple<>(-1, living.getMainHandItem());

					if (TF2Attribute.getModifier("No External Cloak", cloak.getB(), 0, living) == 0) {
						cloak.getB()
								.setDamageValue(Math.max(cloak.getB().getDamageValue() - (2 + this.getBuildingLevel()), 0));
						if (living instanceof ServerPlayer)
							((ServerPlayer) living).connection
									.send(new ClientboundContainerSetSlotPacket(-2, 0, cloak.getA(), cloak.getB()));
					}
				}
				if (this.dispenserTarget != null && !this.dispenserTarget.contains(living))
					this.playSound(TF2Sounds.MOB_DISPENSER_HEAL, 0.75f, 1f);
			}
			if (this.giveAmmoTimer <= 0)
				this.giveAmmoTimer = 20;
		}
		this.dispenserTarget = targetList;
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (player == this.getOwner() && hand == InteractionHand.MAIN_HAND) {
			if (!this.level.isClientSide) {
				if (TF2ConfigVars.disableBuildingGui)
					this.grab();
				else
					TF2weapons.openGui((net.minecraft.world.entity.player.Player) player, 5, this.level, this.getId(), 0, 0);
			}

			return InteractionResult.sidedSuccess(this.level.isClientSide);
		}
		return InteractionResult.sidedSuccess(this.level.isClientSide);
	}

	@Override
	public SoundEvent getSoundNameForState(int state) {
		switch (state) {
		case 0:
			return TF2Sounds.MOB_DISPENSER_IDLE;
		default:
			return super.getSoundNameForState(state);
		}
	}

	public int getRepairMaterialUses(ItemStack tool, int maxRepair, float matCostMult) {
		if (TF2ConfigVars.oldDispenser)
			return maxRepair;
		Iterator<Entry<ItemStack, Float>> it = this.fillMeter.entrySet().iterator();
		while (it.hasNext()) {
			Entry<ItemStack, Float> uses = it.next();
			if (tool.getItem().isValidRepairItem(tool, uses.getKey())) {
				this.currRepairItem = uses.getKey();
				float materialCost = this.getMaterialCost(tool) * matCostMult;
				float use = ((float) maxRepair / (float) tool.getMaxDamage()) * materialCost;

				float maxUse = Math.min(uses.getValue(), use);
				uses.setValue(uses.getValue() - maxUse);
				if (uses.getValue() == 0)
					it.remove();
				return (int) (maxUse * (tool.getMaxDamage() / materialCost));
			}
		}

		for (int i = 0; i < this.items.getSlots(); i++) {
			if (tool.getItem().isValidRepairItem(tool, this.items.getStackInSlot(i))) {

				this.currRepairItem = this.items.getStackInSlot(i);
				this.items.extractItem(i, 1, false);

				float materialCost = this.getMaterialCost(tool) * matCostMult;
				float use = ((float) maxRepair / (float) tool.getMaxDamage()) * materialCost;
				float maxUse = Math.min(1, use);
				this.fillMeter.put(ItemHandlerHelper.copyStackWithSize(currRepairItem, 1), 1f - maxUse);
				return (int) (maxUse * (tool.getMaxDamage() / materialCost));
			}
		}
		return 0;
	}

	@SuppressWarnings("deprecation")
	public float getMaterialCost(ItemStack stack) {
		float cost = 0;
		for (net.minecraft.world.item.crafting.Recipe<?> recipe : this.level.getRecipeManager().getRecipes()) {
			if (recipe.getResultItem().sameItemStackIgnoreDurability(stack)
					&& TF2Util.isBaseSame(recipe.getResultItem().getTag(), stack.getTag())) {
				for (Ingredient ing : recipe.getIngredients()) {
					if (ing.test(this.currRepairItem)) {
						cost++;
					}

				}
				if (cost != 0) {
					break;
				}
			}
		}
		if (cost == 0) {
			if (stack.getItem() instanceof net.minecraft.world.item.PickaxeItem || stack.getItem() instanceof AxeItem)
				cost = 3;
			else if (stack.getItem() instanceof SwordItem || stack.getItem() instanceof net.minecraft.world.item.HoeItem
					|| stack.getItem() instanceof net.minecraft.world.item.ShearsItem)
				cost = 2;
			else if (stack.getItem() instanceof net.minecraft.world.item.ShovelItem)
				cost = 1;
			else if (stack.getItem() instanceof ArmorItem) {
				switch (((ArmorItem) stack.getItem()).getSlot()) {
				case CHEST:
					cost = 8;
					break;
				case FEET:
					cost = 4;
					break;
				case HEAD:
					cost = 5;
					break;
				case LEGS:
					cost = 7;
					break;
				default:
					break;
				}
			} else if (stack.getItem() instanceof ItemDisguiseKit)
				cost = 1;
			else {
				cost = 4;
			}
		}
		if (stack.getItem() instanceof ArmorItem)
			cost /= 4.5f;
		else
			cost /= 3f;
		return cost;
	}

	public static boolean isNearDispenser(Level world, final LivingEntity living) {
		List<EntityDispenser> targetList = world.getEntitiesOfClass(EntityDispenser.class,
				living.getBoundingBox().inflate(8D, 6D, 8D), input -> !input.isDisabled()
						&& input.dispenserTarget != null && input.dispenserTarget.contains(living));
		return !targetList.isEmpty();
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(METAL, 0);
		this.entityData.define(RANGE, 1f);
	}

	public int getMetal() {
		return this.entityData.get(METAL);
	}

	public void setMetal(int amount) {
		this.entityData.set(METAL, amount);
	}

	public float getRange() {
		return this.entityData.get(RANGE);
	}

	public void setRange(float range) {
		this.entityData.set(RANGE, range);
	}

	@Override
	public void upgrade() {
		super.upgrade();
		this.setMetal(this.getMetal() + 25);
	}

	@Override
	public int getMinEnergy() {
		return this.getOwnerUUID() != null ? TF2ConfigVars.dispenserUseEnergy : 0;
	}

	public boolean isItemStackAccepted(ItemStack stack) {
		return stack.isDamageableItem();
	}

	@Override
	public void drawFromBlock(BlockPos pos, BlockEntity ent, Direction facing) {
		super.drawFromBlock(pos, ent, facing);
		Direction front = Direction.getNearest(this.getX() - pos.getX() - 0.5, 0, this.getZ() - pos.getZ() - 0.5);
		IItemHandler handler = ent.getCapability(ForgeCapabilities.ITEM_HANDLER, facing.getOpposite()).orElse(null);
		if (handler != null) {
			if (front == facing || front == facing.getOpposite())
				for (int i = 0; i < items.getSlots(); i++) {
					if (items.getStackInSlot(i).isDamageableItem() && items.getStackInSlot(i).getDamageValue() == 0)
						items.insertItem(i,
								ItemHandlerHelper.insertItem(handler, items.extractItem(i, 1, false), false), false);
				}
			else {
				for (int i = 0; i < handler.getSlots(); i++) {
					handler.insertItem(i, ItemHandlerHelper.insertItem(items, handler.extractItem(i, 1, false), false),
							false);
				}
			}
		}
	}

	@Override
	public boolean shouldUseBlocks() {
		return true;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return null;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return TF2Sounds.MOB_DISPENSER_DEATH;
	}

	@Override
	public int getIronDrop() {
		return 0 + this.getBuildingLevel();
	}

	@Override
	public void addAdditionalSaveData(CompoundTag par1NBTTagCompound) {
		super.addAdditionalSaveData(par1NBTTagCompound);

		par1NBTTagCompound.put("Items", this.items.serializeNBT());
		par1NBTTagCompound.putShort("Metal", (short) this.getMetal());

		par1NBTTagCompound.putShort("Food", (short) food);
		ListTag fill = new ListTag();
		for (Entry<ItemStack, Float> entry : this.fillMeter.entrySet()) {
			CompoundTag values = new CompoundTag();
			values.put("Item", entry.getKey().serializeNBT());
			values.putFloat("Fill", entry.getValue());
			fill.add(values);
		}
		par1NBTTagCompound.put("Fill", fill);
		par1NBTTagCompound.putFloat("Range", this.getRange());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag par1NBTTagCompound) {
		super.readAdditionalSaveData(par1NBTTagCompound);

		this.items.deserializeNBT(par1NBTTagCompound.getCompound("Items"));
		this.setMetal(par1NBTTagCompound.getShort("Metal"));
		this.food = par1NBTTagCompound.getShort("Food");
		this.setRange(par1NBTTagCompound.getShort("Range"));
		ListTag fill = par1NBTTagCompound.getList("Fill", 10);
		for (int i = 0; i < fill.size(); i++) {
			CompoundTag values = (CompoundTag) fill.get(i);
			this.fillMeter.put(ItemStack.of(values.getCompound("Item")), values.getFloat("Fill"));
		}
	}

	@Override
	protected void dropCustomDeathLoot(DamageSource damageSource, int lootingModifier, boolean wasRecentlyHit) {
		super.dropCustomDeathLoot(damageSource, lootingModifier, wasRecentlyHit);
		for (int i = 0; i < this.items.getSlots(); i++) {
			this.spawnAtLocation(this.items.getStackInSlot(i), 0);
		}
	}

	@Override
	public int getBuildingID() {
		return 1;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderGUI(BufferBuilder renderer, Tessellator tessellator, Player player, int width, int height,
			GuiComponent gui) {
		// GlStateManager.color(1.0F, 1.0F, 1.0F, 0.7F);
		// LegacyGui.drawTexturedModalRect(event.getResolution().getScaledWidth()/2-64,
		// event.getResolution().getScaledHeight()/2+35, 0, 0, 128, 40);
		ClientProxy.setColor(TF2Util.getTeamColor(this), 0.7f, 0, 0.25f, 0.8f);
		LegacyGui.drawTexturedModalRect(20, 2, 0, 112, 124, 44);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.7F);
		LegacyGui.drawTexturedModalRect(0, 0, 0, 0, 144, 48);
		/*
		 * renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 - 72,
		 * event.getResolution().getScaledHeight() / 2 + 76, 0.0D).tex(0.0D,
		 * 0.1875D).endVertex(); renderer.pos(event.getResolution().getScaledWidth() / 2
		 * + 72, event.getResolution().getScaledHeight() / 2 + 76, 0.0D).tex(0.5625D,
		 * 0.1875D).endVertex(); renderer.pos(event.getResolution().getScaledWidth() / 2
		 * + 72, event.getResolution().getScaledHeight() / 2 + 28, 0.0D).tex(0.5625D,
		 * 0D).endVertex(); renderer.pos(event.getResolution().getScaledWidth() / 2 -
		 * 72, event.getResolution().getScaledHeight() / 2 + 28, 0.0D).tex(0.0D,
		 * 0D).endVertex(); tessellator.draw();
		 */

		renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		renderer.pos(19, 48, 0.0D).tex(0.75D, 0.75D).endVertex();
		renderer.pos(65, 48, 0.0D).tex(0.9375D, 0.75D).endVertex();
		renderer.pos(65, 0, 0.0D).tex(0.9375D, 0.5625D).endVertex();
		renderer.pos(19, 0, 0.0D).tex(0.75D, 0.5625D).endVertex();
		tessellator.draw();

		if (!this.isAlive())
			return;

		renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		renderer.pos(67, 22, 0.0D).tex(0.9375D, 0.1875D).endVertex();
		renderer.pos(83, 22, 0.0D).tex(1D, 0.1875D).endVertex();
		renderer.pos(83, 6, 0.0D).tex(1D, 0.125D).endVertex();
		renderer.pos(67, 6, 0.0D).tex(0.9375D, 0.125D).endVertex();
		tessellator.draw();

		double imagePos = this.getBuildingLevel() == 1 ? 0.3125D : this.getBuildingLevel() == 2 ? 0.375D : 0.4375D;
		renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		renderer.pos(50, 18, 0.0D).tex(0.9375D, 0.0625D + imagePos).endVertex();
		renderer.pos(66, 18, 0.0D).tex(1D, 0.0625D + imagePos).endVertex();
		renderer.pos(66, 2, 0.0D).tex(1D, imagePos).endVertex();
		renderer.pos(50, 2, 0.0D).tex(0.9375D, imagePos).endVertex();
		tessellator.draw();

		if (this.getBuildingLevel() < 3) {
			renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
			renderer.pos(67, 42, 0.0D).tex(0.9375D, 0.125D).endVertex();
			renderer.pos(83, 42, 0.0D).tex(1D, 0.125D).endVertex();
			renderer.pos(83, 26, 0.0D).tex(1D, 0.0625).endVertex();
			renderer.pos(67, 26, 0.0D).tex(0.9375D, 0.0625).endVertex();
			tessellator.draw();
		}
		float health = this.getHealth() / this.getMaxHealth();
		if (health > 0.33f) {
			GlStateManager.color(0.9F, 0.9F, 0.9F, 1F);
		} else {
			GlStateManager.color(0.85F, 0.0F, 0.0F, 1F);
		}
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		for (int i = 0; i < health * 8; i++) {

			renderer.begin(7, DefaultVertexFormats.POSITION);
			renderer.pos(19, 39 - i * 5, 0.0D).endVertex();
			renderer.pos(9, 39 - i * 5, 0.0D).endVertex();
			renderer.pos(9, 43 - i * 5, 0.0D).endVertex();
			renderer.pos(19, 43 - i * 5, 0.0D).endVertex();
			tessellator.draw();
		}

		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.33F);
		renderer.begin(7, DefaultVertexFormats.POSITION);
		renderer.pos(85, 21, 0.0D).endVertex();
		renderer.pos(140, 21, 0.0D).endVertex();
		renderer.pos(140, 7, 0.0D).endVertex();
		renderer.pos(85, 7, 0.0D).endVertex();
		tessellator.draw();

		if (this.getBuildingLevel() < 3) {
			renderer.begin(7, DefaultVertexFormats.POSITION);
			renderer.pos(85, 41, 0.0D).endVertex();
			renderer.pos(140, 41, 0.0D).endVertex();
			renderer.pos(140, 27, 0.0D).endVertex();
			renderer.pos(85, 27, 0.0D).endVertex();
			tessellator.draw();
		}

		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.85F);
		renderer.begin(7, DefaultVertexFormats.POSITION);
		renderer.pos(85, 21, 0.0D).endVertex();
		renderer.pos(85 + this.getMetal() * 0.1375D, 21, 0.0D).endVertex();
		renderer.pos(85 + this.getMetal() * 0.1375D, 7, 0.0D).endVertex();
		renderer.pos(85, 7, 0.0D).endVertex();
		tessellator.draw();

		if (this.getBuildingLevel() < 3) {
			renderer.begin(7, DefaultVertexFormats.POSITION);
			renderer.pos(85, 41, 0.0D).endVertex();
			renderer.pos(85 + this.getProgress() * 0.275D, 41, 0.0D).endVertex();
			renderer.pos(85 + this.getProgress() * 0.275D, 27, 0.0D).endVertex();
			renderer.pos(85, 27, 0.0D).endVertex();
			tessellator.draw();
		}
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
}
