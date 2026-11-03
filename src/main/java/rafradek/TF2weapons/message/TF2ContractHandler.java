package rafradek.TF2weapons.message;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.message.TF2Message.ContractMessage;
import rafradek.TF2weapons.util.Contract;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;

public class TF2ContractHandler implements IMessageHandler<TF2Message.ContractMessage, IMessage> {

	public static int size;

	@Override
	public IMessage onMessage(final ContractMessage message, MessageContext ctx) {

		if (rafradek.TF2weapons.client.ClientProxy.getLocalPlayer() == null)
			return null;
		ArrayList<Contract> contracts = rafradek.TF2weapons.client.ClientProxy.getLocalPlayer().getCapability(TF2weapons.PLAYER_CAP).orElse(null).contracts;
		if (message.id == -1) {
			contracts.add(message.contract);
			rafradek.TF2weapons.client.ClientProxy.getLocalPlayer().getCapability(TF2weapons.PLAYER_CAP).orElse(null).newContracts = true;
		} else if (contracts.size() <= message.id) {
			contracts.add(message.id, message.contract);
			if (message.contract.rewards > 0) {
				rafradek.TF2weapons.client.ClientProxy.getLocalPlayer().getCapability(TF2weapons.PLAYER_CAP).orElse(null).newRewards = true;
			}
		} else {
			Contract prev = contracts.set(message.id, message.contract);
			if (prev.rewards == 0 && message.contract.rewards > 0) {
				rafradek.TF2weapons.client.ClientProxy.getLocalPlayer().getCapability(TF2weapons.PLAYER_CAP).orElse(null).newRewards = true;
			}
		}
		/*
		 * Minecraft.getMinecraft().addScheduledTask(new Runnable(){
		 * 
		 * @Override public void run() { //System.out.println("Wep drop "+message.name);
		 * ItemStack stack=ItemFromData.getNewStack(message);
		 * ((ItemUsable)stack.getItem()).holster(Minecraft.getMinecraft().player.
		 * getCapability(TF2weapons.WEAPONS_CAP).orElse(null), stack,
		 * Minecraft.getMinecraft().player, Minecraft.getMinecraft().world);
		 * 
		 * }
		 * 
		 * });
		 */

		return null;
	}

}
