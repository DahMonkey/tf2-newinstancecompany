package rafradek.TF2weapons.item;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.entity.boss.EntityHHH;
import rafradek.TF2weapons.entity.boss.EntityMerasmus;
import rafradek.TF2weapons.entity.boss.EntityMonoculus;
import rafradek.TF2weapons.entity.building.EntityBuilding;
import rafradek.TF2weapons.entity.building.EntityDispenser;
import rafradek.TF2weapons.entity.building.EntitySentry;
import rafradek.TF2weapons.entity.building.EntityTeleporter;
import rafradek.TF2weapons.entity.mercenary.*;
import rafradek.TF2weapons.util.TF2Class;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import rafradek.TF2weapons.util.TF2Util;

@SuppressWarnings("deprecation")
public class ItemMonsterPlacerPlus extends Item {

	public ItemMonsterPlacerPlus() {
		this.setHasSubtypes(true);
		this.setCreativeTab(TF2weapons.tabspawnertf2);
	}

	/**
	 * Callback for item usage. If the item does something special on right
	 * clicking, he will have one of those. Return True if something happen and
	 * false if it don't. This is for ITEMS, not BLOCKS
	 */
	@Override
	public InteractionResult useOn(Player player, Level world, BlockPos pos, InteractionHand hand,
			Direction facing, float hitX, float hitY, float hitZ) {

		ItemStack stack = player.getItemInHand(hand);
		if (world.isClientSide)
			return InteractionResult.SUCCESS;
		else if (!player.canPlayerEdit(pos.move(facing), facing, stack))
			return InteractionResult.FAIL;
		else {
			BlockState iblockstate = world.getBlockState(pos);

			pos = pos.move(facing);
			double d0 = 0.0D;

			if (facing == Direction.UP && iblockstate.getBlock() instanceof FenceBlock)
				d0 = 0.5D;

			boolean hastag = stack.getTag() != null && stack.getTag().contains("SavedEntity");

			LivingEntity entity = spawnCreature(player, world, stack.getDamageValue(), pos.getX() + 0.5D,
					pos.getY() + d0, pos.getZ() + 0.5D,
					hastag ? stack.getTag().getCompound("SavedEntity") : null);

			if (entity != null) {
				if (entity instanceof LivingEntity && stack.hasCustomHoverName())
					entity.setCustomName(stack.getHoverName());

				if (!player.getAbilities().instabuild)
					stack.shrink(1);
				if (entity instanceof EntityBuilding) {
					if (entity instanceof EntityTeleporter || !(player.isCreative() && player.isShiftKeyDown()))
						((EntityBuilding) entity).setOwner(player);
					if (hastag) {
						((EntityBuilding) entity).setConstructing(true);
						((EntityBuilding) entity).redeploy = true;
					}
					entity.setYRot((player.yHeadRot));
					entity.yBodyRot = player.yHeadRot;
					entity.yHeadRot = player.yHeadRot;
					if (entity instanceof EntityTeleporter)
						((EntityTeleporter) entity).setExit(stack.getDamageValue() > 23);
				}
			}

			return InteractionResult.SUCCESS;
		}
	}

