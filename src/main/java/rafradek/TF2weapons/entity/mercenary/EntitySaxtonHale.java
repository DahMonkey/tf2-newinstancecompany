package rafradek.TF2weapons.entity.mercenary;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import rafradek.TF2weapons.common.TF2Achievements;
import net.minecraft.world.entity.EntityType;
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

public class EntitySaxtonHale extends PathfinderMob implements net.minecraft.world.entity.npc.Npc, Merchant {

	private net.minecraft.world.entity.EntityDimensions legacySize;

	/** 1.12 setSize: overrides the entity type's size for this entity. */
	public void setSize(float width, float height) {
		this.legacySize = net.minecraft.world.entity.EntityDimensions.scalable(width, height);
		this.refreshDimensions();
	}

	@Override
	public net.minecraft.world.entity.EntityDimensions getDimensions(net.minecraft.world.entity.Pose pose) {
		return this.legacySize != null ? this.legacySize : super.getDimensions(pose);
	}

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
	private final ServerBossEvent bossInfo = (new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.PURPLE,
			BossEvent.BossBarOverlay.PROGRESS));

	public EntitySaxtonHale(Level world) {
		this(TF2weapons.HALE, world);
	}

	public EntitySaxtonHale(EntityType<? extends EntitySaxtonHale> type, Level world) {
		super(type, world);
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.1F, false));
		this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, EntityTF2Character.class, 8.0F));
		this.goalSelector.addGoal(7, new EntityAISeek(this));
		this.targetSelector.addGoal(2, new HurtByTargetGoal(this).setAlertOthers());
		this.xpReward = 1500;
		this.maxUpStep = 1f;
		this.setSize(0.6F, 1.99F);
	}

	@Override
	public void setTradingPlayer(Player player) {
		this.trader = player;
	}

	@Override
	public Player getTradingPlayer() {
		return trader;
	}

	@Override
	public MerchantOffers getOffers() {
		Player player = this.trader;
		if (this.tradeOffers == null || this.level.getGameTime() / 96000L != this.lastWeekCheck)
			makeOffers();
		MerchantOffers list = new MerchantOffers();
		list.addAll(this.tradeOffers);
		if (!TF2ConfigVars.disableInvasionItems && player instanceof ServerPlayer) {
			for (int i = 0; i <= TF2PlayerCapability.get(player).maxInvasionBeaten; i++) {
				if (!(i == 4 && TF2Achievements.read((ServerPlayer) player, TF2weapons.robotsKilled) < 2000)
						&& i != InvasionEvent.DIFFICULTY.length)
					this.addTradeOffer(TF2Util.metaStack(TF2weapons.itemEventMaker, 1, i), 27 + i * 9, list, i + 1);
			}
		}
		return list;
	}

	@Override
	public ResourceLocation getLootTable() {
		return TF2weapons.lootHale;
	}

	public void makeOffers() {
		this.lastWeekCheck = (int) (this.level.getGameTime() / 96000L);
		this.tradeOffers = new MerchantOffers();
		this.tradeOffers.addAll(addRecipes);
		this.tradeOffers.add(new MerchantOffer(TF2Util.metaStack(TF2weapons.itemTF2, 5, 2), ItemStack.EMPTY,
				TF2Util.metaStack(TF2weapons.itemTF2, 1, 7), 0, 100, 0, 0f));
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
			this.tradeOffers.removeIf(recipe -> recipe.getResult().sameItem(toRemove.getResult()));
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
				nugget.getCount() > 0 ? nugget : ItemStack.EMPTY, toBuy, 0, 100, 0, 0f));
	}

	private void addTradeOffer(ItemStack toBuy, int cost, MerchantOffers list, int index) {
		cost *= TF2ConfigVars.costMult;
		ItemStack ingot = TF2Util.metaStack(TF2weapons.itemTF2, cost / 9, 2);
		ItemStack nugget = TF2Util.metaStack(TF2weapons.itemTF2, cost % 9, 6);
		list.add(index, new MerchantOffer(ingot.getCount() > 0 ? ingot : nugget,
				nugget.getCount() > 0 ? nugget : ItemStack.EMPTY, toBuy, 0, 100, 0, 0f));
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (this.isInvulnerableTo(source))
			return false;
		else if (super.hurt(source, amount)) {
			if (source == DamageSource.DROWN || source == DamageSource.LAVA) {
				this.superJump = true;
				this.jumpFromGround();
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
	public void overrideOffers(MerchantOffers recipeList) {
		this.tradeOffers = recipeList;
	}

	@Override
	public void notifyTrade(MerchantOffer recipe) {}

	@Override
	public int getVillagerXp() {
		return 0;
	}

	@Override
	public void overrideXp(int xp) {
	}

	@Override
	public boolean showProgressBar() {
		return false;
	}

	@Override
	public SoundEvent getNotifyTradeSound() {
		return SoundEvents.VILLAGER_YES;
	}

	@Override
	public boolean isClientSide() {
		return this.level.isClientSide;
	}

	@Override
	public void notifyTradeUpdated(ItemStack stack) {
		if (!stack.isEmpty() && stack.hasTag())
			stack.getTag().putBoolean("Bought", true);
		/*
		 * if (this.trader != null && !stack.isEmpty() &&stack.getItem() instanceof
		 * ItemWeapon) this.trader.awardStat(TF2Achievements.MANN_CO_MADE);
		 */

	}

	@Override
	public void travel(Vec3 travel) {
		float move = this.getSpeed();
		super.travel(move == 0 ? travel : new Vec3(travel.x / move, travel.y, travel.z / move));
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
			this.bossInfo.setProgress(this.getHealth() / this.getMaxHealth());
			// System.out.println("Has path: "+this.getNavigator().noPath());

			if (this.getTarget() == null)
				this.heal(0.35f);
			else if (this.getTarget().isAlive()) {

				if (!this.getTarget().isOnGround()) {
					this.targetAirborneTicks++;
				}

				if (!this.onGround) {
					Vec3 forward = new Vec3(this.getTarget().getX() - this.getX(), 0.,
							this.getTarget().getZ() - this.getZ()).normalize().scale(this.getSpeed() * 0.42);
					this.setDeltaMovement(this.getDeltaMovement().x + (forward.x), this.getDeltaMovement().y, this.getDeltaMovement().z);
					this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z + (forward.z));
				}

				List<AABB> boxes = new java.util.ArrayList<>();
				for (net.minecraft.world.phys.shapes.VoxelShape shape : this.level.getBlockCollisions(this,
						getBoundingBox().inflate(1, 0, 1)))
					boxes.addAll(shape.toAabbs());
				boolean obscuredView = false;
				for (AABB box : boxes)
					if (box.clip(this.position().add(0, this.getEyeHeight(), 0),
							this.position().add(this.calculateViewVector(0, this.yHeadRot).add(0,
									this.getEyeHeight(), 0))).isPresent()) {
						obscuredView = true;
						break;
					}

				if (this.onGround && this.jumpCooldown <= 0) {
					if (obscuredView)
						this.superJump();
					else if (this.targetAirborneTicks > 25) {
						double height = TF2Util.getHeightAboveGround(this.getTarget(), this.level, true);
						if (height > 2. && height < 30.)
							this.superJump();
						else if (height >= 30.)
							this.heal(0.35f);
					}
				}
			}

			if (this.tickCount % 3 == 0) {
				if (this.getTarget() != null && this.getTarget().isAlive()
						&& this.getNavigation().createPath(this.getTarget(), 0) == null) {
					this.noPathTime += 1;
					Vec3 forward = this.calculateViewVector(0, this.yHeadRot);
					if (this.noPathTime > 2) {
						for (int x = (int) this.getX(); x <= this.getX() + forward.x; x++) {
							for (int y = (int) Math.max(this.getTarget().getY(), this.getY() - 1); y <= this.getY()
									+ 2; y++) {
								for (int z = (int) this.getZ(); z <= this.getZ() + forward.z; z++) {
									BlockPos pos = new BlockPos(x, y, z);
									if (this.level.getBlockState(pos).getDestroySpeed(this.level, pos) != -1)
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
		return SoundEvents.HOSTILE_SWIM;
	}

	@Override
	protected SoundEvent getSwimSplashSound() {
		return SoundEvents.HOSTILE_SPLASH;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.HOSTILE_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return TF2Sounds.MOB_SAXTON_DEATH;
	}

	@Override
	public boolean canChangeDimensions() {
		return !hostile;
	}

	@Override
	public Team getTeam() {
		return this.hostile ? this.level.getScoreboard().getPlayerTeam("TF2Bosses") : null;
	}

	@Override
	public boolean causeFallDamage(float distance, float damageMultiplier, DamageSource source) {
		return false;
	}

	@Override
	protected float getJumpPower() {
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
		super.jumpFromGround();
		this.superJump = false;
	}

	@Override
	protected void jumpFromGround() {
		/*
		 * if(this.getAttackTarget()!=null&&this.getAttackTarget().posY-this. posY>=3){
		 * this.superJump=true; }
		 */

	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.ATTACK_DAMAGE)
				.add(Attributes.FOLLOW_RANGE, 50.0D)
				.add(Attributes.MAX_HEALTH, 1000.0D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
				.add(Attributes.MOVEMENT_SPEED, 0.12D)
				.add(Attributes.ATTACK_DAMAGE, 20D);
	}

	@Override
	public boolean doHurtTarget(Entity entityIn) {
		float f = (float) this.getAttribute(Attributes.ATTACK_DAMAGE).getValue();
		int i = 0;

		if (entityIn instanceof LivingEntity) {
			f += EnchantmentHelper.getDamageBonus(this.getMainHandItem(),
					((LivingEntity) entityIn).getMobType());
			i += EnchantmentHelper.getKnockbackBonus(this);
		}

		boolean flag = entityIn.hurt(DamageSource.mobAttack(this), f);

		if (flag) {
			if (i > 0 && entityIn instanceof LivingEntity) {
				((LivingEntity) entityIn).knockback(i * 0.5F, Mth.sin(this.getYRot() * 0.017453292F),
						(-Mth.cos(this.getYRot() * 0.017453292F)));
				this.setDeltaMovement(this.getDeltaMovement().x * (0.6D), this.getDeltaMovement().y, this.getDeltaMovement().z);
				this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z * (0.6D));
			}

			int j = EnchantmentHelper.getFireAspect(this);

			if (j > 0)
				entityIn.setSecondsOnFire(j * 4);

			if (entityIn instanceof Player) {
				Player entityplayer = (Player) entityIn;
				ItemStack itemstack = this.getMainHandItem();
				ItemStack itemstack1 = entityplayer.isUsingItem() ? entityplayer.getUseItem() : null;

				if (!itemstack.isEmpty() && itemstack1 != null && itemstack.getItem() instanceof AxeItem
						&& itemstack1.getItem() == Items.SHIELD) {
					float f1 = 0.25F + EnchantmentHelper.getBlockEfficiency(this) * 0.05F;

					if (this.random.nextFloat() < f1) {
						entityplayer.getCooldowns().addCooldown(Items.SHIELD, 100);
						this.level.broadcastEntityEvent(entityplayer, (byte) 30);
					}
				}
			}

			this.doEnchantDamageEffects(this, entityIn);

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
			this.tradeOffers = new MerchantOffers(par1NBTTagCompound.getCompound("Offers"));
		}
		this.endangered = par1NBTTagCompound.getBoolean("Endangered");
		this.lastWeekCheck = par1NBTTagCompound.getInt("LastWeek");
	}

	@Override
	public void addAdditionalSaveData(CompoundTag par1NBTTagCompound) {
		super.addAdditionalSaveData(par1NBTTagCompound);
		if (this.tradeOffers != null)
			par1NBTTagCompound.put("Offers", this.tradeOffers.createTag());
		par1NBTTagCompound.putBoolean("Hostile", hostile);
		par1NBTTagCompound.putBoolean("Endangered", this.endangered);
		par1NBTTagCompound.putInt("LastWeek", (short) this.lastWeekCheck);
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (!(player.getMainHandItem() != null
				&& player.getMainHandItem().getItem() instanceof ItemMonsterPlacerPlus)
				&& this.getTarget() == null && this.isAlive() && !this.isTrading() && !this.isBaby()
				&& !player.isShiftKeyDown()) {
			boolean canTrade = player.getTeam() != null || player.getAbilities().instabuild || !TF2ConfigVars.canJoin;
			if (this.level.isClientSide && !canTrade)
				ClientProxy.displayScreenJoinTeam();
			else if (!this.level.isClientSide && (canTrade) && (this.tradeOffers == null || !this.tradeOffers.isEmpty())) {
				this.setTradingPlayer(player);
				this.openTradingScreen(player, this.getDisplayName(), 1);
			}

			player.awardStat(net.minecraft.stats.Stats.TALKED_TO_VILLAGER);
			return InteractionResult.sidedSuccess(this.level.isClientSide);
		} else
			return super.mobInteract(player, hand);
	}

	public boolean isTrading() {
		return this.trader != null;
	}

	@Override
	public void startSeenByPlayer(ServerPlayer player) {
		super.startSeenByPlayer(player);
		if (this.hostile)
			this.bossInfo.addPlayer(player);
	}

	@Override
	public void stopSeenByPlayer(ServerPlayer player) {
		super.stopSeenByPlayer(player);
		this.bossInfo.removePlayer(player);
	}
}
