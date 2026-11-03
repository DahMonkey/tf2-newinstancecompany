package rafradek.TF2weapons;

import net.minecraft.world.damagesource.CombatRules;
import java.util.function.Predicate;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraft.world.level.GameRules;
import javax.annotation.Nullable;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import rafradek.TF2weapons.util.TF2HitResult;
import rafradek.TF2weapons.common.TF2Achievements;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import io.netty.buffer.Unpooled;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.client.audio.TF2Sounds;
import rafradek.TF2weapons.common.MapList;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.common.WeaponsCapability.RageType;
import rafradek.TF2weapons.entity.EntityDummy;
import rafradek.TF2weapons.entity.EntityStatue;
import rafradek.TF2weapons.entity.IEntityTF2;
import rafradek.TF2weapons.entity.boss.EntityHHH;
import rafradek.TF2weapons.entity.boss.EntityMerasmus;
import rafradek.TF2weapons.entity.boss.EntityMonoculus;
import rafradek.TF2weapons.entity.boss.EntityTF2Boss;
import rafradek.TF2weapons.entity.building.EntityBuilding;
import rafradek.TF2weapons.entity.building.EntityDispenser;
import rafradek.TF2weapons.entity.building.EntitySentry;
import rafradek.TF2weapons.entity.building.EntityTeleporter;
import rafradek.TF2weapons.entity.mercenary.*;
import rafradek.TF2weapons.entity.projectile.EntityProjectileBase;
import rafradek.TF2weapons.inventory.ContainerMercenary;
import rafradek.TF2weapons.inventory.InventoryWearables;
import rafradek.TF2weapons.item.*;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.util.*;
import rafradek.TF2weapons.util.Contract.Objective;
import java.util.*;
import java.util.Map.Entry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.RegisterEvent;
import rafradek.TF2weapons.util.TF2Util;

public class TF2EventsCommon {
	public int tickleft;

	public static EntityBuilding teleporterBView;

	public static EntityBuilding teleporterAView;

	public static EntityBuilding dispenserView;

	public static EntityBuilding sentryView;

	// public ModelSkeleton skeletonModel=new ModelSkeleton();
	// private HashMap eligibleChunksForSpawning = new HashMap();
	public static final String[] STRANGE_TITLES = new String[] { "Strange", "Unremarkable", "Scarely lethal",
			"Mildly Menacing", "Somewhat threatening", "Uncharitable", "Notably dangerous", "Sufficiently lethal",
			"Truly feared", "Spectacularly lethal", "Gore-spatterer", "Wicked nasty", "Positively inhumane",
			"Totally ordinary", "Face-melting", "Rage-inducing", "Server-clearing", "Epic", "Legendary", "Australian",
			"Hale's own" };
	public static final int[] STRANGE_KILLS = new int[] { 0, 10, 25, 45, 70, 100, 135, 175, 225, 275, 350, 500, 750,
			999, 1000, 1500, 2500, 5000, 7500, 7616, 8500 };
	public static HashMap<LivingEntity, LivingEntity> fakeEntities = new HashMap<>();
	public static ArrayList<LivingEntity> pathsToDefine = new ArrayList<>();

	// public static final DataParameter<Boolean> ENTITY_UBER = new
	// DataParameter<Boolean>(169, DataSerializers.BOOLEAN);
	public static final EntityDataAccessor<Float> ENTITY_OVERHEAL = new EntityDataAccessor<>(170, EntityDataSerializers.FLOAT);

	public static final UUID REMOVE_ARMOR = UUID.fromString("5a0959c5-90e8-486b-ae51-26f69f19a248");

	// public static AttributeModifier NO_KNOCKBACK = new
	// AttributeModifier(UUID.fromString("c174c6c6-57cd-43b1-bada-43da0e3a88e1"),
	// "No knockba", tickleft, tickleft);
	public static long[] tickTimeLiving = new long[20];
	public static long[] tickTimeMercUpdate = new long[20];
	public static long[] tickTimeOther = new long[20];

	/*
	 * @SubscribeEvent public void spawn(WorldEvent.PotentialSpawns event){ int
	 * time=(int) (event.getWorld().getWorldInfo().getWorldTotalTime()/24000);
	 * if(MapList.scoutSpawn.containsKey(event.list)){
	 * MapList.scoutSpawn.get(event.list).itemWeight=time; } else{
	 * System.out.println("add"); SpawnListEntry entry=new
	 * SpawnListEntry(EntityScout.class, time, 1, 3); event.list.add(entry);
	 * MapList.scoutSpawn.put(event.list,entry); } }
	 */

	@SubscribeEvent
	public void untargetable(LivingChangeTargetEvent event) {
		boolean notarget = event.getNewTarget() == null;
		if (!(event.getEntity() instanceof Mob))
			return;
		if (notarget && event.getEntity().getAttribute(Attributes.FOLLOW_RANGE)
				.getModifier(TF2Util.FOLLOW_MODIFIER) != null) {
			event.getEntity().getAttribute(Attributes.FOLLOW_RANGE)
					.removeModifier(TF2Util.FOLLOW_MODIFIER);
			event.getEntity().getPersistentData().remove("TF2AM");
		}
		if (!notarget && ((event.getNewTarget().getCapability(TF2weapons.WEAPONS_CAP).isPresent()
				&& event.getNewTarget().getCapability(TF2weapons.WEAPONS_CAP).orElse(null).invisTicks >= 45))) {
			// event.getEntityLiving().setRevengeTarget(null);
			event.setNewTarget(null);
		}
		if (event.getNewTarget() instanceof EntityTF2Character && event.getEntity() instanceof AbstractGolem
				&& ((OwnableEntity) event.getNewTarget()).getOwner() != null) {
			// event.getEntityLiving().setRevengeTarget(null);
			event.setNewTarget(null);
		}
		if (!notarget && event.getEntity().canChangeDimensions()
				&& (event.getNewTarget().getCapability(TF2weapons.WEAPONS_CAP).isPresent()
						&& ItemDisguiseKit.isDisguised(event.getNewTarget(), event.getEntity())
						&& event.getEntity().getLastHurtByMob() != event.getNewTarget()))
			if (event.getEntity() instanceof Mob) {
				event.setNewTarget(null);
			}
	}

	@SubscribeEvent
	public void serverTickEnd(TickEvent.ServerTickEvent event) {

	}

	public static double avg(long[] values) {
		long totalticktime = 0L;
		for (long val : values) {
			totalticktime += val;
		}
		totalticktime /= values.length;
		return totalticktime * 1.0E-6D;
	}

	@SubscribeEvent
	public void worldTick(TickEvent.LevelTickEvent event) {
		if (event.phase == TickEvent.Phase.START && event.side == LogicalSide.SERVER) {
			/*
			 * long biggest = 0; int biggesttick = 0; if
			 * (event.world.getMinecraftServer().getTickCounter()%20 == 0) { for (int i = 0;
			 * i < 100; i++) { if (event.world.getMinecraftServer().tickTimeArray[i] >
			 * biggest) { biggest = event.world.getMinecraftServer().tickTimeArray[i]; } if
			 * (event.world.getMinecraftServer().tickTimeArray[i] > 50000000) biggesttick
			 * +=1; } System.out.println(biggest/1000000+ " "+biggesttick); }
			 */

			/*
			 * if(TF2weapons.server.getTickCounter()%20 == 0) {
			 * System.out.println("TickTimeLiving: "+avg(tickTimeLiving));
			 * System.out.println("TickTimeTF2Mob: "+avg(tickTimeMercUpdate));
			 * System.out.println("TickTimeOther: "+avg(tickTimeOther));
			 * System.out.println("TickTimeTotal: "+avg(TF2weapons.server.tickTimeArray)); }
			 */
			long worldTime = event.level.getDayTime();

			TF2WorldStorage events = event.level.getCapability(TF2weapons.WORLD_CAP).orElse(null);

			Iterator<Entry<Entity, InboundDamage>> it = events.damage.entrySet().iterator();
			while (it.hasNext()) {
				Entry<Entity, InboundDamage> entry = it.next();
				if (entry.getKey().isRemoved()) {
					it.remove();
					continue;
				}

				if (entry.getKey().invulnerableTime <= (entry.getKey() instanceof LivingEntity
						? ((LivingEntity) entry.getKey()).invulnerableDuration / 2f
						: 10f)) {
					TF2Util.dealDamageActual(entry.getKey(), event.level, entry.getValue().living,
							entry.getValue().stack, entry.getValue().critical, entry.getValue().damage,
							entry.getValue().source);
					it.remove();
				}
			}

			if (worldTime % 4 == 0) {
				for (int i = events.destroyProgress.size() - 1; i >= 0; i--) {
					DestroyBlockEntry entry = events.destroyProgress.get(i);
					int count = 0;
					if (entry != null && entry.level == event.level) {
						count++;
						entry.curDamage -= 0.05f;
						if (entry.curDamage <= 0 || entry.level.isEmptyBlock(entry.pos) || count >= 100) {
							events.destroyProgress.set(i, null);
							event.level.destroyBlockProgress(Math.min(Integer.MAX_VALUE, 0xFFFF + i), entry.pos, -1);
							continue;
						} else if (worldTime % 20 == 0) {
							int val = (int) ((entry.curDamage
									/ TF2Util.getHardness(entry.level.getBlockState(entry.pos), entry.level, entry.pos))
									* 10);
							event.level.destroyBlockProgress(Math.min(Integer.MAX_VALUE, 0xFFFF + i), entry.pos, val);
						}
					}
				}
			}
			Iterator<Entry<UUID, InvasionEvent>> ite = events.invasions.entrySet().iterator();
			while (ite.hasNext()) {
				Entry<UUID, InvasionEvent> entry = ite.next();
				entry.getValue().tick();
				if (entry.getValue().finished)
					ite.remove();
			}
			if (event.level.dimension() == Level.OVERWORLD) {
				int dayTime = (int) (worldTime % 24000);
				int dayNum = (int) (worldTime / 24000);
				if (dayTime == 1) {
					if (events != null
							&& new Random(event.level.getSeed() + worldTime * worldTime * 4987142 + worldTime * 5947611)
									.nextFloat() <= TF2ConfigVars.invasionChance) {
						for (Player player : event.level.players()) {
							if (player.getTeam() != null && player.getRandom().nextFloat() < (dayNum
									- TF2PlayerCapability.get(player).lastDayInvasion - TF2ConfigVars.invasionMinDay)
									* 0.055f)
								events.startInvasion(player, Math.min(TF2PlayerCapability.get(player).maxInvasionBeaten,
										Mth.clamp(Mth.ceil(worldTime / 960000f), 0, 2)), false);
						}
					}
				}
				if (!TF2ConfigVars.disableBossSpawn && dayTime >= 13000 && dayTime <= 20000 && dayTime % 1000 == 0
						&& event.level.getCurrentMoonPhaseFactor() == 1 && worldTime > 24000) {
					for (Player player : event.level.players())
						if (player.getCapability(TF2weapons.PLAYER_CAP).orElse(null).nextBossTicks <= worldTime
								&& event.level.getEntitiesOfClass(EntityTF2Boss.class,
										player.getBoundingBox().inflate(200, 200, 200)).isEmpty()) {
							player.getCapability(TF2weapons.PLAYER_CAP).orElse(null).nextBossTicks = (int) (worldTime + Math.min(40000, TF2ConfigVars.bossReappear)
											+ player.getRandom().nextInt(Math.max(1, TF2ConfigVars.bossReappear - 40000)));
							EntityTF2Boss boss;
							int bosslowest = 31;
							int bossid = 0;
							if (TF2PlayerCapability.get(player).highestBossLevel.get(EntityHHH.class) < bosslowest) {
								bosslowest = TF2PlayerCapability.get(player).highestBossLevel.get(EntityHHH.class);
								bossid = 0;
							}
							if (TF2PlayerCapability.get(player).highestBossLevel
									.get(EntityMonoculus.class) < bosslowest) {
								bosslowest = TF2PlayerCapability.get(player).highestBossLevel
										.get(EntityMonoculus.class);
								bossid = 1;
							}
							if (TF2PlayerCapability.get(player).highestBossLevel
									.get(EntityMerasmus.class) < bosslowest) {
								bosslowest = TF2PlayerCapability.get(player).highestBossLevel.get(EntityMerasmus.class);
								bossid = 2;
							}
							switch (bossid) {
							case 0:
								boss = new EntityHHH(event.level);
								break;
							case 1:
								boss = new EntityMonoculus(event.level);
								break;
							default:
								boss = new EntityMerasmus(event.level);
								break;
							}
							player.sendSystemMessage(Component.translatable("boss.message", boss.getName()));
							TF2PlayerCapability.get(player).bossSpawnTicks = worldTime + 1200;
							TF2PlayerCapability.get(player).bossToSpawn = boss;
						}
				}
			}

		}
	}

