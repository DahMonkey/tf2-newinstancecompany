package rafradek.TF2weapons.mixin;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.PyrolandRenderer;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.WeaponData;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(RenderItem.class)
public abstract class MixinRenderItem {

    private static boolean silhouette;

    @Shadow protected abstract void renderItemModel(ItemStack stack, BakedModel bakedmodel, ItemTransforms.TransformType transforms, boolean leftHanded);

    @Shadow public abstract BakedModel getItemModelWithOverrides(ItemStack stack, @Nullable Level world, @Nullable LivingEntity entity);

    @Shadow public abstract void renderItem(ItemStack stack, LivingEntity entitylivingbaseIn, ItemTransforms.TransformType transform, boolean leftHanded);

    @Shadow public abstract void renderItem(ItemStack stack, ItemTransforms.TransformType cameraTransformType);

    @Shadow public abstract void renderItemIntoGUI(ItemStack stack, int x, int y);

    @Shadow protected abstract void renderModel(BakedModel model, int color, ItemStack stack);

    @Shadow public float zLevel;

    @Inject(at=@At("HEAD"), method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/client/renderer/block/model/ItemCameraTransforms$TransformType;Z)V", cancellable = true)
    private void renderItem(ItemStack stack, LivingEntity entity, ItemTransforms.TransformType transforms, boolean leftHanded, CallbackInfo callback) {
        if (transforms == ItemTransforms.TransformType.GUI && stack.getItem() == TF2weapons.itemTF2 && (stack.getDamageValue() == 9 || stack.getDamageValue() == 10)) {
            ItemStack weapon = ItemFromData.getDisplayWeapon(stack, Minecraft.getInstance().level.getDayTime());
            if (!weapon.isEmpty()) {
                silhouette = true;
                zLevel -= 50;
                renderItem(weapon, entity, transforms, leftHanded);
            }
        }
        renderItem(stack, entity, entity.level, transforms, leftHanded, callback);
    }

    @Inject(at=@At("HEAD"), method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemCameraTransforms$TransformType;)V", cancellable = true)
    private void renderItem(ItemStack stack, ItemTransforms.TransformType transforms, CallbackInfo callback) {
        if (transforms == ItemTransforms.TransformType.GUI && stack.getItem() == TF2weapons.itemTF2 && (stack.getDamageValue() == 9 || stack.getDamageValue() == 10)) {
            ItemStack weapon = ItemFromData.getDisplayWeapon(stack, Minecraft.getInstance().level.getDayTime());
            if (!weapon.isEmpty()) {
                silhouette = true;
                zLevel -= 50;
                renderItem(weapon, transforms);
            }
        }
        renderItem(stack, null, null, transforms, false, callback);
    }

    @Inject(at=@At("HEAD"), method = "renderItemModelIntoGUI", cancellable = true)
    private void renderItemModelIntoGUI(ItemStack stack, int x, int y, BakedModel bakedmodel, CallbackInfo callback) {
        if (stack.getItem() == TF2weapons.itemTF2 && (stack.getDamageValue() == 9 || stack.getDamageValue() == 10)) {
            ItemStack weapon = ItemFromData.getDisplayWeapon(stack, Minecraft.getInstance().level.getDayTime());
            if (!weapon.isEmpty()) {
                silhouette = true;
                zLevel -= 50;
                renderItemIntoGUI(weapon, x, y);
            }
        }
    }

    @Inject(at=@At("HEAD"), method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/IBakedModel;)V", cancellable = true)
    private void renderItem(ItemStack stack, BakedModel model, CallbackInfo callback) {
        if (silhouette) {
            callback.cancel();
            if (!stack.isEmpty())
            {
                GlStateManager.pushMatrix();
                GlStateManager.translate(-0.5F, -0.5F, -0.5F);
                renderModel(model,0xFF000000, stack);
                GlStateManager.popMatrix();
            }
            silhouette = false;
            zLevel += 50;
        }
    }

    private void renderItem(ItemStack stack, LivingEntity entity, Level world, ItemTransforms.TransformType transforms, boolean leftHanded, CallbackInfo callback) {
        if (!(stack.getItem() instanceof ItemFromData && stack.getCapability(TF2weapons.WEAPONS_DATA_CAP).isPresent())) return;
        if (stack.getCapability(TF2weapons.WEAPONS_DATA_CAP).orElse(null).getAttributeValue(stack, "Visible in Pyroland", 0) <= 0 ||
                PyrolandRenderer.INSTANCE.shouldRenderPyrovision() |! isThirdPerson(transforms)) return;
        callback.cancel();
        WeaponData data = ItemFromData.getData(stack);
        if (data == null |! data.hasProperty(PropertyType.BASED_ON)) return;
        String base = data.get(PropertyType.BASED_ON);
        if (base == null) return;
        ItemStack basestack = ItemFromData.getNewStack(base);
        if (basestack == null || basestack.isEmpty()) return;
        renderItemModel(basestack, getItemModelWithOverrides(basestack, world, entity), transforms, leftHanded);
    }

    private boolean isThirdPerson(ItemTransforms.TransformType transforms) {
        return transforms == ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND ||
                transforms == ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND ||
                transforms == ItemTransforms.TransformType.HEAD ||
                transforms == ItemTransforms.TransformType.GROUND;
    }

}
