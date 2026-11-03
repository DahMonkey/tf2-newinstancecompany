package rafradek.TF2weapons.mixin;

import rafradek.TF2weapons.client.PyrolandRenderer;
import rafradek.TF2weapons.item.ItemFromData;
import java.util.Map;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemModelMesher.class)
public abstract class MixinItemModelMesher {

    @Shadow @Final private ModelManager modelManager;

    @Shadow @Final protected Map<Item, ItemMeshDefinition> shapers;

    @Inject(at=@At("TAIL"), method = "getItemModel(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/client/renderer/block/model/IBakedModel;", cancellable = true)
    private void getItemModel(ItemStack stack, CallbackInfoReturnable<BakedModel> callback) {
        if (!PyrolandRenderer.INSTANCE.shouldRenderPyrovision()) return;
        ItemMeshDefinition mesh;
        if (stack.getItem() instanceof ItemFromData) mesh = ((ItemFromData) stack.getItem()).getMeshDefinition();
        else mesh = shapers.get(stack.getItem());
        if (mesh == null) return;
        ModelResourceLocation loc = mesh.getModelLocation(stack);
        if (PyrolandRenderer.INSTANCE.hasReplacementModel(loc)) {
            callback.setReturnValue(modelManager.getModel(PyrolandRenderer.INSTANCE.getReplacementModel(loc)));
        }
    }

}
