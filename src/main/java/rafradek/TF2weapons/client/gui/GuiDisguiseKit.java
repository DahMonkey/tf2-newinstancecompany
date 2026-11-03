package rafradek.TF2weapons.client.gui;

import rafradek.TF2weapons.client.compat.GuiScreen;
import rafradek.TF2weapons.client.compat.GuiButton;
import rafradek.TF2weapons.client.compat.GuiTextField;
import rafradek.TF2weapons.client.compat.LegacyInput;

import rafradek.TF2weapons.client.compat.GlStateManager;
import rafradek.TF2weapons.client.compat.OpenGlHelper;
import rafradek.TF2weapons.client.compat.RenderHelper;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import org.apache.commons.lang3.StringUtils;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.mercenary.EntitySpy;
import rafradek.TF2weapons.message.TF2Message;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraftforge.registries.ForgeRegistries;

public class GuiDisguiseKit extends GuiScreen {

	public GuiButton playerDisguise;
	private GuiTextField playerNameField;
	public Player player;
	public ArrayList<LivingEntity> mobList = new ArrayList<>();
	public boolean needUpdating;
	public int ticksUpdate;
	public int firstIndex;
	public float scroll;
	private boolean isScrolling;
	private boolean wasClicking;

	private static final ResourceLocation CRAFTING_TABLE_GUI_TEXTURES = new ResourceLocation(TF2weapons.MOD_ID, "textures/gui/container/cabinet.png");

