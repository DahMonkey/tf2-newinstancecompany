package rafradek.TF2weapons.client;

import com.google.common.collect.Maps;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.item.ItemWearable;
import rafradek.TF2weapons.util.TF2Class;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PyrolandRenderer {

    private static final ResourceLocation PYROLAND_SHADER = new ResourceLocation("rafradek_tf2_weapons", "shaders/post/pyrovision.json");

    public static PyrolandRenderer INSTANCE = new PyrolandRenderer();

    private final Map<ResourceLocation, ResourceLocation> SOUND_MAP = Maps.newHashMap();
    private final Map<ModelResourceLocation, ModelResourceLocation> MODEL_MAP = Maps.newHashMap();

    private PyrolandRenderer() {
        for (TF2Class clazz : TF2Class.getClasses()) {
            String name = clazz.getName();
            registerSoundReplacement(new ResourceLocation(TF2weapons.MOD_ID, "mob."+ name +".hurt"),
                    new ResourceLocation(TF2weapons.MOD_ID, "mob."+ name +".laughshort"));
            registerSoundReplacement(new ResourceLocation(TF2weapons.MOD_ID, "mob."+ name +".death"),
                    new ResourceLocation(TF2weapons.MOD_ID, "mob."+ name +".laughhappy"));
        }
    }

    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (shouldRenderPyrovision()) if (!isShaderLoaded()) enableShader();
        else if (isShaderLoaded()) disableShader();
    }

    @SubscribeEvent
    public void playSound(PlaySoundEvent event) {
        SoundInstance sound = event.getSound();
        SoundSource cat = sound.getCategory();
        if (!(cat == SoundSource.HOSTILE || cat == SoundSource.PLAYERS || cat == SoundSource.NEUTRAL)) return;
        if (sound instanceof AbstractSoundInstance && shouldRenderPyrovision()) {
            AbstractSoundInstance positionedsound = (AbstractSoundInstance) sound;
            positionedsound.pitch = ((AbstractSoundInstance)sound).pitch * 1.5f;
            if (SOUND_MAP.containsKey(sound.getSoundLocation()))
                positionedsound.positionedSoundLocation = SOUND_MAP.get(sound.getSoundLocation());
        }
    }

    public boolean shouldRenderPyrovision() {
        Minecraft mc = Minecraft.getInstance();
        Entity entity = mc.getCameraEntity();
        if (entity instanceof LivingEntity) return shouldRenderPyrovision((LivingEntity) entity);
        return shouldRenderPyrovision(mc.player);
    }

    private boolean shouldRenderPyrovision(LivingEntity entity) {
        if (entity == null) return false;
        for (InteractionHand hand : InteractionHand.values()) if (shouldRenderPyrovision(entity.getItemInHand(hand), entity, false)) return true;
        if (shouldRenderPyrovision(entity.getItemBySlot(EquipmentSlot.HEAD), entity, true)) return true;
        if (!entity.getCapability(TF2weapons.INVENTORY_CAP).isPresent()) return false;
        for (int i = 0; i < 2; i++) if (shouldRenderPyrovision(entity.getCapability(TF2weapons.INVENTORY_CAP).orElse(null)
                .getStackInSlot(i), entity, true)) return true;
        return false;
    }

    private boolean shouldRenderPyrovision(ItemStack stack, LivingEntity entity, boolean isWorn) {
        if (stack == null) return false;
        return (!(stack.getItem() instanceof ItemWearable) || isWorn)
                && TF2Attribute.getModifier("Pyrovision", stack, 0, entity) > 0;
    }

    private boolean isShaderLoaded() {
        PostChain shader = Minecraft.getInstance().entityRenderer.getShaderGroup();
        return shader != null && PYROLAND_SHADER.toString().equals(shader.getShaderGroupName());
    }

    private void enableShader() {
        Minecraft.getInstance().entityRenderer.loadShader(PYROLAND_SHADER);
    }

    private void disableShader() {
        Minecraft mc = Minecraft.getInstance();
        mc.entityRenderer.loadEntityShader(mc.getCameraEntity());
    }

    public void registerSoundReplacement(ResourceLocation sound, ResourceLocation replacement) {
        SOUND_MAP.put(sound, replacement);
    }

    public void registerModelReplacement(ModelResourceLocation model, ModelResourceLocation replacement) {
        MODEL_MAP.put(model, replacement);
    }

    public boolean hasReplacementModel(ModelResourceLocation loc) {
        return MODEL_MAP.containsKey(loc);
    }

    public ModelResourceLocation getReplacementModel(ModelResourceLocation loc) {
        return MODEL_MAP.get(loc);
    }

}
