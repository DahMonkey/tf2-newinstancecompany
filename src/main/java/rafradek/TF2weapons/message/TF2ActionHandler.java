package rafradek.TF2weapons.message;

import rafradek.TF2weapons.TF2PlayerCapability;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientHandler;
import rafradek.TF2weapons.common.TF2Achievements;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.EntityStatue;
import rafradek.TF2weapons.entity.mercenary.EntityEngineer;
import rafradek.TF2weapons.entity.mercenary.EntityMedic;
import rafradek.TF2weapons.entity.mercenary.EntitySoldier;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character.Order;
import rafradek.TF2weapons.item.*;
import rafradek.TF2weapons.util.Contract;
import rafradek.TF2weapons.util.PlayerPersistStorage;
import rafradek.TF2weapons.util.TF2Util;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.items.ItemHandlerHelper;

public class TF2ActionHandler implements IMessageHandler<TF2Message.ActionMessage, IMessage> {

	/*
	 * public static Map<EntityLivingBase,Integer> playerAction=new
	 * HashMap<EntityLivingBase,Integer>(); public static
	 * Map<EntityLivingBase,Integer> playerActionClient=new
	 * HashMap<EntityLivingBase,Integer>();
	 */
	// public static ThreadLocalMap<EntityLivingBase,Integer> playerAction=new
	// ThreadLocalMap<EntityLivingBase,Integer>();
	// public static ThreadLocalMap<EntityLivingBase,Integer>
	// previousPlayerAction=new ThreadLocalMap<EntityLivingBase,Integer>();
	@Override
	public IMessage onMessage(final TF2Message.ActionMessage message, final MessageContext ctx) {
		if (ctx.side == Dist.SERVER) {
			final ServerPlayer player = ctx.getServerHandler().player;
			((ServerLevel) player.level).addScheduledTask(() -> {
				if (message.value <= 15) {
					handleMessage(message, player, false);
					message.entity = player.getId();
					TF2Util.sendTrackingExcluding(message, player);
				} else if (message.value == 99) {
					Entity wearer = ctx.getServerHandler().player.level.getEntity(message.entity);
					// System.out.println("ID: "+message.entity+" "+wearer);
					if (wearer == null || !(wearer instanceof Player))
						wearer = player;
					TF2weapons.network.sendTo(new TF2Message.WearableChangeMessage(wearer, 0,
							wearer.getCapability(TF2weapons.INVENTORY_CAP).orElse(null).getStackInSlot(0)), player);
					TF2weapons.network.sendTo(new TF2Message.WearableChangeMessage(wearer, 1,
							wearer.getCapability(TF2weapons.INVENTORY_CAP).orElse(null).getStackInSlot(1)), player);
					TF2weapons.network.sendTo(new TF2Message.WearableChangeMessage(wearer, 2,
							wearer.getCapability(TF2weapons.INVENTORY_CAP).orElse(null).getStackInSlot(2)), player);
					TF2weapons.network.sendTo(new TF2Message.WearableChangeMessage(wearer, 3,
							wearer.getCapability(TF2weapons.INVENTORY_CAP).orElse(null).getStackInSlot(3)), player);
				} else if (message.value == 16) {
					player.level.getScoreboard().addPlayerToTeam(player.getName(), "RED");
					// player.addStat(TF2Achievements.JOIN_TEAM);
				} else if (message.value == 17) {
					player.level.getScoreboard().addPlayerToTeam(player.getName(), "BLU");
					// player.addStat(TF2Achievements.JOIN_TEAM);
				} else if (message.value == 18 && player.containerMenu != null
						&& player.containerMenu instanceof MerchantMenu
						&& player.level.getCapability(TF2weapons.WORLD_CAP).orElse(null).lostItems
						.containsKey(player.getName())) {
					player.closeScreen();
					final MerchantOffers listg = player.level.getCapability(TF2weapons.WORLD_CAP).orElse(null).lostItems
							.get(player.getName());
					if (listg != null) {
						Iterator<MerchantOffer> iterator = listg.iterator();
						while (iterator.hasNext()) {
							MerchantOffer recipe = iterator.next();
							if (recipe != null && recipe.getItemToBuy().isEmpty()) {
								ItemHandlerHelper.giveItemToPlayer(player, recipe.getItemToSell());
								iterator.remove();
							}
						}
					}
					player.level.getCapability(TF2weapons.WORLD_CAP).orElse(null).lostItems.get(player.getName());
					player.displayVillagerTradeGui(new Merchant() {

						MerchantOffers list;

						@Override
						public void setCustomer(Player player) {
							if (player == null && list != null) {
								Iterator<MerchantOffer> iterator = list.iterator();
								while (iterator.hasNext()) {
									MerchantOffer recipe = iterator.next();
									if (recipe != null && recipe.isRecipeDisabled()) {
										iterator.remove();
									}
								}
							}

						}

						@Override
						public Player getCustomer() {
							return player;
						}

						@Override
						public MerchantOffers getRecipes(Player player) {
							if (list == null)
								list = listg;
							return list;
						}

						@Override
						public void setRecipes(MerchantOffers recipeList) {
							list = recipeList;
						}

						@Override
						public void useRecipe(MerchantOffer recipe) {
							recipe.incrementToolUses();

						}

						@Override
						public void verifySellingItem(ItemStack stack) {

						}

						@Override
						public Component getDisplayName() {
							return Component.translatable("gui.recoveritems");
						}

						@Override
						public Level getWorld() {
							return player.level;
						}

						@Override
						public BlockPos getPos() {
							return player.blockPosition();
						}

					});
					// FMLNetworkHandler.openGui(player, TF2weapons.instance, 4, player.world,(int)
					// player.posX,(int) player.posY,(int) player.posZ);
				} else if (message.value == 23
						&& (WeaponsCapability.get(player).airJumps < WeaponsCapability.get(player).getMaxAirJumps()
								|| WeaponsCapability.get(player).isGrappled())) {
					player.fallDistance = 0;
					if (!WeaponsCapability.get(player).isGrappled()) {
						WeaponsCapability.get(player).airJumps += 1;
						player.getServerWorld().addParticle(ParticleTypes.CLOUD, player.getX(), player.getY(),
								player.getZ(), 12, 1, 0.2, 1, 0D);
					} else {
						if (player.getMainHandItem().getItem() instanceof ItemGrapplingHook) {
							WeaponsCapability.get(player).setPrimaryCooldown(InteractionHand.MAIN_HAND,
									((ItemGrapplingHook) player.getMainHandItem().getItem())
									.getFiringSpeed(player.getMainHandItem(), player));
							player.motionY += 0.42;
							player.hurtMarked = true;
						}
					}
					WeaponsCapability.get(player).setGrapplingHook(null);
				} else if (message.value == 25) {
					ItemStack stack1 = ItemBackpack.getBackpack(player);
					if (!stack1.isEmpty() && stack1.getItem() instanceof ItemParachute) {
						stack1.getTag().putBoolean("Deployed", !stack1.getTag().getBoolean("Deployed"));
					}
				} else if (message.value == 29) {
					player.level.getScoreboard().removePlayerFromTeams(player.getName());
				} else if (message.value == 30) {
					ItemStack stack2 = ItemBackpack.getBackpack(player);
					if (stack2.getItem() instanceof ItemJetpack
							&& TF2Attribute.getModifier("Jetpack Item", stack2, 0f, player) != 0
							&& ((ItemJetpack) stack2.getItem()).canActivate(stack2, player)) {
						((ItemJetpack) stack2.getItem()).activateJetpack(stack2, player, true);
					}
				} else if (message.value == 26) {
					TF2PlayerCapability.get(player).setEquipBackpackItem(true);
				} else if (message.value == 27) {
					TF2PlayerCapability.get(player).setEquipBackpackItem(false);
				} else if (message.value >= 32 && message.value < 48) {
					int id1 = message.value - 32;
					if (player != null && id1 < player.getCapability(TF2weapons.PLAYER_CAP).orElse(null).contracts.size()) {
						player.getCapability(TF2weapons.PLAYER_CAP).orElse(null).contracts.get(id1).active = true;
					}
				} else if (message.value >= 48 && message.value < 64) {
					int id2 = message.value - 48;
					if (player != null && id2 < player.getCapability(TF2weapons.PLAYER_CAP).orElse(null).contracts.size()) {
						Contract contract = player.getCapability(TF2weapons.PLAYER_CAP).orElse(null).contracts.get(id2);
						contract.completeContract(player);
						if (contract.progress >= Contract.REWARD_HIGH)
							player.getCapability(TF2weapons.PLAYER_CAP).orElse(null).contracts.remove(id2);
					}
				} else if (message.value >= 64 && message.value < 80) {
					int id3 = message.value - 64;
					if (player != null && id3 < player.getCapability(TF2weapons.PLAYER_CAP).orElse(null).contracts.size()) {
						player.getCapability(TF2weapons.PLAYER_CAP).orElse(null).contracts.remove(id3);
						player.getStatFile().unlockAchievement(player, TF2Achievements.CONTRACT_DAY,
								(int) (player.level.getDayTime() / 24000 + 1));
					}
				} else if (message.value >= 100 && message.value < 109) {
					int id4 = message.value - 100;
					if (player != null && player.getMainHandItem().getItem() instanceof IItemSlotNumber) {
						((IItemSlotNumber) player.getMainHandItem().getItem())
						.onSlotSelection(player.getMainHandItem(), player, id4);

					}
				} else if (message.value >= 110 && message.value < 119) {
					int id5 = message.value - 110;
					if (player != null) {
						if (id5 == 0) {
							player.getCapability(TF2weapons.PLAYER_CAP).orElse(null).medicCall = 100;
							boolean success1 = false;
							for (EntityMedic medic1 : player.level.getEntities(EntityMedic.class,
									test1 -> (test1.getOwner() == player))) {
								if (TF2Util.teleportSafe(medic1, player)) {
									success1 = true;
									medic1.setOrder(Order.FOLLOW);
									break;
								}
							}
							if (!success1) {
								Iterator<BlockPos> it1 = PlayerPersistStorage.get(player).medicMercPos.iterator();
								while (it1.hasNext()) {
									BlockPos pos1 = it1.next();
									success1 = false;
									ArrayList<EntityMedic> list1 = new ArrayList<>();
									player.level.getChunkFromBlockCoords(pos1).getEntitiesOfTypeWithinAABB(
											EntityMedic.class, new AABB(pos1), list1,
											test2 -> player.getUUID().equals(test2.getOwnerId()));
									for (EntityMedic medic2 : list1) {
										if (TF2Util.teleportSafe(medic2, player)) {

											success1 = true;
											medic2.setOrder(Order.FOLLOW);
										}
									}

									if (success1) {
										it1.remove();
										break;
									} else if (list1.isEmpty())
										it1.remove();
								}
							}
						} else if (id5 == 1) {
							boolean success2 = false;
							for (EntityTF2Character living1 : player.level.getEntities(EntityTF2Character.class,
									test3 -> (test3.getOwner() == player
									&& !(test3 instanceof EntityMedic || test3 instanceof EntityEngineer)))) {
								if (TF2Util.teleportSafe(living1, player)) {
									success2 = true;
									living1.setOrder(Order.FOLLOW);
									break;
								}
							}
							if (!success2) {
								Iterator<BlockPos> it2 = PlayerPersistStorage.get(player).restMercPos.iterator();
								while (it2.hasNext()) {
									BlockPos pos2 = it2.next();
									success2 = false;
									ArrayList<EntityTF2Character> list2 = new ArrayList<>();
									player.level.getChunkFromBlockCoords(pos2).getEntitiesOfTypeWithinAABB(
											EntityTF2Character.class, new AABB(pos2), list2,
											test4 -> player.getUUID().equals(test4.getOwnerId()));
									for (EntityTF2Character medic3 : list2) {
										if (TF2Util.teleportSafe(medic3, player)) {

											success2 = true;
											medic3.setOrder(Order.FOLLOW);
										}
									}

									if (success2) {
										it2.remove();
										break;
									} else if (list2.isEmpty())
										it2.remove();
								}
							}
							List<Mob> attackers = player.level.getEntitiesOfClass(Mob.class,
									player.getBoundingBox().inflate(20, 8, 20),
									test5 -> (!TF2Util.isAlliedTo(player, test5)
											&& test5.getTarget() == player));
							if (attackers.size() > 0)
								for (EntityTF2Character living2 : player.level.getEntitiesOfClass(
										EntityTF2Character.class, player.getBoundingBox().inflate(20, 8, 20),
										test6 -> (TF2Util.isAlliedTo(player, test6)
												&& test6.getTarget() == null))) {
									living2.setTarget(attackers.get(player.getRandom().nextInt(attackers.size())));
								}
						} else if (id5 == 2) {
							player.getCapability(TF2weapons.PLAYER_CAP).orElse(null).medicCharge = true;
						} else if (id5 == 3) {
							for (EntitySoldier living3 : player.level.getEntitiesOfClass(EntitySoldier.class,
									player.getBoundingBox().inflate(20, 8, 20),
									test7 -> (TF2Util.isAlliedTo(player, test7) && ItemBackpack.getBackpack(test7)
											.getItem() instanceof ItemSoldierBackpack))) {
								living3.activateBackpack();
							}
						} else if (id5 == 4) {
							HitResult trace = player.level.rayTraceBlocks(player.getEyePosition(1),
									player.getEyePosition(1).add(player.getViewVector(1).scale(40)));
							if (trace != null) {
								BlockPos pos3 = trace.getBlockPos().move(trace.getDirection());
								List<EntityTF2Character> list3 = player.level.getEntitiesOfClass(
										EntityTF2Character.class, player.getBoundingBox().inflate(20, 8, 20),
										test8 -> (test8.getOwner() == player));
								boolean hasFollow = false;
								for (EntityTF2Character living4 : list3) {
									if (living4.getOrder() == Order.FOLLOW) {
										living4.setHomePosAndDistance(pos3, 0);
										living4.getNavigation().tryMoveToXYZ(pos3.getX(), pos3.getY(), pos3.getZ(), 1);
										living4.setOrder(Order.HOLD);
										hasFollow = true;
									}
								}
								if (!hasFollow && !list3.isEmpty()) {
									list3.get(0).setHomePosAndDistance(pos3, 0);
									list3.get(0).getNavigation().tryMoveToXYZ(pos3.getX(), pos3.getY(), pos3.getZ(), 1);
									list3.get(0).setOrder(Order.HOLD);
								}
							}
						} else if (id5 == 5) {
							for (EntityEngineer living5 : player.level.getEntitiesOfClass(EntityEngineer.class,
									player.getBoundingBox().inflate(40, 15, 40),
									test9 -> TF2Util.isAlliedTo(player, test9))) {

							}
						}
					}
				} else if (message.value >= 120 && message.value < 130) {
					if (TF2PlayerCapability.get(player).getGameArena() != null) {
						TF2PlayerCapability.get(player).getGameArena().tryPlayerJoinTeam(player, message.value - 110);
					}
				}
			});
		} else {
			final LivingEntity player = ClientHandler.getClientEntity(message.entity);
			Minecraft.getInstance().addScheduledTask(() -> {
				if (message.value <= 15)
					handleMessage(message, player, true);
				else if (message.value == 19) {
					if (player != null && player != Minecraft.getInstance().player
							&& !(player.getCapability(TF2weapons.WEAPONS_CAP).isPresent()
									&& WeaponsCapability.get(player).isFeign())) {
						player.discard();
						// player.world.spawnEntity(new EntityStatue(player.world, player,false));
					}
				} else if (message.value == 24) {
					if (player != null) {
						player.level.addFreshEntity(new EntityStatue(player.level, player, true));
					}
				} else if (message.value == 22) {
					if (player != null && player.getMainHandItem() != null
							&& player.getMainHandItem().hasTag())
						player.getMainHandItem().getTag().putByte("active", (byte) 2);
				} else if (message.value == 27) {
					if (player != null) {
						ItemStack stack = player.getMainHandItem();
						if (!stack.isEmpty() && stack.getItem() instanceof ItemWeapon) {
							// System.out.println("dd");
							WeaponsCapability.get(player).fireCoolReduced = true;
							// WeaponData.getCapability(stack).fire1Cool-=;
						}
					}
				} else if (message.value == 28) {
					if (player != null) {
						WeaponsCapability.get(player).expJumpGround = 2;
					}
				} else if (message.value == 30) {
					ItemStack chest = ItemBackpack.getBackpack(player);
					if (chest.getItem() instanceof ItemJetpack) {
						((ItemJetpack) chest.getItem()).activateJetpack(chest, player, true);
					}
				} else if (message.value == 31) {
					if (player != null)
						TF2PlayerCapability.get((Player) player).setEquipBackpackItem(true);
				} else if (message.value == 32) {
					if (player != null)
						TF2PlayerCapability.get((Player) player).setEquipBackpackItem(false);
				}
			});
		}
		return null;
	}