	public static boolean isSpawnEvent(Level world) {
		return world.getCapability(TF2weapons.WORLD_CAP).isPresent()
				&& world.getCapability(TF2weapons.WORLD_CAP).orElse(null).eventFlag == 1;
	}

	/*
	 * @SubscribeEvent public void spawnCharacters(TickEvent.WorldTickEvent event){
	 * if(!event.getWorld().isRemote && event.phase==TickEvent.Phase.END){
	 *
	 * //if(time!=0&&event.getWorld().rand.nextInt(2500/time)!=0) return;
	 * this.eligibleChunksForSpawning.clear(); int i; int k;
	 *
	 * for (i = 0; i < event.getWorld().playerEntities.size(); ++i) { EntityPlayer
	 * entityplayer = (EntityPlayer)event.getWorld().playerEntities.get(i); int j =
	 * MathHelper.floor(entityplayer.posX / 16.0D); k =
	 * MathHelper.floor(entityplayer.posZ / 16.0D); byte b0 = 8;
	 *
	 * for (int l = -b0; l <= b0; ++l) { for (int i1 = -b0; i1 <= b0; ++i1) {
	 * boolean flag3 = l == -b0 || l == b0 || i1 == -b0 || i1 == b0;
	 * ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(l + j, i1 + k);
	 *
	 * if (!flag3) { this.eligibleChunksForSpawning.put(chunkcoordintpair,
	 * Boolean.valueOf(false)); } else if
	 * (!this.eligibleChunksForSpawning.containsKey(chunkcoordintpair)) {
	 * this.eligibleChunksForSpawning.put(chunkcoordintpair, Boolean.valueOf(true));
	 * } } } }
	 *
	 * i = 0; ChunkCoordinates chunkcoordinates = event.getWorld().getSpawnPoint();
	 * Iterator iterator = this.eligibleChunksForSpawning.keySet().iterator();
	 * ArrayList<ChunkCoordIntPair> tmp = new
	 * ArrayList(eligibleChunksForSpawning.keySet()); Collections.shuffle(tmp);
	 * iterator = tmp.iterator(); label110:
	 *
	 * while (iterator.hasNext()) { ChunkCoordIntPair chunkcoordintpair1 =
	 * (ChunkCoordIntPair)iterator.next();
	 *
	 * if (!((Boolean)this.eligibleChunksForSpawning.get(chunkcoordintpair1)).
	 * booleanValue()) { Chunk chunk =
	 * event.getWorld().getChunkFromChunkCoords(chunkcoordintpair1.chunkXPos,
	 * chunkcoordintpair1.chunkZPos); int x = chunkcoordintpair1.chunkXPos * 16 +
	 * event.getWorld().rand.nextInt(16); int z = chunkcoordintpair1.chunkZPos * 16
	 * + event.getWorld().rand.nextInt(16); int y =
	 * event.getWorld().rand.nextInt(chunk == null ?
	 * event.getWorld().getActualHeight() : chunk.getTopFilledSegment() + 16 - 1);
	 * System.out.println("tick2"); if (!event.getWorld().getBlock(x, y,
	 * z).isNormalCube() && event.getWorld().getBlock(x, y, z).getMaterial() ==
	 * Material.air) { int i2 = 0; int j2 = 0; int
	 * team=event.getWorld().rand.nextInt(2); System.out.println("tick3"); while (j2
	 * < 1) { int k2 = x; int l2 = y; int i3 = z; byte b1 = 6; IEntityLivingData
	 * ientitylivingdata = null; int j3 = 0; System.out.println("tick4"); while
	 * (true) { System.out.println("tick5"); if (j3 < 4) { label103: { k2 +=
	 * event.getWorld().rand.nextInt(b1) - event.getWorld().rand.nextInt(b1); l2 +=
	 * event.getWorld().rand.nextInt(1) - event.getWorld().rand.nextInt(1); i3 +=
	 * event.getWorld().rand.nextInt(b1) - event.getWorld().rand.nextInt(b1);
	 *
	 * if (canCreatureTypeSpawnAtLocation(EnumCreatureType.monster,
	 * event.getWorld(), k2, l2, i3)) {System.out.println("tick6"); float f =
	 * (float)k2 + 0.5F; float f1 = (float)l2; float f2 = (float)i3 + 0.5F;
	 *
	 * if (event.getWorld().getClosestPlayer((double)f, (double)f1, (double)f2,
	 * 24.0D) == null) { System.out.println("tick7"); float f3 = f -
	 * (float)chunkcoordinates.posX; float f4 = f1 - (float)chunkcoordinates.posY;
	 * float f5 = f2 - (float)chunkcoordinates.posZ; float f6 = f3 * f3 + f4 * f4 +
	 * f5 * f5;
	 *
	 * if (f6 >= 576.0F) { EntityTF2Character entityliving=null;
	 *
	 * System.out.println("tick8"); try { switch (event.getWorld().rand.nextInt(2)){
	 * case 1:entityliving = new EntityScout(event.getWorld()); default:entityliving
	 * = new EntityHeavy(event.getWorld()); } entityliving.setEntTeam(team); } catch
	 * (Exception exception) { exception.printStackTrace(); }
	 *
	 * entityliving.setLocationAndAngles((double)f, (double)f1, (double)f2,
	 * event.getWorld().rand.nextFloat() * 360.0F, 0.0F);
	 *
	 * Result canSpawn = ForgeEventFactory.canEntitySpawn(entityliving,
	 * event.getWorld(), f, f1, f2); if (canSpawn == Result.ALLOW || (canSpawn ==
	 * Result.DEFAULT && entityliving.checkSpawnRules(entityliving.level, MobSpawnType.NATURAL))) {
	 * System.out.println("tick9"); ++i2;
	 * event.getWorld().spawnEntity(entityliving); if
	 * (!ForgeEventFactory.doSpecialSpawn(entityliving, event.getWorld(), f, f1,
	 * f2)) { ientitylivingdata = entityliving.onSpawnWithEgg(ientitylivingdata); }
	 *
	 * if (j2 >= ForgeEventFactory.getMaxSpawnPackSize(entityliving)) { continue
	 * label110; } }
	 *
	 * i += i2; } } }
	 *
	 * ++j3; continue; } } j2++; break; } } } } } } } public static boolean
	 * canCreatureTypeSpawnAtLocation(EnumCreatureType p_77190_0_, World p_77190_1_,
	 * int p_77190_2_, int p_77190_3_, int p_77190_4_) { if
	 * (!World.doesBlockHaveSolidTopSurface(p_77190_1_, p_77190_2_, p_77190_3_ - 1,
	 * p_77190_4_)) { return false; } else { Block block =
	 * p_77190_1_.getBlock(p_77190_2_, p_77190_3_ - 1, p_77190_4_); boolean
	 * spawnBlock = block.canCreatureSpawn(p_77190_0_, p_77190_1_, p_77190_2_,
	 * p_77190_3_ - 1, p_77190_4_); return spawnBlock && block != Blocks.bedrock &&
	 * !p_77190_1_.getBlock(p_77190_2_, p_77190_3_, p_77190_4_).isNormalCube() &&
	 * !p_77190_1_.getBlock(p_77190_2_, p_77190_3_,
	 * p_77190_4_).getMaterial().isLiquid() && !p_77190_1_.getBlock(p_77190_2_,
	 * p_77190_3_ + 1, p_77190_4_).isNormalCube(); } }
	 */
	@SubscribeEvent
	public void stopHurt(LivingAttackEvent event) {
		if (event.getSource().getEntity() != null && event.getSource().getEntity() instanceof LivingEntity
				&& (event.getSource().msgId.equals("mob") || event.getSource().msgId.equals("player"))) {
			LivingEntity damageSource = (LivingEntity) event.getSource().getEntity();
			if (!TF2Util.canInteract(damageSource)) {
				event.setCanceled(true);
			}
			if (damageSource.getCapability(TF2weapons.WEAPONS_CAP).isPresent()) {
				WeaponsCapability.get(damageSource).setDisguised(false);
			}
		}
		if (event.getSource().getEntity() instanceof LivingEntity
				&& event.getSource() instanceof TF2DamageSource
				&& ((TF2DamageSource) event.getSource()).hasAttackFlag(TF2DamageSource.BACKSTAB)) {
			ItemStack backpack = ItemBackpack.getBackpack(event.getEntity());
			if (!backpack.isEmpty() && backpack.getTag().getShort("Cooldown") <= 0
					&& TF2Attribute.getModifier("No Backstab", backpack, 0, event.getEntity()) != 0) {
				((LivingEntity) event.getSource().getEntity())
						.addEffect(new MobEffectInstance(TF2weapons.stun, 40));
				event.setCanceled(true);
				TF2Util.playSound(event.getEntity(), TF2Sounds.RAZORBACK_BREAK, 1f, 1f);
				backpack.getTag().putShort("Cooldown",
						(short) ((ItemBackpack) backpack.getItem()).getCooldown(backpack));
				return;
			}
		}
		if (!event.getSource().isBypassMagic() && !event.getSource().isBypassInvul()) {
			if ((event.getEntity().getEffect(TF2weapons.bonk) != null
					|| event.getEntity().getEffect(TF2weapons.uber) != null)
					&& !event.getSource().isBypassInvul()) {
				event.setCanceled(true);

				if (event.getEntity() instanceof Player
						&& event.getEntity().getEffect(TF2weapons.bonk) != null) {
					event.getEntity().getCapability(TF2weapons.PLAYER_CAP).orElse(null).dodgedDmg += event.getAmount();
					/*
					 * if(event.getEntityLiving().getCapability(TF2weapons.PLAYER_CAP,
					 * null).dodgedDmg>100){
					 * ((EntityPlayer)event.getEntityLiving()).awardStat(TF2Achievements.DODGE_DAMAGE)
					 * ; }
					 */
				}
				return;
			}
		}

		if (!event.isCanceled() && event.getAmount() > 0) {
			/*
			 * if(event.getEntity().getEntityData().getByte("IsCloaked")!=0){
			 * event.getEntity().getEntityData().setInteger("VisTicks",
			 * Math.min(10,event.getEntity().getEntityData().getInteger( "VisTicks")));
			 * event.getEntity().setInvisible(false); //System.out.println("notInvisible");
			 * }
			 */
			event.getEntity().getPersistentData().putInt("lasthit", event.getEntity().tickCount);
		}

	}

	@SubscribeEvent
	public void clonePlayer(final PlayerEvent.Clone event) {
		InventoryWearables oldInv = event.getOriginal().getCapability(TF2weapons.INVENTORY_CAP).orElse(null);
		InventoryWearables newInv = event.getEntity().getCapability(TF2weapons.INVENTORY_CAP).orElse(null);
		if (event.getEntity().level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
			for (int i = 0; i < oldInv.getContainerSize(); i++) {
				newInv.setInventorySlotContents(i, oldInv.getStackInSlot(i));
				if (!oldInv.getStackInSlot(i).isEmpty()) {
					Multimap<Attribute, AttributeModifier> modifiers = oldInv.getStackInSlot(i)
							.getAttributeModifiers(EquipmentSlot.CHEST);
					if (i == 2) {
						modifiers.removeAll(Attributes.ARMOR);
						modifiers.removeAll(Attributes.ARMOR_TOUGHNESS);
					}
					event.getEntity().getAttributes().addTransientAttributeModifiers(modifiers);
					if (i < TF2PlayerCapability.get(event.getEntity()).wearablesAttrib.length)
						TF2PlayerCapability.get(event.getEntity()).wearablesAttrib[i] = modifiers;
				}
			}
		} else {
			for (int i = 0; i < 2; i++) {
				newInv.setInventorySlotContents(i, oldInv.getStackInSlot(i));
			}
			newInv.setInventorySlotContents(4, oldInv.getStackInSlot(4));
			for (int i = 5; i < 8; i++)
				newInv.setInventorySlotContents(i, oldInv.getStackInSlot(i));
		}
		WeaponsCapability cap = WeaponsCapability.get(event.getEntity());
		cap.forcedClass = WeaponsCapability.get(event.getOriginal()).forcedClass;
		((ItemToken) TF2weapons.itemToken).updateAttributes(cap.forcedClass
				? TF2Util.metaStack(TF2weapons.itemToken, 1, WeaponsCapability.get(event.getOriginal()).getUsedToken())
				: newInv.getStackInSlot(4), event.getEntity());
		cap.ticksTotal = WeaponsCapability.get(event.getOriginal()).ticksTotal += 1000000L;

		TF2PlayerCapability.get(event.getEntity()).clone(TF2PlayerCapability.get(event.getOriginal()));

	}

