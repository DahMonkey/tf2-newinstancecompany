package rafradek.TF2weapons.client.gui;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.tileentity.IEntityConfigurable;
import rafradek.TF2weapons.util.TF2Class;
import java.util.ArrayList;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

public class GuiConfigurable2 extends GuiConfig {

	public CompoundTag tag;
	public ConfigCategory category;
	public ConfigCategory outcategory;
	public BlockPos pos;
	public static ConfigCategory copyConfig;
	private Button pasteButton;

	public GuiConfigurable2(Screen parentScreen, ArrayList<IConfigElement> elements, ConfigCategory category,
			ConfigCategory outcategory, BlockPos pos) {
		super(parentScreen, elements, "", false, false, "TF2 Stuff Configuration");
		this.category = category;
		this.pos = pos;
		this.outcategory = outcategory;
	}

	public static GuiConfigurable2 create(CompoundTag tag, BlockPos pos) {
		IEntityConfigurable entity = (IEntityConfigurable) Minecraft.getInstance().level.getBlockEntity(pos);
		ArrayList<IConfigElement> configElements = new ArrayList<>();
		ConfigCategory category = new ConfigCategory("root");
		for (String key : tag.getAllKeys()) {
			if (!key.equals("Outputs")) {
				Property prop = null;
				switch (tag.getTagId(key)) {
				case 8:
					prop = addString(category, key, (StringTag) tag.get(key));
					break;
				case 1:
					prop = addByte(category, key, (NBTTagByte) tag.get(key));
					break;
				case 3:
					prop = addPrimitive(category, key, (IntTag) tag.get(key));
					break;
				case 5:
					prop = addPrimitive(category, key, (FloatTag) tag.get(key));
					break;
				case 6:
					prop = addPrimitive(category, key, (NBTTagDouble) tag.get(key));
					break;
				case 7:
					prop = addPrimitive(category, key, (NBTTagDouble) tag.get(key));
					break;
				case 11:
					prop = addIntArray(category, key, (IntArrayTag) tag.get(key));
					break;
				}
				if (prop != null) {
					String[] allowed = entity.getAllowedValues(key);
					if (allowed != null) prop.setValidValues(allowed);
					category.put(key, prop);
				}
			}
		}

		ConfigCategory outputcat = new ConfigCategory("Outputs");
		CompoundTag outputtag = tag.getCompound("Outputs");
		for (String outname : entity.getOutputs()) {
			Property outprop = new Property(outname, new String[0], Type.STRING);
			ListTag outlist = outputtag.getList(outname, 11);
			String[] outvalues = new String[outlist.size()];
			for (int i = 0; i < outlist.size(); i++) {
				int[] coord = ((IntArrayTag) outlist.get(i)).getIntArray();
				outvalues[i] = coord[0] + " " + coord[1] + " " + coord[2] + " " + coord[3];
			}
			outprop.set(outvalues);
			outputcat.put(outname, outprop);
		}
		configElements.add(new ConfigElement(outputcat));
		configElements.addAll(new ConfigElement(category).getChildElements());
		return new GuiConfigurable2(null, configElements, category, outputcat, pos);
	}

	@Override
	public void initGui() {
		super.initGui();
		pasteButton = new Button(2501, width - 60, 0, 50, 20, "Paste");
		buttonList.add(new Button(2500, 10, 0, 50, 20, "Copy"));
		buttonList.add(pasteButton);
		if (copyConfig == null) pasteButton.enabled = false;
	}

	public static Property addString(ConfigCategory cat, String name, StringTag tag) {
		Property prop = new Property(name, tag.getString(), Type.STRING);
		if (name.startsWith("T:")) {
			Scoreboard scoreboard = Minecraft.getInstance().level.getScoreboard();
			String[] teams = new String[scoreboard.getTeams().size() + 1];
			teams[0] = "";
			int i = 1;
			for (PlayerTeam team : scoreboard.getTeams()) {
				teams[i] = team.getName();
				i++;
			}
			prop.setValidValues(teams);
		} else if (name.startsWith("C:")) prop.setValidValues(TF2Class.getClassNames());
		return prop;
	}

