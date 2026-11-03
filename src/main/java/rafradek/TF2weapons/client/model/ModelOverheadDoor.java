package rafradek.TF2weapons.client.model;

import rafradek.TF2weapons.client.compat.ModelBase;
import rafradek.TF2weapons.client.compat.ModelRenderer;


public class ModelOverheadDoor extends ModelBase {

	public ModelRenderer roller;
	public ModelRenderer door;
	public ModelOverheadDoor() {
		roller = new ModelRenderer(this, 0, 0).setTextureSize(32, 16);
		door = new ModelRenderer(this, 16, 0).setTextureSize(32, 16);
	}
}
