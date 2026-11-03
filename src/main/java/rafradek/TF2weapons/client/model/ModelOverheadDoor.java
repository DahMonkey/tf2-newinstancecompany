package rafradek.TF2weapons.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;

public class ModelOverheadDoor extends EntityModel {

	public ModelPart roller;
	public ModelPart door;
	public ModelOverheadDoor() {
		roller = new ModelPart(this, 0, 0).setTextureSize(32, 16);
		door = new ModelPart(this, 16, 0).setTextureSize(32, 16);
	}
}
