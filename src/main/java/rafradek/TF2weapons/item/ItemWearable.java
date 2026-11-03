package rafradek.TF2weapons.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.common.MapList;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.projectile.EntityProjectileBase;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Explosion;
import rafradek.TF2weapons.util.TF2Util;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemWearable extends ItemFromData {

	public static int usedModel;

	@SuppressWarnings("unchecked")
	public static final Tuple<String, AttributeModifier>[] EFFECT_MODIFIERS = new Tuple[] {
			new Tuple<>(Attributes.ATTACK_DAMAGE.getName(), new AttributeModifier("Mod e1", 0.1, 2)),
			new Tuple<>(Attributes.MAX_HEALTH.getName(), new AttributeModifier("Mod e2", 1, 0)),
			new Tuple<>(Attributes.MOVEMENT_SPEED.getName(), new AttributeModifier("Mod e3", 0.12, 1)),
			new Tuple<>(Attributes.ARMOR_TOUGHNESS.getName(), new AttributeModifier("Mod e4", 3, 0)),
			new Tuple<>(Attributes.ATTACK_SPEED.getName(), new AttributeModifier("Mod e5", 0.14, 2)),
			new Tuple<>(Attributes.ATTACK_DAMAGE.getName(), new AttributeModifier("Mod e6", 1, 0)),
			new Tuple<>(Attributes.ATTACK_SPEED.getName(), new AttributeModifier("Mod e7", 0.08, 2)),
			new Tuple<>(Attributes.MAX_HEALTH.getName(), new AttributeModifier("Mod e8", 2, 0)),
			new Tuple<>(Attributes.ARMOR_TOUGHNESS.getName(), new AttributeModifier("Mod e9", 4, 0)),
			new Tuple<>(Attributes.ATTACK_DAMAGE.getName(), new AttributeModifier("Mod e10", 0.08, 2)),
			new Tuple<>(Attributes.MOVEMENT_SPEED.getName(), new AttributeModifier("Mod e11", 0.16, 1)),
			new Tuple<>(Attributes.ATTACK_SPEED.getName(), new AttributeModifier("Mod e12", 0.12, 2)),
			new Tuple<>(Attributes.MOVEMENT_SPEED.getName(), new AttributeModifier("Mod e13", 0.1, 1)),
			new Tuple<>(Attributes.MAX_HEALTH.getName(), new AttributeModifier("Mod e14", 0.1, 2)),
			new Tuple<>(Attributes.ARMOR_TOUGHNESS.getName(), new AttributeModifier("Mod e15", 5, 0)) };

	public ItemWearable() {
		super();
		this.addPropertyOverride(new ResourceLocation("bodyModel"), (ItemStack stack, @Nullable Level world,
				@Nullable LivingEntity entityIn) -> (ItemWearable.usedModel == 1) ? 1 : 0);
		this.addPropertyOverride(new ResourceLocation("headModel"), (ItemStack stack, @Nullable Level world,
				@Nullable LivingEntity entityIn) -> (ItemWearable.usedModel == 2) ? 1 : 0);
	}

	@Override
	public boolean isValidArmor(ItemStack stack, EquipmentSlot slot, Entity player) {
		return slot == (isHat(stack) ? EquipmentSlot.HEAD : EquipmentSlot.CHEST);
	}

	@Override
	public EquipmentSlot getEquipmentSlot(ItemStack stack) {
		return isHat(stack) ? EquipmentSlot.HEAD : EquipmentSlot.CHEST;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player living, InteractionHand hand) {
		if (!world.isClientSide)
			FMLNetworkHandler.openGui(living, TF2weapons.instance, 0, world, 0, 0, 0);
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, living.getItemInHand(hand));
	}

	public boolean isHat(ItemStack stack) {
		return (getData(stack).getInt(PropertyType.WEAR) & 1) == 1;
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
		return getData(stack).getString(PropertyType.ARMOR_IMAGE);
	}

	@Override
	public void onArmorTick(Level world, Player player, ItemStack itemStack) {
		onUpdateWearing(itemStack, world, player);
	}

	public void applyRandomEffect(ItemStack stack, Random rand) {
		stack.getTag().putByte("UEffect", (byte) rand.nextInt(11));
	}

	@Override
	public String getName(ItemStack stack) {
		String name = super.getName(stack);
		if (stack.hasTag() && stack.getTag().contains("UEffect"))
			name = ChatFormatting.DARK_PURPLE + "Unusual " + name;
		return name;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level world, List<String> tooltip, TooltipFlag advanced) {
		super.appendHoverText(stack, world, tooltip, advanced);
		if (stack.hasTag()) {

			if (stack.getTag().contains("UEffect")) {
				tooltip.add("");
				String str = I18n.get("item.wearable.effect." + stack.getTag().getByte("UEffect"));
				tooltip.add(I18n.get("item.wearable.effect") + " " + str);
			}
		}
	}

	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
		Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);

		if (slot == EquipmentSlot.HEAD && stack.getTag().contains("UEffect")) {
			Tuple<String, AttributeModifier> tuple = EFFECT_MODIFIERS[stack.getTag().getByte("UEffect")
					% EFFECT_MODIFIERS.length];
			multimap.put(tuple.getFirst(), tuple.getSecond());
		}

		return multimap;
	}

	public void onUpdateWearing(ItemStack stack, Level par2World, LivingEntity living) {
		if (!living.level.isClientSide && living.deathTime == 18
				&& TF2Attribute.getModifier("Explode Death", stack, 0, living) != 0) {
			TF2Explosion explosion = new TF2Explosion(living.level, living, living.getX(), living.getY() + 0.5, living.getZ(),
					5, null, 0, 3, SoundEvents.ENTITY_GENERIC_EXPLODE);
			// System.out.println("ticks: "+this.ticksExisted);
			explosion.isFlaming = false;
			explosion.isSmoking = true;
			explosion.doExplosionA();
			explosion.doExplosionB(true);
			Iterator<Entity> affectedIterator = explosion.affectedEntities.keySet().iterator();
			while (affectedIterator.hasNext()) {
				Entity ent = affectedIterator.next();
				TF2Util.dealDamage(ent, living.level, living, ItemStack.EMPTY, 2,
						explosion.affectedEntities.get(ent) * 26,
						TF2Util.causeDirectDamage(stack, living).setCritical(2).setExplosion());
			}
			Iterator<Player> iterator = living.level.players().iterator();

			while (iterator.hasNext()) {
				Player entityplayer = iterator.next();

				if (entityplayer.distanceToSqr(living.getX(), living.getY() + 0.5, living.getZ()) < 4096.0D) {
					((ServerPlayer) entityplayer).connection
							.sendPacket(new ClientboundExplodePacket(living.getX(), living.getY() + 0.5, living.getZ(), 4,
									explosion.affectedBlockPositions, explosion.getKnockbackMap().get(entityplayer)));
				}
			}
		}

		if (WeaponsCapability.get(living).isDisguised())
			return;
		if (!living.level.isClientSide && living.tickCount % 13 == 0
				&& TF2Attribute.getModifier("Bomb Enemy", stack, 0, living) != 0) {
			LivingEntity target = WeaponsCapability.get(living).lastAttacked;
			if (target == null || !target.isAlive() || target.distanceToSqr(living) > 200) {
				target = Iterables
						.getFirst(
								living.level
										.getEntitiesOfClass(Mob.class,
												living.getBoundingBox().inflate(11),
												ent -> ent.getTarget() == living
														&& TargetGoal.isSuitableTarget(ent, living, false, true)),
								null);
			}
			if (target != null && living.hasLineOfSight(target) && target.isAlive()
					&& target.distanceToSqr(living) < 144) {
				ItemStack stackW = ItemFromData.getNewStack("bombinomiconbomb");
				// ((ItemWeapon)stackW.getItem()).shoot(stackW, living, living.world, 0,
				// EnumHand.OFF_HAND);

				try {
					EntityProjectileBase proj = MapList.projectileClasses
							.get(ItemFromData.getData(stackW).getString(PropertyType.PROJECTILE))
							.getConstructor(Level.class).newInstance(living.level);
					proj.damageModifier = TF2Attribute.getModifier("Bomb Enemy", stack, 0, living);
					proj.initProjectile(living, InteractionHand.MAIN_HAND, stackW);
					double x = target.getX();
					double y = target.getY() + target.getEyeHeight();
					double z = target.getZ();
					// float speed = TF2Attribute.getModifier("Proj Speed", stackW,
					// ItemFromData.getData(stackW).getFloat(PropertyType.PROJECTILE_SPEED),
					// living);
					proj.face(x, y, z, 1);
					living.level.addFreshEntity(proj);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
		if (living.level.isClientSide
				&& (living != ClientProxy.getLocalPlayer()
						|| Minecraft.getInstance().options.thirdPersonView != 0)
				&& stack.getTag().contains("UEffect")) {
			ParticleTypes type;
			switch (stack.getTag().getByte("UEffect")) {
			case 0:
				type = ParticleTypes.FLAME;
				break;
			case 1:
				type = ParticleTypes.HEART;
				break;
			case 2:
				type = ParticleTypes.SMOKE_LARGE;
				break;
			case 3:
				type = ParticleTypes.WATER_BUBBLE;
				break;
			case 4:
				type = ParticleTypes.VILLAGER_ANGRY;
				break;
			case 5:
				type = ParticleTypes.VILLAGER_HAPPY;
				break;
			case 6:
				type = ParticleTypes.SUSPENDED_DEPTH;
				break;
			case 7:
				type = ParticleTypes.SLIME;
				break;
			case 8:
				type = ParticleTypes.CRIT_MAGIC;
				break;
			case 9:
				type = ParticleTypes.DRAGON_BREATH;
				break;
			case 10:
				type = ParticleTypes.NOTE;
				break;
			case 11:
				type = ParticleTypes.FIREWORKS_SPARK;
				break;
			case 12:
				type = ParticleTypes.EXPLOSION_NORMAL;
				break;
			case 13:
				type = ParticleTypes.SPELL_MOB;
				break;
			default:
				type = ParticleTypes.END_ROD;
				break;
			}
			par2World.spawnParticle(type,
					living.getX() + living.getRandom().nextDouble() * living.getBbWidth() - (living.getBbWidth() / 2),
					living.getY() + living.getBbHeight() + living.getRandom().nextDouble() * 0.2,
					living.getZ() + living.getRandom().nextDouble() * living.getBbWidth() - (living.getBbWidth() / 2), living.getDeltaMovement().x,
					living.getRandom().nextDouble() * 0.02, living.getDeltaMovement().z, new int[0]);
		}
	}
}
