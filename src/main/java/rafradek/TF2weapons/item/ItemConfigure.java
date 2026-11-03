package rafradek.TF2weapons.item;

import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.entity.EntityPickup;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.tileentity.IEntityConfigurable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;

public class ItemConfigure extends LegacyItem {

	public ItemConfigure() {
		this.setUnlocalizedName(TF2weapons.MOD_ID + ".configurator");
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		InteractionHand hand = context.getHand();
		Direction facing = context.getClickedFace();
		float hitX = (float) (context.getClickLocation().x - pos.getX());
		float hitY = (float) (context.getClickLocation().y - pos.getY());
		float hitZ = (float) (context.getClickLocation().z - pos.getZ());
		if (!world.isClientSide) {
			BlockEntity ent = world.getBlockEntity(pos);
			if (ent instanceof IEntityConfigurable) {
				// TF2weapons.openGui((net.minecraft.world.entity.player.Player) player, 7, world,
				// pos.getX(),
				// pos.getY(), pos.getZ());
				TF2weapons.network.sendTo(new TF2Message.GuiConfigMessage(
						((IEntityConfigurable) ent).writeConfig(new CompoundTag()), pos), (ServerPlayer) player);
			} else {
				BlockPos forwardpos = pos.relative(facing);
				for (EntityPickup pickup : world.getEntitiesOfClass(EntityPickup.class,
						new AABB(forwardpos))) {
					pickup.discard();
				}
			}
		} else {
			BlockEntity ent = world.getBlockEntity(pos);
			if (!(ent instanceof IEntityConfigurable)) {
				Screen.setClipboardString(pos.getX() + " " + pos.getY() + " " + pos.getZ());
				player.sendSystemMessage(
						Component.literal("Copied coordinates to clipboard: " + Screen.getClipboardString()));
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand handIn) {
		if (world.isClientSide) {
			HitResult ray = world.rayTraceBlocks(player.getEyePosition(1f),
					player.getEyePosition(1f).add(player.getLookAngle().scale(256)), false);
			if (ray != null && ((BlockHitResult) ray).getBlockPos() != null) {
				BlockPos pos = ((BlockHitResult) ray).getBlockPos();
				BlockEntity ent = world.getBlockEntity(pos);
				if (!(ent instanceof IEntityConfigurable)) {
					Screen.setClipboardString(pos.getX() + " " + pos.getY() + " " + pos.getZ());
					player.sendSystemMessage(Component.literal(
							"Copied coordinates to clipboard: " + Screen.getClipboardString()));
				}
			}
		}
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(handIn));
	}
}
