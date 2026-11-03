package rafradek.TF2weapons.entity.mercenary;

import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2PlayerCapability;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.client.audio.TF2Sounds;
import rafradek.TF2weapons.entity.ai.EntityAINearestChecked;
import rafradek.TF2weapons.entity.ai.EntityAISeek;
import rafradek.TF2weapons.entity.building.EntityBuilding;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemKnife;
import rafradek.TF2weapons.item.ItemMonsterPlacerPlus;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2DamageSource;
import rafradek.TF2weapons.util.TF2Util;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;

public class EntitySaxtonHale extends PathfinderMob implements INpc, Merchant {

	public Player trader;
	public MerchantOffers tradeOffers;
	public static List<MerchantOffer> addRecipes = new ArrayList<>();
	public static List<MerchantOffer> removeRecipes = new ArrayList<>();
	public float rage;
	public boolean hostile;
	public boolean superJump;
	public int jumpCooldown;
	public boolean endangered;
	public int lastWeekCheck;

	private int targetAirborneTicks;

	private int noPathTime = 0;
	private final ServerBossEvent bossInfo = (new ServerBossEvent(this.getHoverName(), BossEvent.Color.PURPLE,
			BossEvent.Overlay.PROGRESS));

	public EntitySaxtonHale(Level world) {
		super(world);
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.1F, false));
		this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, EntityTF2Character.class, 8.0F));
		this.goalSelector.addGoal(7, new EntityAISeek(this));
		this.targetSelector.addGoal(2, new HurtByTargetGoal(this, true));
		this.xpReward = 1500;
		this.maxUpStep = 1f;
		this.setSize(0.6F, 1.99F);
	}

	@Override
	public void setCustomer(Player player) {
		this.trader = player;
	}

	@Override
	public Player getCustomer() {
		return trader;
	}

	@Override
	public MerchantOffers getRecipes(Player player) {
		if (this.tradeOffers == null || this.level.getGameTime() / 96000L != this.lastWeekCheck)
			makeOffers();
		MerchantOffers list = new MerchantOffers();
		list.addAll(this.tradeOffers);
		if (!TF2ConfigVars.disableInvasionItems) {
			for (int i = 0; i <= TF2PlayerCapability.get(player).maxInvasionBeaten; i++) {
				if (!(i == 4 && ((ServerPlayer) player).getStatFile().readStat(TF2weapons.robotsKilled) < 2000)
						&& i != InvasionEvent.DIFFICULTY.length)
					this.addTradeOffer(TF2Util.metaStack(TF2weapons.itemEventMaker, 1, i), 27 + i * 9, list, i + 1);
			}
		}
		return list;
	}

	@Override
	protected ResourceLocation getLootTable() {
		return TF2weapons.lootHale;
	}

	public void makeOffers() {
		this.lastWeekCheck = (int) (this.level.getGameTime() / 96000L);
		this.tradeOffers = new MerchantOffers();
		this.tradeOffers.addAll(addRecipes);
		this.tradeOffers.add(new MerchantOffer(TF2Util.metaStack(TF2weapons.itemTF2, 5, 2), ItemStack.EMPTY,
				TF2Util.metaStack(TF2weapons.itemTF2, 1, 7), 0, 100));
		int weaponCount = 13 + this.random.nextInt(2);
		List<ItemStack> weapons = ItemFromData.getRandomWeapons(this.random, ItemFromData.VISIBLE_WEAPON, weaponCount);
		for (int i = 0; i < weaponCount; i++) {
			ItemStack item = weapons.get(i);
			int cost = ItemFromData.getData(item).getInt(PropertyType.COST);
			this.addTradeOffer(item, cost);
		}
		int hatCount = 2 + this.random.nextInt(3);

		for (int i = 0; i < hatCount; i++) {

			ItemStack item = ItemFromData.getRandomWeaponOfType("cosmetic", this.random, false);
			int cost = ItemFromData.getData(item).getInt(PropertyType.COST);
			this.addTradeOffer(item, cost);
		}

		for (MerchantOffer toRemove : removeRecipes) {
			this.tradeOffers.removeIf(recipe -> recipe.getItemToBuy().sameItem(toRemove.getItemToBuy()));
		}

		/*
		 * ArrayList<TF2Attribute> list = new
		 * ArrayList<>(Arrays.asList(TF2Attribute.attributes)); list.removeIf(attr ->
		 * attr == null || attr.perKill == 0); for (int i = 0; i < 3; i++) { int level =
		 * 0; float rand = this.rand.nextFloat(); if (rand < 0.02) level = 2; else if
		 * (rand < 0.2) level = 1; ItemStack item = new
		 * ItemStack(TF2weapons.itemKillstreak, 1,
		 * list.get(this.rand.nextInt(list.size())).id + level << 9); int cost = level *
		 * 24; this.addTradeOffer(item, cost); }
		 */
	}

	private void addTradeOffer(ItemStack toBuy, int cost) {
		cost *= TF2ConfigVars.costMult;
		ItemStack ingot = TF2Util.metaStack(TF2weapons.itemTF2, cost / 9, 2);
		ItemStack nugget = TF2Util.metaStack(TF2weapons.itemTF2, cost % 9, 6);
		this.tradeOffers.add(new MerchantOffer(ingot.getCount() > 0 ? ingot : nugget,
				nugget.getCount() > 0 ? nugget : ItemStack.EMPTY, toBuy, 0, 100));
	}

	private void addTradeOffer(ItemStack toBuy, int cost, MerchantOffers list, int index) {
		cost *= TF2ConfigVars.costMult;
		ItemStack ingot = TF2Util.metaStack(TF2weapons.itemTF2, cost / 9, 2);
		ItemStack nugget = TF2Util.metaStack(TF2weapons.itemTF2, cost % 9, 6);
		list.add(index, new MerchantOffer(ingot.getCount() > 0 ? ingot : nugget,
				nugget.getCount() > 0 ? nugget : ItemStack.EMPTY, toBuy, 0, 100));
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.isInvulnerableTo(source))
			return false;
		else if (super.hurt(source, amount)) {
			if (source == DamageSource.DROWN || source == DamageSource.LAVA) {
				this.superJump = true;
				this.jump();
			}
			Entity entity = source.getEntity();
			if (entity instanceof ServerPlayer)
				this.bossInfo.addPlayer((ServerPlayer) entity);
			this.rage += amount / 100f;
			if (source instanceof TF2DamageSource && ((TF2DamageSource) source).getCritical() == 2
					&& !((TF2DamageSource) source).getWeapon().isEmpty()
					&& ((TF2DamageSource) source).getWeapon().getItem() instanceof ItemKnife)
				this.playSound(TF2Sounds.MOB_SAXTON_STAB, 2.5F, 1f);
			return this.getVehicle() != entity && this.getVehicle() != entity ? true : true;

		} else
			return false;
	}

	public void setHostile() {
		this.targetSelector.addGoal(1, new EntityAINearestChecked<>(this, LivingEntity.class, true,
				false, input -> input instanceof Player || input instanceof EntityTF2Character, true, false));
		this.hostile = true;
	}

	@Override
	public void setRecipes(MerchantOffers recipeList) {
		this.tradeOffers = recipeList;
	}

	@Override
	public void useRecipe(MerchantOffer recipe) {}

	@Override
	public void verifySellingItem(ItemStack stack) {
		if (!stack.isEmpty() && stack.hasTag())
			stack.getTag().putBoolean("Bought", true);
		/*
		 * if (this.trader != null && !stack.isEmpty() &&stack.getItem() instanceof
		 * ItemWeapon) this.trader.addStat(TF2Achievements.MANN_CO_MADE);
		 */

	}

	@Override
	public void travel(float m1, float m2, float m3) {
		float move = this.getAIMoveSpeed();
		super.travel(m1 / move, m2, m3 / move);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (!this.level.isClientSide) {
			this.jumpCooldown--;

			if (this.random.nextInt(20) == 0) {
				this.getAttribute(Attributes.MOVEMENT_SPEED)
						.setBaseValue(0.12 * TF2Util.lerp(1.5f, 1, this.getHealth() / this.getMaxHealth()));
			}
			this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
			// System.out.println("Has path: "+this.getNavigator().noPath());

			if (this.getTarget() == null)
				this.heal(0.35f);
			else if (this.getTarget().isAlive()) {

				if (!this.getTarget().onGround) {
					this.targetAirborneTicks++;
				}

				if (!this.onGround) {
					Vec3 forward = new Vec3(this.getTarget().getX() - this.getX(), 0.,
							this.getTarget().getZ() - this.getZ()).normalize().scale(this.getAIMoveSpeed() * 0.42);
					this.setDeltaMovement(this.getDeltaMovement().x + (forward.x), this.getDeltaMovement().y, this.getDeltaMovement().z);
					this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z + (forward.z));
				}

				List<AABB> boxes = this.level.getCollisionBoxes(this, getBoundingBox().inflate(1, 0, 1));
				boolean obscuredView = false;
				for (AABB box : boxes)
					if (box.clip(this.position().add(0, this.getEyeHeight(), 0),
							this.position().add(this.getVectorForRotation(0, this.yHeadRot).add(0,
									this.getEyeHeight(), 0))) != null) {
						obscuredView = true;
						break;
					}

				if (this.onGround && this.jumpCooldown <= 0) {
					if (obscuredView)
						this.superJump();
					else if (this.targetAirborneTicks > 25) {
						double height = TF2Util.getHeightAboveGround(this.getTarget(), world, true);
						if (height > 2. && height < 30.)
							this.superJump();
						else if (height >= 30.)
							this.heal(0.35f);
					}
				}
			}

			if (this.tickCount % 3 == 0) {
				if (this.getTarget() != null && this.getTarget().isAlive()
						&& this.getNavigation().getPathToEntityLiving(this.getTarget()) == null) {
					this.noPathTime += 1;
					Vec3 forward = this.getVectorForRotation(0, this.yHeadRot);
					if (this.noPathTime > 2) {
						for (int x = (int) this.getX(); x <= this.getX() + forward.x; x++) {
							for (int y = (int) Math.max(this.getTarget().getY(), this.getY() - 1); y <= this.getY()
									+ 2; y++) {
								for (int z = (int) this.getZ(); z <= this.getZ() + forward.z; z++) {
									BlockPos pos = new BlockPos(x, y, z);
									if (this.level.getBlockState(pos).getBlockHardness(world, pos) != -1)
										this.level.destroyBlock(pos, true);
								}
							}
						}
						this.noPathTime = 1;
					}
				} else
					this.noPathTime = 0;
			}
			if (this.rage > 1) {
				List<LivingEntity> list = this.level.getEntitiesOfClass(LivingEntity.class,
						this.getBoundingBox().inflate(12, 12, 12), input -> !(input instanceof EntitySaxtonHale)
								&& !(input instanceof Player && ((Player) input).isCreative())
								&& input.distanceToSqr(EntitySaxtonHale.this) < 144);
				if (!list.isEmpty()) {
					this.rage = 0;
					this.playSound(TF2Sounds.MOB_SAXTON_RAGE, 2.5F, 1F);
					for (LivingEntity living : list)
						TF2Util.stun(living, 160, false);
					this.superJump();
				}
			}
		}
	}

	@Override
	public void setTarget(LivingEntity living) {
		super.setTarget(living);
		if (!endangered) {
			this.endangered = true;
			this.playSound(TF2Sounds.MOB_SAXTON_START, 2F, 1F);
		}
	}

	@Override
	protected SoundEvent getSwimSound() {
		return SoundEvents.ENTITY_HOSTILE_SWIM;
	}

	@Override
	protected SoundEvent getSplashSound() {
		return SoundEvents.ENTITY_HOSTILE_SPLASH;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.ENTITY_HOSTILE_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return TF2Sounds.MOB_SAXTON_DEATH;
	}

	@Override
	public boolean isNonBoss() {
		return !hostile;
	}

	@Override
	public Team getTeam() {
		return this.hostile ? this.level.getScoreboard().getTeam("TF2Bosses") : null;
	}

	@Override
	public void fall(float distance, float damageMultiplier) {
		super.fall(distance, 0);
	}

	@Override
	protected float getJumpUpwardsMotion() {
		if (superJump)
			return 2.7F;
		return 0.7F;
	}

	public void superJump() {
		if (this.jumpCooldown > 0)
			return;

		this.playSound(TF2Sounds.MOB_SAXTON_JUMP, 2F, 1F);
		this.setDeltaMovement(this.getDeltaMovement().x, (0), this.getDeltaMovement().z);

		this.jumpCooldown = 25;

		this.superJump = true;
		super.jump();
		this.superJump = false;
	}

	@Override
	public void jump() {
		/*
		 * if(this.getAttackTarget()!=null&&this.getAttackTarget().posY-this. posY>=3){
		 * this.superJump=true; }
		 */

	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getAttributes().registerAttribute(Attributes.ATTACK_DAMAGE);
		this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(50.0D);
		this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0D);
		this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.8D);
		this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.12D);
		this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(20D);
	}

	@Override
	public boolean attackEntityAsMob(Entity entityIn) {
		float f = (float) this.getAttribute(Attributes.ATTACK_DAMAGE).getValue();
		int i = 0;

		if (entityIn instanceof LivingEntity) {
			f += EnchantmentHelper.getModifierForCreature(this.getMainHandItem(),
					((LivingEntity) entityIn).getMobType());
			i += EnchantmentHelper.getKnockbackModifier(this);
		}

		boolean flag = entityIn.hurt(DamageSource.causeMobDamage(this), f);

		if (flag) {
			if (i > 0 && entityIn instanceof LivingEntity) {
				((LivingEntity) entityIn).knockback(this, i * 0.5F, Mth.sin(this.getYRot() * 0.017453292F),
						(-Mth.cos(this.getYRot() * 0.017453292F)));
				this.setDeltaMovement(this.getDeltaMovement().x * (0.6D), this.getDeltaMovement().y, this.getDeltaMovement().z);
				this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z * (0.6D));
			}

			int j = EnchantmentHelper.getFireAspectModifier(this);

			if (j > 0)
				entityIn.setSecondsOnFire(j * 4);

			if (entityIn instanceof Player) {
				Player entityplayer = (Player) entityIn;
				ItemStack itemstack = this.getMainHandItem();
				ItemStack itemstack1 = entityplayer.isUsingItem() ? entityplayer.getUseItem() : null;

				if (!itemstack.isEmpty() && itemstack1 != null && itemstack.getItem() instanceof AxeItem
						&& itemstack1.getItem() == Items.SHIELD) {
					float f1 = 0.25F + EnchantmentHelper.getEfficiencyModifier(this) * 0.05F;

					if (this.random.nextFloat() < f1) {
						entityplayer.getCooldowns().setCooldown(Items.SHIELD, 100);
						this.level.broadcastEntityEvent(entityplayer, (byte) 30);
					}
				}
			}

			this.applyEnchantments(this, entityIn);

			if (entityIn instanceof LivingEntity && ((LivingEntity) entityIn).getHealth() <= 0)
				if (entityIn instanceof EntityBuilding)
					this.playSound(TF2Sounds.MOB_SAXTON_DESTROY, 2.2F, 1f);
				else
					this.playSound(TF2Sounds.MOB_SAXTON_KILL, 2.2F, 1f);
		}

		return flag;
	}

	@Override
	public void readAdditionalSaveData(CompoundTag par1NBTTagCompound) {
		super.readAdditionalSaveData(par1NBTTagCompound);
		if (par1NBTTagCompound.getBoolean("Hostile"))
			this.setHostile();
		if (par1NBTTagCompound.contains("Offers")) {
			this.tradeOffers = new MerchantOffers();
			this.tradeOffers.readRecipiesFromTags(par1NBTTagCompound.getCompound("Offers"));
		}
		this.endangered = par1NBTTagCompound.getBoolean("Endangered");
		this.lastWeekCheck = par1NBTTagCompound.getInt("LastWeek");
	}

	@Override
	public void addAdditionalSaveData(CompoundTag par1NBTTagCompound) {
		if (this.tradeOffers != null)
			par1NBTTagCompound.put("Offers", this.tradeOffers.getRecipiesAsTags());
		par1NBTTagCompound.putBoolean("Hostile", hostile);
		par1NBTTagCompound.putBoolean("Endangered", this.endangered);
		par1NBTTagCompound.putInt("LastWeek", (short) this.lastWeekCheck);
	}

	@Override
	protected boolean canDespawn() {
		return false;
	}

	@Override
	public boolean processInteract(Player player, InteractionHand hand) {
		if (!(player.getMainHandItem() != null
				&& player.getMainHandItem().getItem() instanceof ItemMonsterPlacerPlus)
				&& this.getTarget() == null && this.isAlive() && !this.isTrading() && !this.isBaby()
				&& !player.isShiftKeyDown()) {
			boolean canTrade = player.getTeam() != null || player.getAbilities().instabuild || !TF2ConfigVars.canJoin;
			if (this.level.isClientSide && !canTrade)
				ClientProxy.displayScreenJoinTeam();
			else if (!this.level.isClientSide && (canTrade) && (this.tradeOffers == null || !this.tradeOffers.isEmpty())) {
				this.setCustomer(player);
				player.displayVillagerTradeGui(this);
			}

			player.addStat(StatList.TALKED_TO_VILLAGER);
			return true;
		} else
			return super.processInteract(player, hand);
	}

	public boolean isTrading() {
		return this.trader != null;
	}

	@Override
	public void addTrackingPlayer(ServerPlayer player) {
		super.addTrackingPlayer(player);
		if (this.hostile)
			this.bossInfo.addPlayer(player);
	}

	@Override
	public void removeTrackingPlayer(ServerPlayer player) {
		super.removeTrackingPlayer(player);
		this.bossInfo.removePlayer(player);
	}

	@Override
	public Level getLevel() {
		return this.level;
	}

	@Override
	public BlockPos getPos() {
		return this.getBlockPos();
	}
}