	/*
	 * public static class TF2ActionHandlerReturn implements
	 * IMessageHandler<TF2Message.ActionMessage, IMessage> {
	 *
	 * @Override public IMessage onMessage(TF2Message.ActionMessage message,
	 * MessageContext ctx) { EntityLivingBase player=(EntityLivingBase)
	 * Minecraft.getMinecraft().theWorld.getEntityByID(message.entity);
	 * handleMessage(message, player); return null; }
	 *
	 * }
	 */
	public static void handleMessage(TF2Message.ActionMessage message, LivingEntity player, boolean client) {
		if (player != null) {
			/*
			 * int oldValue=playerAction.get().containsKey(player)?playerAction.get(
			 * ).get(player):0; if(player.getHeldItem(EnumHand.MAIN_HAND) != null &&
			 * player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemUsable){
			 * if((oldValue&1)==0&&(message.value&1)!=0){
			 * ((ItemUsable)player.getHeldItem(EnumHand.MAIN_HAND).getItem()).
			 * startUse(player.getHeldItem(EnumHand.MAIN_HAND), player, player.world); }
			 * if((oldValue&1)==0&&(message.value&1)!=0){
			 * ((ItemUsable)player.getHeldItem(EnumHand.MAIN_HAND).getItem()).
			 * endUse(player.getHeldItem(EnumHand.MAIN_HAND), player, player.world); } }
			 */
			/*
			 * if(previousPlayerAction.get(player.world.isRemote).containsKey (player)){
			 * previousPlayerAction.get(player.world.isRemote).put(player, 0); } int
			 * oldState=previousPlayerAction.get(player.world.isRemote).get( player);
			 *
			 * previousPlayerAction.get(player.world.isRemote).put(player,
			 * playerAction.get(true).get(player));
			 */

			WeaponsCapability cap = player.getCapability(TF2weapons.WEAPONS_CAP).orElse(null);
			ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
			int oldState = cap.state & 3;
			// System.out.println("Action: "+message.value);
			cap.state = message.value + (cap.state & 8);
			if (!stack.isEmpty() && stack.getItem() instanceof ItemUsable && oldState != (message.value & 3)
					&& stack.getCapability(TF2weapons.WEAPONS_DATA_CAP).orElse(null).active == 2) {
				int stateOverride = ((ItemUsable) stack.getItem()).getStateOverride(stack, player, cap.state);
				if ((oldState & 2) < (message.value & 2)) {
					((ItemUsable) stack.getItem()).startUse(stack, player, player.level, oldState, message.value & 3);

					cap.stateDo(player, stack, InteractionHand.MAIN_HAND, stateOverride);

				} else if ((oldState & 2) > (message.value & 2))
					((ItemUsable) stack.getItem()).endUse(stack, player, player.level, oldState, message.value & 3);
				if ((oldState & 1) < (message.value & 1)) {
					((ItemUsable) stack.getItem()).startUse(stack, player, player.level, oldState, message.value & 3);
					cap.stateDo(player, stack, InteractionHand.MAIN_HAND, stateOverride);
				} else if ((oldState & 1) > (message.value & 1))
					((ItemUsable) stack.getItem()).endUse(stack, player, player.level, oldState, message.value & 3);
			}
			// System.out.println("change
			// "+playerAction.get(player.world.isRemote).get(player));
			// System.out.println("dostal: "+message.value);
		}
	}

}