	@SubscribeEvent
	public void uber(LivingHurtEvent event) {
		/*
		 * if(event.getEntity().getEntityData().getByte("IsCloaked")!=0){
		 * event.getEntity().getEntityData().setInteger("VisTicks",
		 * Math.min(10,event.getEntity().getEntityData().getInteger("VisTicks")) );
		 * event.getEntity().setInvisible(false); //System.out.println("notInvisible");
		 * }
		 */
		// System.out.println("damage "+event.getAmount());
		if (event.isCanceled() || event.getAmount() <= 0)
			return;
		LivingEntity attacker = null;
		LivingEntity target = event.getEntity();

		if (event.getSource().getEntity() != null
				&& event.getSource().getEntity() instanceof LivingEntity) {
			attacker = (LivingEntity) event.getSource().getEntity();
		}
		if (target.getEffect(TF2weapons.crit) != null) {
			event.setAmount(event.getAmount() * 1.1f);
		}

		if (!event.getSource().isBypassMagic() && !event.getSource().isBypassInvul()) {
			if (target.getCapability(TF2weapons.WEAPONS_CAP).isPresent()) {
				if (attacker != null)
					WeaponsCapability.get(target).lastAttacked = attacker;
				if (WeaponsCapability.get(target).getUsedToken() == 2 && event.getSource() == DamageSource.ON_FIRE) {
					event.setCanceled(true);
					target.clearFire();
				}
				if (WeaponsCapability.get(target).isInvisible()) {
					if (TF2Attribute.getModifier("Weapon Mode", ItemCloak.searchForWatches(target).getB(), 0,
							target) == 1)
						event.setAmount(event.getAmount() * 0.5f);
					else
						event.setAmount(event.getAmount() * 0.8f);
				}
				if (WeaponsCapability.get(target).isFeign()) {
					ItemStack watch = ItemCloak.getFeignDeathWatch(target);
					if (!watch.isEmpty()) {
						Inventory inv = null;
						if (target instanceof Player) {
							inv = new Inventory((Player) target);
							inv.replaceWith(((Player) target).getInventory());
						}
						event.setAmount(event.getAmount() * 0.15f);
						target.getCombatTracker().recordDamage(event.getSource(), target.getHealth(), event.getAmount());
						if (TF2ConfigVars.deadRingerTrigger)
							target.die(event.getSource());
						TF2Util.sendTracking(new TF2Message.ActionMessage(24, target), target);
						target.clearFire();
						for (Mob living : target.level.getEntitiesOfClass(Mob.class,
								target.getBoundingBox().inflate(80), living -> {
									return living.getTarget() == living;
								})) {
							living.setTarget(null);
						}
						/*
						 * EntityPlayerMP feign=FakePlayerFactory.get((WorldServer)
						 * target.getEntityWorld(), ((EntityPlayer)target).getGameProfile());
						 * feign.setPositionAndRotation(target.posX, target.posY, target.posZ,
						 * target.rotationYaw, target.rotationPitch); feign.setHealth(0f);
						 * target.getEntityWorld().spawnEntity(feign);
						 */
						if (target instanceof Player)
							((Player) target).getInventory().replaceWith(inv);
						((ItemCloak) watch.getItem()).setCloak(true, watch, target, target.getLevel());
						WeaponsCapability.get(target).invisTicks = 20;
						target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 1, false, false));
						WeaponsCapability.get(target).setFeign(false);
						target.setSilent(true);
					}
				}
				if (target.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).isExpJump()) {
					if (event.getAmount() > 1 && event.getSource() == DamageSource.FALL) {
						event.setAmount((float) Math.sqrt(event.getAmount()));
						WeaponsCapability.get(event.getEntity()).setExpJump(false);
					}
					target.hurtMarked = false;
					target.addEffect(new MobEffectInstance(TF2weapons.noKnockback, 1));
				}
			}

			ItemStack backpack = ItemBackpack.getBackpack(target);
			if (!backpack.isEmpty())
				event.setAmount(getDamageReductionFromItem(backpack, event.getSource(), target, event.getAmount()));

			for (ItemStack stack : target.getAllSlots())
				if (!stack.isEmpty() && !(stack.getItem() instanceof ItemBackpack)) {
					// System.out.println("Damaged");
					event.setAmount(getDamageReductionFromItem(stack, event.getSource(), target, event.getAmount()));
				}
			if (target.getEffect(TF2weapons.shieldBullet) != null && event.getSource().isProjectile()
					&& !event.getSource().isExplosion() && !event.getSource().isFire()
					&& !event.getSource().isMagic()) {
				if (target.getEffect(TF2weapons.shieldBullet).getAmplifier() == 1)
					event.setAmount(event.getAmount() * 0.25f);
				else
					event.setAmount(event.getAmount() * 0.9f);
			} else if (target.getEffect(TF2weapons.shieldExplosive) != null
					&& event.getSource().isExplosion()) {
				if (target.getEffect(TF2weapons.shieldExplosive).getAmplifier() == 1)
					event.setAmount(event.getAmount() * 0.25f);
				else
					event.setAmount(event.getAmount() * 0.9f);
			} else if (target.getEffect(TF2weapons.shieldFire) != null
					&& event.getSource().isFire()) {
				if (target.getEffect(TF2weapons.shieldFire).getAmplifier() == 1)
					event.setAmount(event.getAmount() * 0.25f);
				else
					event.setAmount(event.getAmount() * 0.9f);
			}
			if (target.getEffect(TF2weapons.backup) != null) {
				if (event.getSource().getDirectEntity() instanceof AbstractArrow) {
					event.setAmount(Math.min(event.getAmount(), 8f));
				}
				if ((event.getSource().msgId.equals("mob") || event.getSource().msgId.equals("player"))
						&& (attacker != null
								&& attacker.getAttributes()
										.getInstance(Attributes.ATTACK_DAMAGE) != null
								&& attacker.getAttributes()
										.getInstance(Attributes.ATTACK_DAMAGE).getModifier(
												UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF")) != null)) {
					event.setAmount((float) Math.min(event.getAmount(),
							1 + attacker.getAttributes().getInstance(Attributes.ATTACK_DAMAGE)
									.getModifier(UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF"))
									.getAmount()))/*
													 * ((EntityLivingBase)event. getSource().getEntity()).
													 * getHeldItemMainhand(). getAttributeModifiers(
													 * EntityEquipmentSlot.MAINHAND) .get(SharedMonsterAttributes.
													 * ATTACK_DAMAGE. getAttributeUnlocalizedName() ).toArray(new
													 * AttributeModifier[2])[0].))
													 */;
				}
				event.setAmount(event.getAmount() * 0.65f);
			}
			if (target instanceof EntityTF2Character && event.getSource().getEntity() != null
					&& event.getSource().getEntity() == event.getEntity()) {
				event.setAmount(event.getAmount() * 0.35f);
			}
		}

		int crit = 0;
		if (attacker != null && attacker.getCapability(TF2weapons.WEAPONS_CAP).isPresent()) {
			WeaponsCapability.get(attacker).lastAttacked = target;
			if (attacker.getEffect(TF2weapons.it) != null
					&& (event.getSource().msgId.equals("mob") || event.getSource().msgId.equals("player"))) {
				attacker.removeEffect(TF2weapons.it);
				attacker.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).itProtection = 15;
				target.addEffect(new MobEffectInstance(TF2weapons.it, 600));
			}
			if (attacker.getEffect(TF2weapons.bonk) != null)
				event.setCanceled(true);

			ItemStack stack = attacker == event.getSource().getDirectEntity() ? attacker.getMainHandItem()
					: ItemStack.EMPTY;
			crit = TF2Util.calculateCritPre(stack, attacker);

			ItemStack backpack = ItemBackpack.getBackpack(attacker);
			if (backpack.getItem() instanceof ItemSoldierBackpack
					&& !WeaponsCapability.get(attacker).isRageActive(RageType.BANNER)) {
				((ItemSoldierBackpack) backpack.getItem()).addRage(backpack, event.getAmount(), target, attacker);
			}
			if (attacker.getEffect(TF2weapons.conch) != null) {
				attacker.heal(0.35f * TF2Util.getDamageReduction(event.getSource(), target, event.getAmount()));
			}
			if (target.getEffect(TF2weapons.madmilk) != null) {
				attacker.heal(0.6f * TF2Util.getDamageReduction(event.getSource(), target, event.getAmount()));
			}
		}
		if (!(event.getSource() instanceof TF2DamageSource)) {
			if (event.getSource().getDirectEntity() != null
					&& event.getSource().getDirectEntity().getPersistentData().getBoolean("CritHeadshot"))
				crit = 2;
			crit = TF2Util.calculateCritPost(target, attacker, crit, ItemStack.EMPTY, event.getSource());
			if (crit == 1) {
				event.setAmount(event.getAmount() * 1.35f);
				TF2Util.playSound(target, TF2Sounds.MISC_MINI_CRIT, 1.5F,
						1.2F / (target.getRandom().nextFloat() * 0.2F + 0.9F));
			} else if (crit == 2) {
				event.setAmount(event.getAmount() * 2.2f);
				TF2Util.playSound(target, TF2Sounds.MISC_CRIT, 1.5F,
						1.2F / (target.getRandom().nextFloat() * 0.2F + 0.9F));
			}
		}
		if (attacker != null && target.getEffect(TF2weapons.gas) != null) {
			target.removeEffect(TF2weapons.gas);

			TF2Util.explosion(target.level, attacker, ItemFromData.getNewStack("explodeonignite"), attacker, target,
					target.getX(), target.getY(), target.getZ(), 2, 1f, 0, 0f);
			TF2Util.igniteAndAchievement(target, attacker, 8, 1f);
			// event.setAmount(event.getAmount() + 35f);
		}
		if (!target.level.isClientSide && target.getEntityData().get(ENTITY_OVERHEAL) > 0) {
			target.getEntityData().set(ENTITY_OVERHEAL, target.getAbsorptionAmount());
			if (target.getEntityData().get(ENTITY_OVERHEAL) <= 0) {
				target.getEntityData().set(ENTITY_OVERHEAL, -1f);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void reduceDamageArmor(LivingHurtEvent event) {
		if (event.getSource() instanceof TF2DamageSource && event.getAmount() > 0f) {
			LivingEntity living = event.getEntity();

			float orig = CombatRules.getDamageAfterAbsorb(event.getAmount(), living.getArmorValue(),
					(float) living.getAttribute(Attributes.ARMOR_TOUGHNESS).getValue())
					/ event.getAmount();
			float protect = orig;
			ItemStack weapon = ((TF2DamageSource) event.getSource()).getWeapon();
			if (!weapon.isEmpty() && weapon.getItem() instanceof ItemWeapon) {
				float damage = ((ItemWeapon) weapon.getItem()).getDamageForArmor(weapon,
						(LivingEntity) event.getSource().getEntity(), living);
				protect = CombatRules.getDamageAfterAbsorb(damage, living.getArmorValue(),
						(float) living.getAttribute(Attributes.ARMOR_TOUGHNESS).getValue())
						/ damage;
			}
			if (event.getSource().getEntity() != null
					&& event.getSource().getEntity().getCapability(TF2weapons.WEAPONS_CAP).isPresent()
					&& event.getSource().getEntity().getCapability(TF2weapons.WEAPONS_CAP).orElse(null).focusShotRemaining > 0) {

				float focus = TF2Attribute.getModifier("Focus", ((TF2DamageSource) event.getSource()).getWeapon(), 0,
						null);
				// float pierce = TF2Util.lerp(protect, 1f, 0.12f * focus);

				float hpDamage = event.getAmount() + Math.min(35 * focus, (event.getAmount()
						* (living.getMaxHealth() - living.getHealth()) / living.getMaxHealth() * focus * 0.6f));
				// if (hpDamage > event.getAmount() / pierce)
				event.setAmount(hpDamage);
				// else
				// protect = pierce;
			}

			TF2Util.addModifierSafe(living, Attributes.ARMOR,
					new AttributeModifier(REMOVE_ARMOR, "remove_arm", -event.getEntity().getArmorValue(), AttributeModifier.Operation.fromValue(0)),
					false);
			TF2Util.addModifierSafe(living, Attributes.ARMOR_TOUGHNESS,
					new AttributeModifier(
							REMOVE_ARMOR, "remove_armt", -event.getEntity()
									.getAttribute(Attributes.ARMOR_TOUGHNESS).getValue(), AttributeModifier.Operation.fromValue(0)),
					false);
			// vent.getSource().setDamageBypassesArmor();
			event.setAmount(event.getAmount() * (protect));
			if (WeaponsCapability.get(event.getEntity()) != null) {
				WeaponsCapability cap = WeaponsCapability.get(event.getEntity());
				float armorDamage = event.getAmount();
				armorDamage += cap.damageArmorMin;
				cap.damageArmorMin = armorDamage % 4f;
				armorDamage /= 4f;

				if (armorDamage >= 1f) {
					for (ItemStack armor : event.getEntity().getArmorSlots()) {
						if (armor.getItem() instanceof ArmorItem) {
							armor.hurtAndBreak((int) armorDamage, event.getEntity(), broken -> {});
						}
					}
				}
			}
			// System.out.println("Health2: "+event.getAmount() + " "+protect);

			// System.out.println("Health3: "+event.getAmount());
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void reduceDamageArmor(LivingDamageEvent event) {
		if (event.getSource() instanceof TF2DamageSource) {
			event.getEntity().getAttribute(Attributes.ARMOR).removeModifier(REMOVE_ARMOR);
			event.getEntity().getAttribute(Attributes.ARMOR_TOUGHNESS)
					.removeModifier(REMOVE_ARMOR);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void healFromAttack(LivingDamageEvent event) {
		LivingEntity attacker = null;
		if (event.getSource().getEntity() instanceof LivingEntity)
			attacker = (LivingEntity) event.getSource().getEntity();
		LivingEntity target = event.getEntity();
		if (attacker != null) {
			if (attacker.getEffect(TF2weapons.conch) != null) {
				attacker.heal(TF2Util.getReducedHealing(attacker, target, 0.35f * event.getAmount()));
			}
			if (target.getEffect(TF2weapons.madmilk) != null) {
				attacker.heal(TF2Util.getReducedHealing(attacker, target, 0.6f * event.getAmount()));
			}

		}
	}

	public static float getDamageReductionFromItem(ItemStack stack, DamageSource source, LivingEntity target,
			float damage) {
		float initialDamage = damage;
		if (TF2Attribute.getModifier("Breakable", stack, 0, target) != 0
				&& stack.getDamageValue() == stack.getMaxDamage())
			return damage;
		damage = TF2Attribute.getModifier("Damage Resist", stack, damage, target);
		if (source.isExplosion()) {
			damage = TF2Attribute.getModifier("Explosion Resist", stack, damage, target);
		}
		// System.out.println("Absorbed:
		// "+TF2Attribute.getModifier("Explosion Resist", stack, 1,
		// target));
		if (source.isFire()) {
			damage = TF2Attribute.getModifier("Fire Resist", stack, damage, target);
		}
		if (target.getMainHandItem() == stack) {
			if (source.isProjectile() || source.isExplosion() || source instanceof IndirectEntityDamageSource)
				damage = TF2Attribute.getModifier("Ranged Resist", stack, damage, target);
			else if (!source.isProjectile() && !source.isExplosion() && !source.isMagic()
					&& source instanceof EntityDamageSource)
				damage = TF2Attribute.getModifier("Melee Resist", stack, damage, target);
		}
		if (source == DamageSource.FALL && stack.getItem() instanceof ItemJetpack
				&& stack.getTag().getBoolean("Active")) {
			damage = damage * 0.4f;
		}
		if (initialDamage != damage) {
			float mult = TF2Attribute.getModifier("Breakable", stack, 0, target);
			if (mult != 0) {
				stack.hurtAndBreak(Mth.ceil((initialDamage - damage) * mult), target, broken -> {});
				if (stack.isEmpty()) {
					stack.setCount(1);
					stack.setDamageValue(stack.getMaxDamage());
				}
			}
		}
		return damage;
	}

	@SubscribeEvent
	public void stopBreak(BlockEvent.BreakEvent event) {

		if (event.getPlayer().getItemInHand(InteractionHand.MAIN_HAND) != null
				&& event.getPlayer().getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof ItemUsable
				&& !TF2PlayerCapability.get(event.getPlayer()).breakBlocks) {
			event.setCanceled(true);
		}
		if (event.getPlayer().getEffect(TF2weapons.bonk) != null) {
			event.setCanceled(true);
		}
		if (event.getPlayer().getCapability(TF2weapons.WEAPONS_CAP).orElse(null).invisTicks > 0) {
			event.setCanceled(true);
		}
		if (event.getPlayer().getEffect(TF2weapons.stun) != null) {
			event.setCanceled(true);
		}
		WeaponsCapability.get(event.getPlayer()).setDisguised(false);
	}

	/*
	 * @SubscribeEvent public void stopInteract(playerteractEvent event){
	 * if(!((event.==PlayerInteractEvent.Action.RIGHT_CLICK_AIR||event.action==
	 * PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)&&event.getEntity()Player.
	 * getHeldItem(EnumHand.MAIN_HAND)!=null&&(event.getEntity()Player.
	 * getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemCloak ||
	 * event.getEntity()Player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof
	 * ItemDisguiseKit))){
	 * if(event.getEntity()Player.getEntityData().getByte("Disguised")!=0){
	 * disguise(event.getEntity()Player,false); }
	 * if((event.getEntity()Player.getHeldItem(EnumHand.MAIN_HAND) !=
	 * null&&event.getEntity()Player.getHeldItem(EnumHand.MAIN_HAND).getItem()
	 * instanceof ItemUsable &&
	 * event.action==PlayerInteractEvent.Action.LEFT_CLICK_BLOCK)||event.
	 * getEntity()Player.getEntityData().getInteger("VisTicks")!=0){
	 * event.setCanceled(true); } } }
	 */

	@SubscribeEvent
	public void onSpawn(EntityJoinLevelEvent event) {
		if (event.getEntity() instanceof ItemEntity
				&& ((ItemEntity) event.getEntity()).getItem().getItem() instanceof IBackpackItem) {
			event.getEntity().discard();
			event.setCanceled(true);
		}
		if (event.getEntity() instanceof PathfinderMob && TF2Util.isHostile((LivingEntity) event.getEntity())
				&& !(event.getEntity() instanceof IEntityTF2) && event.getEntity().canChangeDimensions()) {
			if (TF2ConfigVars.targetSentries) {
				((PathfinderMob) event.getEntity()).targetSelector.addGoal(535,
						new NearestAttackableTargetGoal<EntitySentry>((PathfinderMob) event.getEntity(),
								EntitySentry.class, 10, true, false, sentry -> {
									return ((EntitySentry) sentry).getOwnerUUID() != null;
								}) {
							@Override
							protected double getFollowDistance() {
								return super.getFollowDistance() * 0.45f;
							}
						});
			}
			if (TF2ConfigVars.attackMobs) {
				((PathfinderMob) event.getEntity()).targetSelector.addGoal(536, new NearestAttackableTargetGoal<>(
						(PathfinderMob) event.getEntity(), EntityTF2Character.class, 10, true, false, (target) -> {
							return !((EntityTF2Character) target).isRobot();
						}));
			}
		}
		if (event.getEntity() instanceof Player) {
			if (event.getEntity().level.isClientSide) {
				if (event.getEntity() == ClientProxy.getLocalPlayer())
					TF2weapons.network.sendToServer(TF2Message.InitClientMessage.fromConfig());
				TF2weapons.network.sendToServer(new TF2Message.ActionMessage(99, (LivingEntity) event.getEntity()));
			}
			if (event.getEntity().level != null && !event.getEntity().level.isClientSide
					&& event.getEntity() instanceof ServerPlayer) {
				ServerPlayer player = ((ServerPlayer) event.getEntity());
				player.inventoryMenu.addSlotListener(new TF2ContainerListener(player));
			}
		}
	}

	@SubscribeEvent
	public void entityConstructing(final EntityEvent.EntityConstructing event) {

		if (event.getEntity() instanceof LivingEntity) {
			// event.getEntity().getDataManager().register(ENTITY_UBER, false);
			event.getEntity().getEntityData().define(ENTITY_OVERHEAL, 0f);
		}

	}

	/*
	 * @SubscribeEvent public void ChunkLoad(ChunkEvent.Load event){
	 * if(!event.getWorld().isRemote){ List<EntityTeleporter>
	 * teleporter=event.getWorld().getEntitiesWithinAABB(EntityTeleporter.class, new
	 * AxisAligned())); } }
	 */
	@SubscribeEvent
	public void loadPlayer(PlayerEvent.PlayerLoggedInEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer))
			return;
		ServerPlayer player = (ServerPlayer) event.getEntity();
		if (!player.server.isSingleplayerOwner(player.getGameProfile())) {
			TF2weapons.network.sendTo(new TF2Message.WeaponDataMessage(TF2weapons.itemDataCompressed), player);
		}
		int i = 0;
		for (Contract contract : player.getCapability(TF2weapons.PLAYER_CAP).orElse(null).contracts) {
			TF2weapons.network.sendTo(new TF2Message.ContractMessage(i, contract), player);
			i++;
		}
		TF2weapons.network.sendTo(new TF2Message.InitMessage(-1, 0, TF2ConfigVars.buildingsUseEnergy), player);
	}

	/*
	 * @SubscribeEvent public void onConnect(ServerConnectionFromClientEvent event){
	 * new NetHandlerPlayServer(MinecraftServer.getServer(), event.manager,
	 * ((NetHandlerPlayServer)event.handler).playerEntity); }
	 */
	/*
	 * public static class PacketReceiveHack extends
	 * SimpleChannelInboundHandler<Packet>{
	 *
	 * @Override protected void channelRead0(ChannelHandlerContext ctx, Packet msg)
	 * throws Exception { System.out.println(msg); }
	 *
	 * }
	 */
	/*
	 * public static class MovePacketHack extends NetHandlerPlayServer{
	 *
	 * public MovePacketHack(MinecraftServer server, NetworkManager
	 * networkManagerIn, EntityPlayerMP player) { super(server, networkManagerIn,
	 * player); } public void processPlayer(C03PacketPlayer packetIn) {
	 * System.out.println("send"); super.processPlayer(packetIn); } }
	 */
	@SubscribeEvent
	public void stopUsing(PlayerInteractEvent.RightClickBlock event) {
		if (!TF2Util.canInteract(event.getEntity())) {
			event.setCanceled(true);
		}
		event.getEntity().removeEffect(TF2weapons.charging);
	}

	@SubscribeEvent
	public void stopUsing(PlayerInteractEvent.RightClickItem event) {
		ItemStack item = event.getEntity().getItemInHand(event.getHand());
		if (!TF2Util.canInteract(event.getEntity()) && !(item.getItem() instanceof ItemDisguiseKit)
				&& !(item.getItem() instanceof ItemCloak && (item.getTag().getBoolean("Active")
						|| (WeaponsCapability.get(event.getEntity()).isFeign()
								&& TF2Attribute.getModifier("Weapon Mode", item, 0, event.getEntity()) == 1)))) {
			event.setCanceled(true);
			return;
		}
		if (!(item.isEdible() || item.getItem() instanceof ItemCloak
				|| item.getItem() instanceof ItemDisguiseKit)) {
			WeaponsCapability.get(event.getEntity()).setDisguised(false);
		}
		event.getEntity().removeEffect(TF2weapons.charging);
	}

	@SubscribeEvent
	public void stopUsing(PlayerInteractEvent.EntityInteract event) {

		if (!TF2Util.canInteract(event.getEntity())) {
			event.setCanceled(true);
			return;
		}
		if (!(event.getEntity().getItemInHand(event.getHand()).isEdible())) {
			WeaponsCapability.get(event.getEntity()).setDisguised(false);
		}
		event.getEntity().removeEffect(TF2weapons.charging);
	}

	@SubscribeEvent
	public void stopUsing(PlayerInteractEvent.LeftClickBlock event) {
		if (!TF2Util.canInteract(event.getEntity())) {
			event.setCanceled(true);
		}
		event.getEntity().removeEffect(TF2weapons.charging);
	}

	@SubscribeEvent
	public void stopJump(LivingEvent.LivingJumpEvent event) {

		LivingEntity living = event.getEntity();

		if (WeaponsCapability.get(living) != null && WeaponsCapability.get(living).isGrappled()) {
			WeaponsCapability.get(living).setGrappled(false);
		}

		if ((living.getEffect(TF2weapons.stun) != null
				&& living.getEffect(TF2weapons.bombmrs) == null)
				|| living.getEffect(TF2weapons.charging) != null
				|| (living.getMainHandItem() != null
						&& (living.getMainHandItem().getItem() instanceof ItemMinigun
								|| living.getMainHandItem().getItem() instanceof ItemHuntsman)
						&& living.getCapability(TF2weapons.WEAPONS_CAP).isPresent()
						&& living.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).chargeTicks > 0)) {
			living.hasImpulse = false;
			living.setDeltaMovement(living.getDeltaMovement().x, living.getDeltaMovement().y - (0.5f), living.getDeltaMovement().z);
			if (living.isSprinting()) {
				float f = living.getYRot() * 0.017453292F;
				living.setDeltaMovement(living.getDeltaMovement().x + (Mth.sin(f) * 0.2F), living.getDeltaMovement().y, living.getDeltaMovement().z);
				living.setDeltaMovement(living.getDeltaMovement().x, living.getDeltaMovement().y, living.getDeltaMovement().z - (Mth.cos(f) * 0.2F));
			}

		}
		if (living.getMainHandItem().getItem() instanceof ItemSniperRifle
				&& living.getCapability(TF2weapons.WEAPONS_CAP).isPresent())
			((ItemSniperRifle) living.getMainHandItem().getItem()).disableZoom(living.getMainHandItem(),
					living);

	}

	@SubscribeEvent
	public void livingUpdate(final LivingEvent.LivingTickEvent event) {
		event.getEntity().getLevel().getProfiler().push("TF2TickEvent");
		final LivingEntity living = event.getEntity();
		if (living.getCapability(TF2weapons.INVENTORY_CAP).isPresent()) {
			for (int i = 0; i < 3; i++) {
				ItemStack stack = living.getCapability(TF2weapons.INVENTORY_CAP).orElse(null).getStackInSlot(i);
				if (!stack.isEmpty() && stack.getItem() instanceof ItemWearable)
					((ItemWearable) stack.getItem()).onUpdateWearing(stack, living.level, living);
				else if (!stack.isEmpty() && stack.getItem() instanceof ItemBackpack)
					((ItemBackpack) stack.getItem()).onArmorTickAny(living.level, living, stack);
			}
			if (!living.level.isClientSide && living.tickCount % 2 == 0)
				living.getCapability(TF2weapons.INVENTORY_CAP).orElse(null).updateSlots();
		}
		if (living.getCapability(TF2weapons.PLAYER_CAP).isPresent())
			living.getCapability(TF2weapons.PLAYER_CAP).orElse(null).tick();
		if (living.isAlive() && (living.getCapability(TF2weapons.WEAPONS_CAP).isPresent())) {

			// long nanoTickStart=System.nanoTime();
			final WeaponsCapability cap = living.getCapability(TF2weapons.WEAPONS_CAP).orElse(null);
			cap.tick();

			// if(!living.world.isRemote)
			// tickTimeLiving[TF2weapons.server.getTickCounter()%20]+=System.nanoTime()-nanoTickStart;
		}

		if (living.getLastHurtByMob() != null && living.getLastHurtByMob().getCapability(TF2weapons.WEAPONS_CAP).isPresent()
				&& living.getLastHurtByMob().getCapability(TF2weapons.WEAPONS_CAP).orElse(null).invisTicks >= 45) {
			living.setLastHurtByMob(null);
		}

		if (living instanceof Mob && ((Mob) living).getTarget() != null) {
			if (living.getPersistentData().contains("TF2AM")
					&& living.getLevel().getGameTime() - living.getPersistentData().getInt("TF2AM") > 360) {
				living.getPersistentData().remove("TF2AM");
				living.getAttribute(Attributes.FOLLOW_RANGE).removeModifier(TF2Util.FOLLOW_MODIFIER);
			} else if (((Mob) living).getTarget().getCapability(TF2weapons.WEAPONS_CAP).isPresent()
					&& ((Mob) living).getTarget().getCapability(TF2weapons.WEAPONS_CAP).orElse(null).invisTicks >= 45) {
				((Mob) living).setTarget(null);
			}
		}

		if (living.getEntityData().get(ENTITY_OVERHEAL) == -1) {
			living.getEntityData().set(ENTITY_OVERHEAL, 0F);
			living.setAbsorptionAmount(0);
		}
		if (living.getEntityData().get(ENTITY_OVERHEAL) > 0) {
			if (living.level.isClientSide) {
				living.setAbsorptionAmount(living.getEntityData().get(ENTITY_OVERHEAL));
			}
			living.setAbsorptionAmount(living.getAbsorptionAmount() - living.getMaxHealth() * 0.001666f);
			if (!living.level.isClientSide && living.tickCount % 4 == 0)
				if (living.getAbsorptionAmount() <= 0) {
					living.getEntityData().set(ENTITY_OVERHEAL, -1f);
					// living.getEntityData().setFloat("Overheal", -1f);
				} else {
					living.getEntityData().set(ENTITY_OVERHEAL, living.getAbsorptionAmount());
				}
		}
		living.level.getProfiler().pop();
		/*
		 * if (living.getActivePotionEffect(TF2weapons.uber)!=null &&
		 * !(living.getHeldItem(EnumHand.MAIN_HAND) != null &&
		 * living.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemMedigun &&
		 * living.getHeldItem(EnumHand.MAIN_HAND).getTagCompound().getBoolean(
		 * "Activated"))) { List<EntityLivingBase> list =
		 * living.world.getEntitiesWithinAABB(EntityLivingBase.class, new
		 * AxisAlignedBB(living.posX - 8, living.posY - 8, living.posZ - 8, living.posX
		 * + 8, living.posY + 8, living.posZ + 8), new Predicate<EntityLivingBase>() {
		 *
		 * @Override public boolean apply(EntityLivingBase input) { return
		 * input.world.getEntityByID( input.getCapability(TF2weapons.WEAPONS_CAP).orElse(null)
		 * != null ? input.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).healTarget : -1)
		 * == living &&
		 * input.getHeldItem(EnumHand.MAIN_HAND).getTagCompound().getBoolean("Activated"
		 * ); }
		 *
		 * }); boolean isOK = !list.isEmpty(); if (!isOK) {
		 * living.getEntityData().setBoolean("Ubercharge", false); } }
		 */
	}

	@SubscribeEvent
	public void loadWorld(LevelEvent.Load event) {
		if (!(event.getLevel() instanceof Level))
			return;
		Level world = (Level) event.getLevel();
		/*
		 * if (!event.getWorld().isRemote &&
		 * event.getWorld().getPerWorldStorage().getOrLoadData(TF2WorldStorage.class,
		 * TF2weapons.MOD_ID)==null){
		 * event.getWorld().getPerWorldStorage().setData(TF2weapons.MOD_ID, new
		 * TF2WorldStorage()); dummyEnt = new EntityCreeper(null); }
		 */
		if (world.isClientSide) {
			TF2EventsCommon.sentryView = new EntitySentry(world);
			TF2EventsCommon.sentryView.die(DamageSource.GENERIC);
			TF2EventsCommon.dispenserView = new EntityDispenser(world);
			TF2EventsCommon.dispenserView.die(DamageSource.GENERIC);
			TF2EventsCommon.teleporterAView = new EntityTeleporter(world);
			TF2EventsCommon.teleporterAView.die(DamageSource.GENERIC);
			TF2EventsCommon.teleporterBView = new EntityTeleporter(world);
			TF2EventsCommon.teleporterBView.die(DamageSource.GENERIC);
		}
		if (TF2weapons.dummyEnt == null)
			TF2weapons.dummyEnt = new EntityDummy(world);
		if (!world.isClientSide && world.getScoreboard().getPlayerTeam("RED") == null) {
			PlayerTeam teamRed = world.getScoreboard().createTeam("RED");
			PlayerTeam teamBlu = world.getScoreboard().createTeam("BLU");

			teamRed.setSeeFriendlyInvisibles(true);
			teamRed.setAllowFriendlyFire(false);
			teamBlu.setSeeFriendlyInvisibles(true);
			teamBlu.setAllowFriendlyFire(false);
			teamRed.setColor(ChatFormatting.RED);
			teamBlu.setColor(ChatFormatting.BLUE);

		}
		if (!world.isClientSide && world.getScoreboard().getPlayerTeam("TF2Bosses") == null) {
			PlayerTeam teamBosses = world.getScoreboard().createTeam("TF2Bosses");
			teamBosses.setSeeFriendlyInvisibles(true);
			teamBosses.setAllowFriendlyFire(false);
			teamBosses.setColor(ChatFormatting.DARK_PURPLE);
		}
		if (!world.isClientSide && world.getScoreboard().getPlayerTeam("Robots") == null) {
			PlayerTeam teamRobots = world.getScoreboard().createTeam("Robots");
			teamRobots.setSeeFriendlyInvisibles(true);
			teamRobots.setAllowFriendlyFire(false);
			teamRobots.setColor(ChatFormatting.AQUA);
		}
		if (!world.isClientSide
				&& world.getScoreboard().getPlayerTeam("RED").getColor() == ChatFormatting.RESET) {
			world.getScoreboard().getPlayerTeam("RED").setColor(ChatFormatting.RED);
			world.getScoreboard().getPlayerTeam("BLU").setColor(ChatFormatting.BLUE);
		}
	}

	@SubscribeEvent
	public void unloadWorld(LevelEvent.Unload event) {
		if (!(event.getLevel() instanceof Level))
			return;
		Level world = (Level) event.getLevel();
		if (TF2weapons.dummyEnt != null && TF2weapons.dummyEnt.level == world)
			TF2weapons.dummyEnt = null;
		if (world.isClientSide && sentryView != null && sentryView.level == world) {
			TF2EventsCommon.sentryView = null;
			TF2EventsCommon.dispenserView = null;
			TF2EventsCommon.teleporterAView = null;
			TF2EventsCommon.teleporterBView = null;
		}
	}

	@SubscribeEvent
	public void medicSpawn(LivingSpawnEvent.SpecialSpawn event) {
		float chance = 0f;

		if (event.getEntity() instanceof EntityHeavy) {
			chance = 0.16f;
		} else if (event.getEntity() instanceof EntitySoldier) {
			chance = 0.08f;
		} else if (event.getEntity() instanceof EntityDemoman) {
			chance = 0.07f;
		} else if (event.getEntity() instanceof EntityPyro) {
			chance = 0.06f;
		} else if (event.getEntity() instanceof EntityScout) {
			chance = 0.03f;
		} else
			return;
		chance *= TF2ConfigVars.medicChance;
		if (event.getLevel().getRandom().nextFloat() < event.getLevel().getDifficulty().getId() * chance) {
			((EntityTF2Character) event.getEntity()).spawnMedic = true;
			/*
			 * EntityMedic medic = new EntityMedic(event.getWorld());
			 * medic.setLocationAndAngles(event.getEntity().posX +
			 * event.getWorld().rand.nextDouble() * 0.5 - 0.25, event.getEntity().posY,
			 * event.getEntity().posZ + event.getWorld().rand.nextDouble() * 0.5 - 0.25,
			 * event.getWorld().rand.nextFloat() * 360.0F, 0.0F); medic.natural = true; //
			 * medic.setEntTeam(event.getWorld().rand.nextInt(2));
			 * TF2Util.initialSpawn(medic, event.getWorld().getDifficultyForLocation(new
			 * BlockPos(event.getX(), event.getY(), event.getZ())), null);
			 * EntityTF2Character.nextEntTeam = medic.getEntTeam();
			 *
			 * event.getWorld().spawnEntity(medic);
			 */
		}
	}

	@SubscribeEvent
	public void attachCapabilityEnt(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof Player) {
			event.addCapability(new ResourceLocation(TF2weapons.MOD_ID, "weaponcapability"),
					new WeaponsCapability((LivingEntity) event.getObject()));
			if (!event.getObject().getCapability(TF2weapons.INVENTORY_CAP).isPresent()) {
				final InventoryWearables inv = new InventoryWearables((Player) event.getObject());
				event.addCapability(new ResourceLocation(TF2weapons.MOD_ID, "wearablescapability"), inv);
			}
			event.addCapability(new ResourceLocation(TF2weapons.MOD_ID, "playercapability"),
					new TF2PlayerCapability((Player) event.getObject()));
		}
	}

	@SubscribeEvent
	public void attachCapabilityWorld(AttachCapabilitiesEvent<Level> event) {
		event.addCapability(new ResourceLocation(TF2weapons.MOD_ID, "tf2worldcapability"),
				new TF2WorldStorage(event.getObject()));
	}

	@SubscribeEvent
	@SuppressWarnings("deprecation")
	public void placeBanner(BlockEvent.EntityPlaceEvent event) {
		if (!(event.getLevel() instanceof Level))
			return;
		BlockEntity banner = event.getLevel().getBlockEntity(event.getPos());
		if (banner != null && banner instanceof BannerBlockEntity) {
			List<BannerPattern> patterns = getPatterns((BannerBlockEntity) banner);
			if (patterns.contains(TF2weapons.redPattern) || patterns.contains(TF2weapons.bluPattern)
					|| patterns.contains(TF2weapons.neutralPattern)) {
				// System.out.println("Banner is");
				((Level) event.getLevel()).getCapability(TF2weapons.WORLD_CAP).orElse(null).banners.add(event.getPos());
			}
		}

	}

	@SubscribeEvent
	public void removeBanner(BlockEvent.BreakEvent event) {
		if (!(event.getLevel() instanceof Level))
			return;
		BlockEntity banner = event.getLevel().getBlockEntity(event.getPos());
		if (banner != null && banner instanceof BannerBlockEntity) {
			if (getPatterns((BannerBlockEntity) banner).contains(TF2weapons.redPattern)) {
				((Level) event.getLevel()).getCapability(TF2weapons.WORLD_CAP).orElse(null).banners.remove(event.getPos());
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void craftItem(PlayerEvent.ItemCraftedEvent event) {
		ItemStack withPattern = ItemStack.EMPTY;
		ItemStack noPattern = ItemStack.EMPTY;
		for (int i = 0; i < event.getInventory().getContainerSize(); i++) {
			ItemStack stack = event.getInventory().getItem(i);
			if (!stack.isEmpty() && stack.getItem() instanceof BannerItem) {
				if (BannerBlockEntity.getPatternCount(stack) > 0) {
					withPattern = stack;
				} else {
					noPattern = stack;
				}
				if (!withPattern.isEmpty() && !noPattern.isEmpty()) {
					withPattern.shrink(1);
				}
			}
		}
	}

	/*
	 * @SubscribeEvent public void attachCapabilityItem(AttachCapabilitiesEvent.Item
	 * event) { if (event.getItem() instanceof ItemFromData) {
	 * System.out.println("Adding cap"); event.addCapability(new
	 * ResourceLocation(TF2weapons.MOD_ID, "weapondatacapability"), new
	 * WeaponData.WeaponDataCapability()); } }
	 */
	@SubscribeEvent
	public void livingDeath(LivingDeathEvent event) {
		if (event.isCanceled())
			return;
		if (!event.getEntity().level.isClientSide && event.getSource() != null && event.getSource().getEntity() != null
				&& event.getSource().getEntity() instanceof LivingEntity) {
			ItemStack stack = ItemStack.EMPTY;
			final LivingEntity living = (LivingEntity) event.getSource().getEntity();
			boolean isEnemy = TF2Util.isEnemy(living, event.getEntity());
			if (event.getSource() instanceof TF2DamageSource) {

				stack = ((TF2DamageSource) event.getSource()).getWeaponOrig();

			} else {
				stack = living.getMainHandItem();
			}
			if (living instanceof ServerPlayer && isEnemy) {
				ServerPlayer player = (ServerPlayer) living;
				TF2PlayerCapability plcap = player.getCapability(TF2weapons.PLAYER_CAP).orElse(null);
				if (event.getEntity() instanceof EntityTF2Character
						&& !((EntityTF2Character) event.getEntity()).isRobot()) {
					if ((plcap.fastKillTimer += 120) > 360) {
						plcap.fastKillTimer = 0;
						plcap.completeObjective(Objective.KILLS, stack);
					}
					plcap.completeObjective(Objective.KILL, stack);
					if (event.getSource() instanceof TF2DamageSource
							&& ((TF2DamageSource) event.getSource()).hasAttackFlag(TF2DamageSource.HEADSHOT)) {
						plcap.completeObjective(Objective.HEADSHOT, stack);
					}
					if (event.getSource() instanceof TF2DamageSource
							&& ((TF2DamageSource) event.getSource()).hasAttackFlag(TF2DamageSource.BACKSTAB)) {
						plcap.completeObjective(Objective.BACKSTAB, stack);
					}
					if (event.getSource().getDirectEntity() instanceof EntityProjectileBase
							&& ((EntityProjectileBase) event.getSource().getDirectEntity()).reflected) {
						plcap.completeObjective(Objective.KILL_REFLECTED, stack);
					}
					if (stack.getItem() instanceof ItemStickyLauncher) {
						plcap.completeObjective(Objective.STICKY_KILL, stack);
					}
					if (stack.getItem() instanceof ItemMeleeWeapon) {
						plcap.completeObjective(Objective.KILL_MELEE, stack);
					}
					if (player.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).isExpJump()) {
						plcap.completeObjective(Objective.KILL_BLAST, stack);
					}
					if (player.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).airJumps > 0) {
						plcap.completeObjective(Objective.KILL_DOUBLE, stack);
					}
					if (event.getEntity() instanceof EntityScout)
						plcap.completeObjective(Objective.KILL_SCOUT, stack);
					else if (event.getEntity() instanceof EntityPyro)
						plcap.completeObjective(Objective.KILL_PYRO, stack);
					else if (event.getEntity() instanceof EntitySoldier)
						plcap.completeObjective(Objective.KILL_SOLDIER, stack);
					else if (event.getEntity() instanceof EntityHeavy)
						plcap.completeObjective(Objective.KILL_HEAVY, stack);
					else if (event.getEntity() instanceof EntityDemoman)
						plcap.completeObjective(Objective.KILL_DEMOMAN, stack);
					else if (event.getEntity() instanceof EntityEngineer)
						plcap.completeObjective(Objective.KILL_ENGINEER, stack);
					else if (event.getEntity() instanceof EntityMedic)
						plcap.completeObjective(Objective.KILL_MEDIC, stack);
					else if (event.getEntity() instanceof EntitySpy)
						plcap.completeObjective(Objective.KILL_SPY, stack);
					else if (event.getEntity() instanceof EntitySniper)
						plcap.completeObjective(Objective.KILL_SNIPER, stack);
				}

			} else if (living instanceof ServerPlayer && event.getEntity() instanceof EntityBuilding
					&& !TF2Util.isAlliedTo(living, event.getEntity())) {
				living.getCapability(TF2weapons.PLAYER_CAP).orElse(null).completeObjective(Objective.DESTROY_BUILDING, stack);
			}
			if (event.getEntity() instanceof EntitySentry) {
				for (ServerPlayer player : living.level.getEntitiesOfClass(ServerPlayer.class,
						living.getBoundingBox().inflate(8, 8, 8), new Predicate<ServerPlayer>() {

							@Override
							public boolean test(ServerPlayer input) {
								return input.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).getHealTarget() == living
										.getId()
										&& input.getMainHandItem().getTag().getBoolean("Activated");
							}

						})) {
					player.getCapability(TF2weapons.PLAYER_CAP).orElse(null).completeObjective(Objective.DESTROY_SENTRY_UBER,
							stack);
				}

			}
			if (!stack.isEmpty() && stack.getItem() instanceof ItemWeapon) {

				if (!(event.getEntity() instanceof Player)
						&& TF2Attribute.getModifier("Silent Kill", stack, 0, null) != 0) {
					event.getEntity().setSilent(true);
				}

				if (stack.hasTag() && stack.getTag().getBoolean("Strange")) {

					if (!(event.getEntity() instanceof Player)) {
						stack.getTag().putInt("Kills", stack.getTag().getInt("Kills") + 1);
					} else {
						stack.getTag().putInt("PlayerKills",
								stack.getTag().getInt("PlayerKills") + 1);
					}
					onStrangeUpdate(stack, living);

				}
				if (stack.hasTag() && stack.getTag().contains(NBTLiterals.STREAK_ATTRIB) && isEnemy) {
					stack.getTag().putInt(NBTLiterals.STREAK_KILLS,
							stack.getTag().getInt(NBTLiterals.STREAK_KILLS) + 1);
					stack.getTag().putLong(NBTLiterals.STREAK_COOL,
							WeaponsCapability.get(living).ticksTotal + ItemKillstreakKit.getCooldown(
									stack.getTag().getByte(NBTLiterals.STREAK_LEVEL),
									stack.getTag().getInt(NBTLiterals.STREAK_KILLS)));
					/*
					 * stack.getTagCompound().setLong(NBTLiterals.STREAK_COOL,
					 * WeaponsCapability.get(living).ticksTotal +
					 * Math.max(100,ItemKillstreakKit.getCooldown(stack.getTagCompound().getByte(
					 * NBTLiterals.STREAK_LEVEL)) -
					 * MathHelper.log2(stack.getTagCompound().getInteger(NBTLiterals.STREAK_KILLS))*
					 * 250));
					 */
					stack.getTag().putLong(NBTLiterals.STREAK_LAST, WeaponsCapability.get(living).ticksTotal);
					stack.getTag().putShort(NBTLiterals.STREAK_REDUCTION, (short) 1);

					stack.getCapability(TF2weapons.WEAPONS_DATA_CAP).orElse(null).cached = false;
				}
				if (stack.hasTag() && stack.getTag().getBoolean("Australium")
						&& TF2ConfigVars.australiumStatue) {
					event.getEntity().level
							.addFreshEntity(new EntityStatue(event.getEntity().level, event.getEntity(), false));
					TF2Util.sendTracking(new TF2Message.ActionMessage(19, event.getEntity()), event.getEntity());
					event.getEntity().playSound(TF2Sounds.WEAPON_TO_GOLD, 1.5f, 2f);
					// event.getEntityLiving().deathTime = 20;
					// event.getEntityLiving().onEntityUpdate();
					if (!(event.getEntity() instanceof Player)) {
						event.getEntity().setSilent(true);
					}
				}
				if (isEnemy && living.getCapability(TF2weapons.WEAPONS_CAP).isPresent()
						&& TF2Attribute.getModifier("Kill Count", stack, 0, living) != 0) {
					living.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).addHead(stack);
				}
				float toHeal = TF2Attribute.getModifier("Health Kill", stack, 0, living);
				if (toHeal != 0) {
					living.heal(TF2Util.getReducedHealing(living, event.getEntity(),
							isEnemy ? toHeal : toHeal * 0.35f));
				}
				if (TF2Attribute.getModifier("Crit Kill", stack, 0, living) > 0) {
					living.addEffect(new MobEffectInstance(TF2weapons.critBoost,
							(int) TF2Attribute.getModifier("Crit Kill", stack, -1, living) * 20, 1));
				}
				if (event.getEntity() instanceof Player && !event.getEntity().level.isClientSide) {
					ItemStack held = event.getEntity().getMainHandItem();
					if (!held.isEmpty() && held.getItem() instanceof ItemUsable) {
						((ItemUsable) held.getItem()).holster(
								event.getEntity().getCapability(TF2weapons.WEAPONS_CAP).orElse(null), held,
								event.getEntity(), event.getEntity().level);
					}
				}
			}
		}
		if (event.getEntity().getPersistentData().contains("Cleavers")) {
			ListTag list = event.getEntity().getPersistentData().getList("Cleavers", 10);
			for (int i = 0; i < list.size(); i++) {
				event.getEntity().spawnAtLocation(ItemStack.of(list.getCompound(i)), 0f);
			}
			event.getEntity().getPersistentData().remove("Cleavers");
		}
	}

	@SubscribeEvent
	public void pickAmmo(EntityItemPickupEvent event) {
		ItemStack stack = event.getItem().getItem();

		if (event.getItem().getPersistentData().getBoolean("Fake")) {
			event.setResult(Result.ALLOW);
			return;
		}

		if ((stack.getItem() instanceof ItemFoodThrowable) && stack.hasTag()
				&& stack.getTag().getBoolean("IsHealing")) {
			if (event.getEntity().getHealth() < event.getEntity().getMaxHealth()) {
				event.getEntity().heal(event.getEntity().getMaxHealth()
						* TF2weapons.itemSandvich.getFoodProperties(stack, event.getEntity()).getNutrition() / 28f);
				stack.shrink(1);
				if (stack.isEmpty())
					event.setResult(Result.ALLOW);
			} else {
				event.getEntity().getCooldowns().removeCooldown(stack.getItem());
			}
			stack.setTag(null);
			return;
		}

		if (WeaponsCapability.get(event.getEntity()).isInvisible()) {
			event.setResult(Result.DENY);
			event.setCanceled(true);
			return;
		}

		if (stack.getItem() instanceof ItemAmmoPackage) {
			ItemStack weapon = event.getEntity().getMainHandItem();
			int ammoType = stack.getDamageValue() % 16;
			if (!weapon.isEmpty() && weapon.getItem() instanceof ItemUsable
					&& ((ItemUsable) weapon.getItem()).getAmmoType(weapon) != 0
					&& ((ItemUsable) weapon.getItem()).getAmmoType(weapon) < ItemAmmo.AMMO_TYPES.length)
				ammoType = ((ItemUsable) weapon.getItem()).getAmmoType(weapon);
			else {
				for (ItemStack invstack : event.getEntity().getInventory().items) {
					if (!invstack.isEmpty() && invstack.getItem() instanceof ItemUsable
							&& ((ItemUsable) invstack.getItem()).getAmmoType(invstack) != 0
							&& ((ItemUsable) invstack.getItem()).getAmmoType(invstack) < ItemAmmo.AMMO_TYPES.length) {
						ammoType = ((ItemUsable) invstack.getItem()).getAmmoType(invstack);
						break;
					}
				}
			}
			stack = ItemAmmoPackage.convertPackage(stack, event.getEntity(), ammoType);
			event.getItem().setItem(stack);
			event.setResult(Result.DENY);
			event.setCanceled(true);
			return;
		}

		if (stack.getItem() instanceof ItemMoney) {
			ItemMoney.collect(stack, event.getEntity());
			event.setResult(Result.ALLOW);
		}
		/*
		 * if (!(stack.getItem() instanceof ItemCrate) && stack.hasTagCompound() &&
		 * stack.getTagCompound().getBoolean("DropFrom")) {
		 * event.getEntityPlayer().awardStat(TF2Achievements.SPOILS_WAR); }
		 */
		ItemStack stackam = TF2Util.pickAmmo(stack, event.getEntity(), false);
		stack.setCount(stackam.getCount());

		if (stack.isEmpty()) {
			event.setResult(Result.ALLOW);
			return;
		}
		event.setResult(Result.DEFAULT);
	}

	public static void onStrangeUpdate(ItemStack stack, LivingEntity player) {
		int points = 0;
		if (stack.getItem() instanceof ItemMedigun) {
			points = stack.getTag().getInt("Ubercharges");
		} else if (stack.getItem() instanceof ItemCloak) {
			points = stack.getTag().getInt("CloakTicks") / 400;
		} else {
			points = stack.getTag().getInt("Kills");
			points += stack.getTag().getInt("PlayerKills") * 5;
		}
		int calculatedLevel = 0;

		if (points >= STRANGE_KILLS[STRANGE_KILLS.length - 1]) {
			calculatedLevel = STRANGE_KILLS.length - 1;
		} else {
			for (int i = 1; i < STRANGE_KILLS.length; i++)
				if (points < STRANGE_KILLS[i]) {
					calculatedLevel = i - 1;
					break;
				}
		}

		if (calculatedLevel > stack.getTag().getInt("StrangeLevel")) {
			stack.getTag().putInt("StrangeLevel", calculatedLevel);
			if (player instanceof Player) {
				int rewardLevel = calculatedLevel < 10 ? calculatedLevel : calculatedLevel * 2 - 10;
				TF2Util.setExperiencePoints((Player) player,
						TF2Util.getExperiencePoints((Player) player) + 35 * rewardLevel);
				ItemHandlerHelper.giveItemToPlayer((Player) player,
						TF2Util.metaStack(TF2weapons.itemTF2, Mth.ceil(rewardLevel / 2f), 6));
				if (calculatedLevel == 20)
					ItemHandlerHelper.giveItemToPlayer((Player) player, TF2Util.metaStack(TF2weapons.itemTF2, 2, 7));
			}
			/*
			 * final int level = calculatedLevel; if (player instanceof EntityPlayerMP) {
			 * ((EntityPlayerMP) player).awardStat(new
			 * Achievement(Integer.toString(player.getRNG().nextInt()), "strangeUp", 0, 0,
			 * stack, null) {
			 *
			 * @Override public ITextComponent getStatName() { return
			 * super.getStatName().appendText(STRANGE_TITLES[level]); } }); }
			 */
		}
	}

	@SubscribeEvent
	public void addTable(LootTableLoadEvent event) {
		if (!TF2ConfigVars.disableLoot && (event.getName().equals(BuiltInLootTables.SIMPLE_DUNGEON)
				|| event.getName().equals(BuiltInLootTables.END_CITY_TREASURE)
				|| event.getName().equals(BuiltInLootTables.NETHER_BRIDGE)
				|| event.getName().equals(BuiltInLootTables.ABANDONED_MINESHAFT)
				|| event.getName().equals(BuiltInLootTables.STRONGHOLD_CORRIDOR))) {
			// System.out.println("loaded: "+new
			// ResourceLocation(TF2weapons.MOD_ID,event.getName().getPath()));
			event.getTable()
					.addPool(getLootPool(new ResourceLocation(TF2weapons.MOD_ID, event.getName().getPath())));
		}
	}

	@SubscribeEvent
	public void craftEvent(PlayerEvent.ItemCraftedEvent event) {
		/*
		 * if (event.crafting != null &&
		 * Block.getBlockFromItem(event.crafting.getItem()) == TF2weapons.blockCabinet)
		 * { event.player.awardStat(TF2Achievements.WEAPON_CRAFTING); }
		 */
	}

	@SubscribeEvent
	public void itemToss(ItemTossEvent event) {
		ItemStack stack = (event.getEntity().getItem());
		if (!event.getPlayer().level.isClientSide && stack.getItem() instanceof ItemUsable
				&& stack.getCapability(TF2weapons.WEAPONS_DATA_CAP).orElse(null).active > 0) {

			// event.getPlayer().getCapability(TF2weapons.WEAPONS_CAP).orElse(null).state = 0;
			WeaponsCapability.get(event.getPlayer()).setInactiveHand(InteractionHand.MAIN_HAND, stack);
		}

		if (stack.getItem() instanceof ItemFoodThrowable
				&& !event.getPlayer().getCooldowns().isOnCooldown(stack.getItem())) {
			event.getPlayer().getCooldowns().addCooldown(stack.getItem(),
					((ItemFoodThrowable) stack.getItem()).waitTime / (TF2ConfigVars.fastItemCooldown ? 2 : 1));
			if (!stack.hasTag())
				stack.setTag(new CompoundTag());
			stack.getTag().putBoolean("IsHealing", true);
		}

		if (stack.getItem() instanceof ItemFromData && stack.hasTag()
				&& (stack.getTag().getBoolean("Bought") || stack.getTag().getBoolean("Valve"))) {
			event.getEntity().setInvulnerable(true);
			event.getEntity().setThrower(event.getPlayer().getUUID());
		}
	}

	@SubscribeEvent
	public void itemExpire(ItemExpireEvent event) {
		ItemStack stack = event.getEntity().getItem();
		String thrower = TF2Util.getPlayerName(event.getEntity().level, event.getEntity().getThrower());
		if (stack.getItem() instanceof ItemFromData && thrower != null) {
			// System.out.println("put");
			HashMap<String, MerchantOffers> map = event.getEntity().level.getCapability(TF2weapons.WORLD_CAP).orElse(null).lostItems;
			if (!map.containsKey(thrower))
				map.put(thrower, new MerchantOffers());
			int cost = ItemFromData.getData(stack).getInt(PropertyType.COST) / 3;
			ItemStack ingot = TF2Util.metaStack(TF2weapons.itemTF2, cost / 9, 2);
			ItemStack nugget = TF2Util.metaStack(TF2weapons.itemTF2, cost % 9, 6);
			map.get(thrower).add(new MerchantOffer(ingot.getCount() > 0 ? ingot : nugget,
					nugget.getCount() > 0 && ingot.getCount() > 0 ? nugget : ItemStack.EMPTY, stack, 1, 0, 0f));

			// ItemHandlerHelper.insertItemStacked(map.get(thrower), stack, false);
		}
	}

	@SubscribeEvent
	public void startTrack(PlayerEvent.StartTracking event) {
		if (event.getTarget().getCapability(TF2weapons.WEAPONS_CAP).isPresent()) {
			TF2weapons.network.sendTo(new TF2Message.ActionMessage(WeaponsCapability.get(event.getTarget()).state,
					(LivingEntity) event.getTarget()), (ServerPlayer) event.getEntity());
			TF2weapons.network.sendTo(new TF2Message.CapabilityMessage(event.getTarget(), true),
					(ServerPlayer) event.getEntity());
		}
		if (event.getTarget().getCapability(TF2weapons.PLAYER_CAP).isPresent()) {
			if (event.getTarget() == event.getEntity())
				TF2weapons.network.sendTo(new TF2Message.PlayerCapabilityMessage(event.getTarget(), true, false),
						(ServerPlayer) event.getEntity());
			TF2weapons.network.sendTo(new TF2Message.PlayerCapabilityMessage(event.getTarget(), true, true),
					(ServerPlayer) event.getEntity());
		}

		if (event.getTarget().getCapability(TF2weapons.INVENTORY_CAP).isPresent() && !event.getTarget().level.isClientSide) {
			// System.out.println("Tracking");
			InventoryWearables inv = event.getTarget().getCapability(TF2weapons.INVENTORY_CAP).orElse(null);
			TF2weapons.network.sendTo(new TF2Message.WearableChangeMessage(event.getTarget(), 0, inv.getStackInSlot(0)),
					(ServerPlayer) event.getEntity());
			TF2weapons.network.sendTo(new TF2Message.WearableChangeMessage(event.getTarget(), 1, inv.getStackInSlot(1)),
					(ServerPlayer) event.getEntity());
			TF2weapons.network.sendTo(new TF2Message.WearableChangeMessage(event.getTarget(), 2, inv.getStackInSlot(2)),
					(ServerPlayer) event.getEntity());
			TF2weapons.network.sendTo(new TF2Message.WearableChangeMessage(event.getTarget(), 3, inv.getStackInSlot(3)),
					(ServerPlayer) event.getEntity());
		}
		if (event.getTarget() instanceof EntityTF2Character) {
			ItemStackHandler loadout = ((EntityTF2Character) event.getTarget()).loadout;
			for (int i = 0; i < loadout.getSlots(); i++) {
				// if (loadout.getStackInSlot(i).getItem() instanceof ItemFromData &&
				// loadout.getStackInSlot(i).getItem())
				TF2weapons.network.sendTo(
						new TF2Message.WearableChangeMessage(event.getTarget(), 20 + i, loadout.getStackInSlot(i)),
						(ServerPlayer) event.getEntity());
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void dropFakeItems(LivingDropsEvent event) {
		if (event.getEntity() instanceof Player && WeaponsCapability.get(event.getEntity()).isFeign())
			for (ItemEntity item : event.getDrops()) {
				item.getPersistentData().putBoolean("Fake", true);
				item.setExtendedLifetime();
			}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void dropItems(LivingDropsEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;
		InventoryWearables inv = event.getEntity().getCapability(TF2weapons.INVENTORY_CAP).orElse(null);
		for (int i = 2; i < 4; i++)
			if (!inv.getStackInSlot(i).isEmpty()) {
				event.getDrops().add(TF2Util.makeDrop(event.getEntity(), inv.getStackInSlot(i)));
			}
		for (int i = 5; i < 8; i++)
			if (!inv.getStackInSlot(i).isEmpty() && inv.getStackInSlot(i).getCount() >= 4)
				event.getDrops().add(TF2Util.makeDrop(event.getEntity(),
						inv.getStackInSlot(i).split(Mth.floor(inv.getStackInSlot(i).getCount() * 0.25f))));
	}

	@SuppressWarnings("unchecked")
	@SubscribeEvent
	public void looting(LootingLevelEvent event) {
		if (event.getDamageSource() instanceof TF2DamageSource) {
			event.setLootingLevel(event.getLootingLevel()
					+ (int) TF2Attribute.getModifier("Looting", ((TF2DamageSource) event.getDamageSource()).getWeapon(),
							0, (LivingEntity) event.getDamageSource().getEntity()));
		}
	}

	@SubscribeEvent
	public void containerOpen(PlayerContainerEvent.Open event) {
		if (!event.getEntity().level.isClientSide && event.getContainer() instanceof ContainerMercenary) {
			MerchantOffers merchantrecipelist = ((ContainerMercenary) event.getContainer()).mercenary.getOffers();
			if (merchantrecipelist != null)
				((ServerPlayer) event.getEntity()).sendMerchantOffers(event.getContainer().containerId,
						merchantrecipelist, 0, 0, false, false);
		}
	}

	@SubscribeEvent
	public void equipItem(LivingEquipmentChangeEvent event) {
		if (event.getEntity().getCapability(TF2weapons.WEAPONS_CAP).isPresent()
				&& WeaponsCapability.get(event.getEntity()).getUsedToken() >= 0
				&& event.getTo().getCapability(TF2weapons.WEAPONS_DATA_CAP).isPresent()) {
			event.getTo().getCapability(TF2weapons.WEAPONS_DATA_CAP).orElse(null).usedClass = WeaponsCapability
					.get(event.getEntity()).getUsedToken();
		}
	}

	@SubscribeEvent
	public void drops(LivingDropsEvent event) {
		if (event.getSource().getEntity() instanceof Player
				&& TF2Util.isEnemy((LivingEntity) event.getSource().getEntity(), event.getEntity())
				&& !(event.getEntity() instanceof IEntityTF2) && event.getSource() instanceof TF2DamageSource
				&& event.getEntity().getRandom()
						.nextFloat() < (TF2ConfigVars.dropAmmo * Math.sqrt(event.getEntity().getMaxHealth() / 20f)
								* (1 + event.getEntity().getArmorValue() / 13f))) {
			event.getDrops()
					.add(event.getEntity().spawnAtLocation(TF2Util.metaStack(TF2weapons.itemAmmoPackage, 1,
							1 + event.getEntity().getRandom().nextInt(ItemAmmoPackage.AMMO_PACKAGE_MIN.length - 1)),
							0));
		}
	}

	@SubscribeEvent
	public void arrowHeadshot(ProjectileImpactEvent event) {
		if (!(event.getProjectile() instanceof AbstractArrow) || !(event.getRayTraceResult() instanceof EntityHitResult))
			return;
		AbstractArrow arrow = (AbstractArrow) event.getProjectile();
		Entity hit = ((EntityHitResult) event.getRayTraceResult()).getEntity();
		if (hit instanceof LivingEntity && arrow.getPersistentData().getBoolean("TF2Arrow")) {
			HitResult trace = Iterables.getFirst(TF2Util.pierce(arrow.level, (LivingEntity) arrow.getOwner(),
					arrow.getX(), arrow.getY(), arrow.getZ(), arrow.getX() + arrow.getDeltaMovement().x,
					arrow.getY() + arrow.getDeltaMovement().y, arrow.getZ() + arrow.getDeltaMovement().z, true, 0.08f,
					false), null);
			float armor = (float) ((LivingEntity) hit).getAttribute(Attributes.ARMOR).getValue();
			if (TF2HitResult.getHitInfo(trace) instanceof Boolean && ((Boolean) TF2HitResult.getHitInfo(trace))) {
				arrow.getPersistentData().putBoolean("CritHeadshot", true);
				if (((LivingEntity) hit).getItemBySlot(EquipmentSlot.HEAD).isEmpty())
					arrow.setBaseDamage(arrow.getBaseDamage() * 1.25f);
				arrow.setBaseDamage(arrow.getBaseDamage() * (1 - armor * 0.0115));
			}
			arrow.setBaseDamage(arrow.getBaseDamage() * (1 - armor * 0.0152));
		}

	}

	public static LootPool getLootPool(ResourceLocation res) {
		return LootPool.lootPool().add(LootTableReference.lootTableReference(res)).setRolls(ConstantValue.exactly(1))
				.name(TF2weapons.MOD_ID + "_combined").build();
	}

	public static List<BannerPattern> getPatterns(BannerBlockEntity banner) {
		List<BannerPattern> patterns = new ArrayList<>();
		for (com.mojang.datafixers.util.Pair<net.minecraft.core.Holder<BannerPattern>, net.minecraft.world.item.DyeColor> pair : banner
				.getPatterns())
			patterns.add(pair.getFirst().value());
		return patterns;
	}

	public static class DestroyBlockEntry {
		public BlockPos pos;
		public float curDamage;
		public Level level;

		public DestroyBlockEntry(BlockPos pos, Level world) {
			this.level = world;
			this.pos = pos;
		}
	}

	public static class InboundDamage {
		public DamageSource source;
		public float damage;
		public int critical;
		public LivingEntity living;
		public ItemStack stack;

		public InboundDamage(DamageSource source, float damage, int critical, LivingEntity living,
				ItemStack stack) {
			this.source = source;
			this.damage = damage;
			this.critical = critical;
			this.living = living;
			this.stack = stack;
		}
	}

	public static class TF2WorldStorage implements ICapabilityProvider, INBTSerializable<CompoundTag> {

		public int eventFlag;

		public Level level;
		public HashMap<Entity, InboundDamage> damage = new HashMap<>();
		public ArrayList<BlockPos> banners = new ArrayList<>();
		public HashMap<String, MerchantOffers> lostItems = new HashMap<>();
		private HashMap<UUID, PlayerPersistStorage> playerStorage = new HashMap<>();
		public Map<UUID, InvasionEvent> invasions = new HashMap<>();
		public ArrayList<DestroyBlockEntry> destroyProgress = new ArrayList<>();
		public boolean silent;

		/*
		 * @Override public void readFromNBT(NBTTagCompound nbt) {
		 *
		 * System.out.println("Load world nbt"); }
		 *
		 * @Override public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		 *
		 * System.out.println("SAVE world nbt"); return compound; }
		 */
		public TF2WorldStorage(Level world) {
			this.level = world;
		}

		@Override
		public CompoundTag serializeNBT() {
			CompoundTag nbt = new CompoundTag();
			nbt.putInt("Event", eventFlag);
			CompoundTag items = new CompoundTag();
			nbt.put("Items", items);
			ListTag bannersS = new ListTag();
			for (BlockPos pos : banners) {

				ListTag coords = new ListTag();
				coords.add(IntTag.valueOf(pos.getX()));
				coords.add(IntTag.valueOf(pos.getY()));
				coords.add(IntTag.valueOf(pos.getZ()));
				bannersS.add(coords);
			}
			nbt.put("Banners", bannersS);
			for (Entry<String, MerchantOffers> entry : lostItems.entrySet()) {

				items.put(entry.getKey(), entry.getValue().createTag());
			}
			CompoundTag tagPlSt = new CompoundTag();
			nbt.put("PlayerStorage", tagPlSt);

			for (Entry<UUID, PlayerPersistStorage> entry : playerStorage.entrySet()) {
				if (entry.getValue().save)
					tagPlSt.put(entry.getKey().toString(), entry.getValue().serializeNBT());
			}

			CompoundTag invTag = new CompoundTag();
			for (Entry<UUID, InvasionEvent> entry : invasions.entrySet()) {
				invTag.put(entry.getKey().toString(), entry.getValue().serializeNBT());
			}
			nbt.put("Invasions", invTag);
			return nbt;
		}

		@Override
		public void deserializeNBT(CompoundTag nbt) {
			eventFlag = nbt.getInt("Event");
			CompoundTag items = nbt.getCompound("Items");
			CompoundTag mercslost = nbt.getCompound("MercsLost");
			CompoundTag tagPlSt = nbt.getCompound("PlayerStorage");
			for (String key : items.getAllKeys()) {
				MerchantOffers handler = new MerchantOffers(items.getCompound(key));
				lostItems.put(key, handler);
			}
			for (String key : tagPlSt.getAllKeys()) {
				PlayerPersistStorage storage = new PlayerPersistStorage(UUID.fromString(key));
				storage.deserializeNBT(tagPlSt.getCompound(key));
				this.playerStorage.put(UUID.fromString(key), storage);
			}
			for (String key : mercslost.getAllKeys()) {
				if (this.playerStorage.containsKey(UUID.fromString(key))) {
					ListTag list = nbt.getList(key, 11);
					for (int i = 0; i < list.size(); i++) {
						int[] pos = list.getIntArray(i);
						this.playerStorage.get(UUID.fromString(key)).lostMercPos
								.add(new BlockPos(pos[0], pos[1], pos[2]));
					}
				}

			}
			ListTag bannersS = nbt.getList("Banners", 9);
			for (int i = 0; i < bannersS.size(); i++) {
				ListTag coords = (ListTag) bannersS.get(i);
				banners.add(new BlockPos(coords.getInt(0), coords.getInt(1), coords.getInt(2)));
			}
			CompoundTag invTag = nbt.getCompound("Invasions");
			for (String key : invTag.getAllKeys()) {
				invasions.put(UUID.fromString(key), new InvasionEvent(this.level, invTag.getCompound(key)));
			}
		}

		public boolean hasCapabilityLegacy(Capability<?> capability, Direction facing) {
			return TF2weapons.WORLD_CAP != null && capability == TF2weapons.WORLD_CAP;
		}

		@Override
	public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(Capability<T> capability, @javax.annotation.Nullable Direction facing) {
		T value = this.getCapabilityLegacy(capability, facing);
		return value == null ? net.minecraftforge.common.util.LazyOptional.empty() : net.minecraftforge.common.util.LazyOptional.of(() -> value);
	}

	public <T> T getCapabilityLegacy(Capability<T> capability, Direction facing) {
			if (TF2weapons.WORLD_CAP != null && capability == TF2weapons.WORLD_CAP)
				return (T) (Object) this;
			return null;
		}

		public PlayerPersistStorage getPlayerStorage(Player player) {
			return this.getPlayerStorage(player.getUUID());
		}

		public PlayerPersistStorage getPlayerStorage(UUID player) {
			if (!this.playerStorage.containsKey(player))
				playerStorage.put(player, new PlayerPersistStorage(player));
			return this.playerStorage.get(player);
		}

		public boolean startInvasion(Player player, int difficulty, boolean force) {
			for (Entry<UUID, InvasionEvent> entry : invasions.entrySet()) {
				if (entry.getKey().equals(player.getUUID()) || entry.getValue().isInRange(player.blockPosition())) {
					if (force)
						entry.getValue().finish();
					else
						return false;
				}
			}

			InvasionEvent event = new InvasionEvent(this.level, player.blockPosition(), difficulty);
			this.invasions.put(player.getUUID(), event);
			return true;
		}
	}

	public static class TF2ContainerListener implements ContainerListener {

		public ServerPlayer player;

		public TF2ContainerListener(ServerPlayer player) {
			this.player = player;
		}

		@Override
		public void slotChanged(AbstractContainerMenu containerToSend, int slotInd, ItemStack stack) {
			if (player.getMainHandItem().getItem() instanceof ItemWeapon)
				TF2weapons.network.sendTo(new TF2Message.UseMessage(
						((ItemWeapon) player.getMainHandItem().getItem()).getClip(player.getMainHandItem()),
						false, ((ItemUsable) player.getMainHandItem().getItem()).getAmmoAmount(player,
								player.getMainHandItem()),
						InteractionHand.MAIN_HAND), player);
		}

		@Override
		public void dataChanged(AbstractContainerMenu containerIn, int varToUpdate, int newValue) {}

	}
}