	/**
	 * Applies the data in the EntityTag tag of the given ItemStack to the given
	 * Entity.
	 */

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack itemStackIn = player.getItemInHand(hand);
		if (world.isClientSide)
			return new InteractionResultHolder<>(InteractionResult.PASS, itemStackIn);
		else {
			HitResult raytraceresult = this.rayTrace(world, player, true);

			if (raytraceresult != null && raytraceresult.getType() == HitResult.Type.BLOCK) {
				BlockPos blockpos = raytraceresult.getBlockPos();

				if (!(world.getBlockState(blockpos).getBlock() instanceof LiquidBlock))
					return new InteractionResultHolder<>(InteractionResult.PASS, itemStackIn);
				else if (world.isBlockModifiable(player, blockpos)
						&& player.canPlayerEdit(blockpos, raytraceresult.getDirection(), itemStackIn)) {

					boolean hastag = itemStackIn.getTag() != null
							&& itemStackIn.getTag().contains("SavedEntity");

					LivingEntity entity = spawnCreature(player, world, itemStackIn.getDamageValue(),
							blockpos.getX() + 0.5D, blockpos.getY() + 0.5D, blockpos.getZ() + 0.5D,
							hastag ? itemStackIn.getTag().getCompound("SavedEntity") : null);

					if (entity == null)
						return new InteractionResultHolder<>(InteractionResult.PASS, itemStackIn);
					else {
						if (entity instanceof LivingEntity && itemStackIn.hasCustomHoverName())
							entity.setCustomName(itemStackIn.getHoverName());

						if (!player.getAbilities().instabuild)
							itemStackIn.shrink(1);
						if (entity instanceof EntityBuilding) {
							if (entity instanceof EntityTeleporter || !(player.isCreative() && player.isShiftKeyDown()))
								((EntityBuilding) entity).setOwner(player);
							if (hastag) {
								((EntityBuilding) entity).setConstructing(true);
								((EntityBuilding) entity).redeploy = true;
							}
							if (entity instanceof EntitySentry && itemStackIn.hasTag()
									&& itemStackIn.getTag().getBoolean("Mini"))
								((EntitySentry) entity).setMini(true);

							entity.setYRot((player.yHeadRot));
							entity.yBodyRot = player.yHeadRot;
							entity.yHeadRot = player.yHeadRot;

							/*
							 * if(entity instanceof EntityTeleporter){ ((EntityTeleporter)
							 * entity).setExit(itemStackIn.getItemDamage()>23); }
							 */
						}
						player.addStat(StatList.getObjectUseStats(this));
						return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStackIn);
					}
				} else
					return new InteractionResultHolder<>(InteractionResult.FAIL, itemStackIn);
			} else
				return new InteractionResultHolder<>(InteractionResult.PASS, itemStackIn);
		}
	}

	public static Mob spawnCreature(Entity spawner, Level par0World, int par1, double par2, double par4,
			double par6, CompoundTag nbtdata) {
		Mob entity = null;

		for (int j = 0; j < 1; ++j) {
			int team = par1 % 2;
			if (par1 < 18 || (par1 >= 36 && par1 < 45)) {
				switch (par1 % 9) {
				case 0:
					entity = new EntityScout(par0World);
					break;
				case 1:
					entity = new EntitySoldier(par0World);
					break;
				case 2:
					entity = new EntityPyro(par0World);
					break;
				case 3:
					entity = new EntityDemoman(par0World);
					break;
				case 4:
					entity = new EntityHeavy(par0World);
					break;
				case 5:
					entity = new EntityEngineer(par0World);
					break;
				case 6:
					entity = new EntityMedic(par0World);
					break;
				case 7:
					entity = new EntitySniper(par0World);
					break;
				case 8:
					entity = new EntitySpy(par0World);
					break;
				}
				team = par1 / 9;
				if (par1 >= 36)
					team = 2;
			} else if (par1 / 2 == 9)
				entity = new EntitySentry(par0World);
			else if (par1 / 2 == 10)
				entity = new EntityDispenser(par0World);
			else if (par1 / 2 == 11)
				entity = new EntityTeleporter(par0World);
			else if (par1 / 2 == 13)
				entity = new EntitySaxtonHale(par0World);
			else if (par1 == 28)
				entity = new EntityMonoculus(par0World);
			else if (par1 == 29)
				entity = new EntityHHH(par0World);
			else if (par1 == 30)
				entity = new EntityMerasmus(par0World);
			if (entity != null) {
				Mob entityliving = entity;
				if (nbtdata != null)
					entityliving.readFromNBT(nbtdata);
				// System.out.println("read");
				entity.moveTo(par2, par4, par6,
						Mth.wrapDegrees(par0World.random.nextFloat() * 360.0F), 0.0F);
				entityliving.yHeadRot = entityliving.getYRot();
				entityliving.yBodyRot = entityliving.getYRot();
				entityliving.setPersistenceRequired();
				TF2CharacterAdditionalData data = new TF2CharacterAdditionalData();
				data.team = team;
				data.noEquipment = team < 2 && spawner != null && spawner.isShiftKeyDown();
				data.isGiant = team == 2 && spawner != null && spawner.isShiftKeyDown();
				if (nbtdata == null)
					entityliving.onInitialSpawn(par0World.getCurrentDifficultyAt(new BlockPos(entityliving)), data);
				entityliving.playLivingSound();
				if (entity instanceof EntityBuilding)
					((EntityBuilding) entity).setEntTeam(team);
				if (entity instanceof EntitySaxtonHale && par1 % 2 == 1)
					((EntitySaxtonHale) entity).setHostile();
				if (!par0World.getCollisionBoxes(entity, entity.getBoundingBox()).isEmpty())
					return null;
				par0World.addFreshEntity(entity);

			}

		}

		return entity;
	}

	@Override
	@OnlyIn(Dist.CLIENT)

	/**
	 * returns a list of items with the same ID, but different meta (eg: dye returns
	 * 16 items)
	 */
	public void fillItemCategory(CreativeModeTab par2CreativeTabs, NonNullList<ItemStack> par3List) {
		if (!this.isInCreativeTab(par2CreativeTabs))
			return;
		for (int i = 0; i < 18; i++)
			par3List.add(TF2Util.metaStack(this, 1, i));
		for (int i = 36; i < 45; i++)
			par3List.add(TF2Util.metaStack(this, 1, i));
		par3List.add(TF2Util.metaStack(this, 1, 26));
		par3List.add(TF2Util.metaStack(this, 1, 27));
		par3List.add(TF2Util.metaStack(this, 1, 28));
		par3List.add(TF2Util.metaStack(this, 1, 29));
		par3List.add(TF2Util.metaStack(this, 1, 30));

	}

	@Override
	public String getName(ItemStack p_77653_1_) {
		String s = ("" + TF2Util.translate(this.getDescriptionId() + ".name")).trim();
		int i = p_77653_1_.getDamageValue();
		String s1 = "hale";
		if (i < 18 || (i >= 36 && i < 45))
			s1 = TF2Class.getClass(i % 9).getName();
		if (p_77653_1_.getDamageValue() == 28)
			s1 = "monoculus";
		if (p_77653_1_.getDamageValue() == 29)
			s1 = "hhh";
		if (p_77653_1_.getDamageValue() == 30)
			s1 = "merasmus";
		s1 = TF2Util.translate("entity." + s1 + ".name");
		if (p_77653_1_.getDamageValue() == 27)
			s1 = s1.concat(" " + TF2Util.translate("item." + TF2weapons.MOD_ID + ".placer.hostile"));
		return s.concat(" " + s1);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level world, List<String> tooltip, TooltipFlag advanced) {
		if (stack.getDamageValue() < 18)
			tooltip.add("Hold " + KeyMapping.getDisplayString("key.sneak").get() + " to spawn with default equipment");
		if (stack.getDamageValue() >= 36 && stack.getDamageValue() < 45)
			tooltip.add("Hold " + KeyMapping.getDisplayString("key.sneak").get() + " to spawn a giant");
	}
}