	public static Property addIntArray(ConfigCategory cat, String name, IntArrayTag tag) {
		if (name.startsWith("L:")) {
			String[] values = new String[tag.getIntArray().length];
			for (int i = 0; i < values.length; i++) values[i] = String.valueOf(tag.getIntArray()[i]);
			Property prop = new Property(name, values, Type.INTEGER);
			return prop;
		} else {
			StringBuilder builder = new StringBuilder();
			int[] array = tag.getIntArray();
			for (int i = 0; i < array.length; i++) {
				builder.append(array[i]);
				if (i != array.length - 1) builder.append(' ');
			}
			Property prop = new Property(name, builder.toString(), Type.STRING);
			prop.setComment("position");
			return prop;
		}
	}

	public static Property addPrimitive(ConfigCategory cat, String name, NBTPrimitive tag) {
		return new Property(name, tag.toString(), (tag instanceof FloatTag || tag instanceof NBTTagDouble) ? Type.DOUBLE : Type.INTEGER);
	}

	public static Property addByte(ConfigCategory cat, String name, NBTTagByte tag) {
		return new Property(name, tag.getByte() != 0 ? "true" : "false", Type.BOOLEAN);
	}

	private void copyCategory(ConfigCategory src, ConfigCategory dest) {
		for (Entry<String, Property> entry : src.entrySet()) if (dest.containsKey(entry.getKey())) dest.put(entry.getKey(), entry.getValue());
		for (ConfigCategory config : src.getChildren()) for (ConfigCategory config2 : dest.getChildren())
			if (config.getName().equals(config2.getName())) copyCategory(config, config2);
	}

	@Override
	protected void actionPerformed(Button button) {
		super.actionPerformed(button);
		if (button.id == 2000) sendChanges();
		else if (button.id == 2500) {
			copyConfig = new ConfigCategory(category.getName());
			copyConfig.putAll(category);
			pasteButton.enabled = true;
		} else if (button.id == 2501 && copyConfig != null) {
			copyCategory(copyConfig, category);
			sendChanges();
			mc.setScreen(parentScreen);
		}
	}

	public void sendChanges() {
		CompoundTag newtag = new CompoundTag();
		for (Entry<String, Property> entry : category.entrySet()) {
			Property prop = entry.getValue();
			if (prop.getType() == Type.STRING) {
				if (prop.getComment().equals("position")) {
					String[] propvalues = prop.getString().split(" ");
					int[] intarray = new int[propvalues.length];
					for (int i = 0; i < propvalues.length; i++) {
						intarray[i] = Integer.parseInt(propvalues[i]);
					}
					newtag.putIntArray(entry.getKey(), intarray);
				} else
					newtag.putString(entry.getKey(), prop.getString());
			}
			if (prop.getType() == Type.BOOLEAN) {
				newtag.putByte(entry.getKey(), (byte) (prop.getBoolean() ? 1 : 0));
			}
			if (prop.getType() == Type.INTEGER) {
				if (prop.isList()) {
					newtag.putIntArray(entry.getKey(), prop.getIntList());
				} else {
					newtag.putInt(entry.getKey(), prop.getInt());
				}
			}
		}
		CompoundTag outtag = new CompoundTag();
		for (Entry<String, Property> entry : outcategory.entrySet()) {
			Property prop = entry.getValue();
			ListTag list = new ListTag();
			for (String value : prop.getStringList()) {
				try {
					String[] decode = value.split(" ");
					int[] coord = new int[] { Integer.parseInt(decode[0]), Integer.parseInt(decode[1]),
							Integer.parseInt(decode[2]), 15 };
					if (decode.length >= 4)
						coord[3] = Integer.parseInt(decode[3]);
					list.add(new IntArrayTag(coord));
				} catch (Exception e) {
					TF2weapons.LOGGER.error("Invalid output target");
				}
			}
			outtag.put(entry.getKey(), list);
		}
		newtag.put("Outputs", outtag);
		TF2weapons.network.sendToServer(new TF2Message.GuiConfigMessage(newtag, pos));
	}
}