	@Override
	public void initGui() {
		player = new RemotePlayer(this.minecraft.level, new GameProfile(mc.player.getUUID(), "name"), null);
		mobList.clear();
		for (net.minecraft.world.entity.EntityType<?> entry : ForgeRegistries.ENTITY_TYPES.getValues()) {
			Entity entity;
			try {
				entity = entry.create(mc.level);
			} catch (Exception e) {
				continue;
			}
			if (entity instanceof LivingEntity && ((entity.getBbWidth() + entity.getBbHeight() < 6 && entity.canChangeDimensions() && entity instanceof PathfinderMob)
							|| mc.player.getAbilities().instabuild)) {
				mobList.add((LivingEntity) entity);
				if (entity instanceof EntitySpy) entity.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).invisTicks = 0;
			}
		}
		Collections.sort(mobList, (o1, o2) -> net.minecraft.world.entity.EntityType.getKey(o2.getType()).toString().compareTo(net.minecraft.world.entity.EntityType.getKey(o1.getType()).toString()));
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		playerNameField = new GuiTextField(6, fontRenderer, width / 2 + 26, height / 2 + 60, 108, 19);
		playerNameField.setMaxStringLength(64);
		playerNameField.setFocused(true);
		buttonList.clear();
		for (int i = 0; i < 16; i++) buttonList.add(new GuiButton(i, this.width / 2 - 135 + (i % 2) * 70, this.height / 2 - 60 + i / 2 * 20,
					70, 20, net.minecraft.world.entity.EntityType.ZOMBIE.getDescription().getString()));
		buttonList.add(playerDisguise = new GuiButton(30, width / 2 + 25, height / 2 + 80, 110, 20, "Player"));
		setButtons();
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (!button.enabled) return;
		if (button.id == 30) TF2weapons.network.sendToServer(new TF2Message.DisguiseMessage("P:" + playerNameField.getText()));
		if (button.id < 16) TF2weapons.network.sendToServer(new TF2Message.DisguiseMessage("M:" + net.minecraft.world.entity.EntityType.getKey(mobList.get(button.id + firstIndex).getType()).toString()));
		mc.setScreen(null);
	}

	public void setButtons() {
		for (int i = 0; i < 16; i++) {
			if (i + firstIndex < mobList.size()) {
				buttonList.get(i).displayString = mobList.get(i + firstIndex).getName().getString();
				buttonList.get(i).visible = true;
			} else buttonList.get(i).visible = false;
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		playerNameField.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		playerNameField.textboxKeyTyped(typedChar, keyCode);
		if (playerNameField.isFocused()) needUpdating = true;

	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		drawCenteredString(fontRenderer, I18n.get("gui.disguise.info", new Object[0]), width / 2 - 5, 20, 16777215);
		/*
		 * for (int i = 0; i < this.mobs.length; i++) drawEntityOnScreen(this.width / 2
		 * - 105 + (i % 4) * 70, this.height / 2 - 26 + 110 * (i / 4), 35, mobs[i]);
		 * 
		 * drawEntityOnScreen(this.width / 2 + 105, this.height / 2 + 66, 35, player);
		 */
		int entdraw = -1;
		for (int i = 0; i < 16; i++) if (i + firstIndex < mobList.size() && buttonList.get(i).isMouseOver()) {
				entdraw = i + firstIndex;
				break;
			}
		if (entdraw == -1) drawEntityOnScreen(width / 2 + 75, height / 2 + 40, 35, player);
		else drawEntityOnScreen(width / 2 + 75, height / 2 + 40, 35, mobList.get(entdraw));
		boolean flag = LegacyInput.isMouseButtonDown(0);
		int i = width / 2;
		int j = height / 2;
		int k = i + 5;
		int l = j - 60;
		int i1 = k + 14;
		int j1 = l + 160;
		if (!wasClicking && flag && mouseX >= k && mouseY >= l && mouseX < i1 && mouseY < j1) isScrolling = true;
		if (!flag) isScrolling = false;
		wasClicking = flag;
		if (isScrolling) {
			scroll = (mouseY - l - 7.5F) / (j1 - l - 15.0F);
			scroll = Mth.clamp(scroll, 0.0F, 1.0F);
			int rows = -(-mobList.size() / 2) - 7;
			firstIndex = Math.round(scroll * rows) * 2;
			setButtons();
		}
		RenderSystem.setShaderTexture(0, CRAFTING_TABLE_GUI_TEXTURES);
		k = width / 2 + 6;
		l = height / 2 - 59;
		i = l + 160;
		drawTexturedModalRect(k, l + (int) ((i - l - 17) * scroll), 232, 0, 12, 15);
		super.drawScreen(mouseX, mouseY, partialTicks);
		playerNameField.drawTextBox();
	}

	public static void drawEntityOnScreen(int posX, int posY, int scale, LivingEntity ent) {
		net.minecraft.client.gui.screens.inventory.InventoryScreen.renderEntityInInventory(posX, posY, scale, 0, 0,
				ent);
	}

	@Override
	public void onGuiClosed() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
	}

	@Override
	public void updateScreen() {
		playerNameField.tick();
		if (--ticksUpdate < 0 && needUpdating && !StringUtils.isBlank(playerNameField.getText())) {
			needUpdating = false;
			ticksUpdate = 12;
			WeaponsCapability.THREAD_POOL.submit(() -> {
				SkullBlockEntity.updateGameprofile(new GameProfile(mc.player.getUUID(), playerNameField.getText()),
						profile -> mc.execute(() -> this.setDisguisePlayer(profile)));
			});
		}
	}

	private void setDisguisePlayer(GameProfile foundProfile) {
		GameProfile profile = foundProfile;
		{
			{
				Minecraft mc = this.minecraft;
				if (profile == null || profile.getId() == null)
					profile = new GameProfile(mc.player.getUUID(), playerNameField.getText());
				player = new RemotePlayer(mc.level, profile, null);
				WeaponsCapability.get(player).setDisguised(true);
				WeaponsCapability.get(player).setDisguiseType("P:" + playerNameField.getText());
				final WeaponsCapability cap = player.getCapability(TF2weapons.WEAPONS_CAP).orElse(null);
				cap.skinType = DefaultPlayerSkin.getSkinModelName(player.getUUID());
				if (profile.getId() != null) cap.skinType = DefaultPlayerSkin.getSkinModelName(profile.getId());
				Minecraft.getInstance().getSkinManager().registerSkins(profile, (typeIn, location, profileTexture) -> {
					if (typeIn != Type.SKIN) return;
					cap.skinDisguise = location;
					cap.skinType = profileTexture.getMetadata("model");
					if (cap.skinType == null) cap.skinType = "default";
					}, false);
			}
		}
		// this.playerNameField.tick();
	}
}
