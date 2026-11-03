package rafradek.TF2weapons.client.compat;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import rafradek.TF2weapons.client.ItemMeshDefinition;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 1.12 ModelLoader item-model API. 1.19 has one model per item; items registered here get a wrapper
 * model whose overrides pick the model the 1.12 code asked for (by metadata or mesh definition).
 * Model locations "ns:name#inventory" load models/item/name.json.
 */
public final class ModelLoader {
	private static final Map<Item, Int2ObjectOpenHashMap<ModelResourceLocation>> META_MODELS = new HashMap<>();
	private static final Map<Item, ItemMeshDefinition> MESHES = new HashMap<>();
	private static final Set<ResourceLocation> EXTRA = new HashSet<>();

	public static void setCustomModelResourceLocation(Item item, int meta, ModelResourceLocation model) {
		META_MODELS.computeIfAbsent(item, i -> new Int2ObjectOpenHashMap<>()).put(meta, model);
		EXTRA.add(toModelPath(model));
	}

	public static void setCustomMeshDefinition(Item item, ItemMeshDefinition mesh) {
		MESHES.put(item, mesh);
	}

	public static void registerItemVariants(Item item, ResourceLocation... models) {
		for (ResourceLocation model : models)
			EXTRA.add(toModelPath(model));
	}

	/** "ns:name#inventory" -> "ns:item/name" (a plain model file location). */
	public static ResourceLocation toModelPath(ResourceLocation model) {
		String path = model.getPath();
		if (path.startsWith("item/") || path.startsWith("block/") || path.startsWith("builtin/"))
			return new ResourceLocation(model.getNamespace(), path);
		return new ResourceLocation(model.getNamespace(), "item/" + path);
	}

	public static void onRegisterAdditional(ModelEvent.RegisterAdditional event) {
		for (ResourceLocation model : EXTRA)
			event.register(model);
	}

	public static void onBakingCompleted(ModelEvent.BakingCompleted event) {
		Map<ResourceLocation, BakedModel> models = event.getModels();
		Set<Item> items = new HashSet<>(META_MODELS.keySet());
		items.addAll(MESHES.keySet());
		for (Item item : items) {
			ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
			if (key == null)
				continue;
			ModelResourceLocation location = new ModelResourceLocation(key, "inventory");
			BakedModel base = models.get(location);
			if (base == null)
				base = event.getModelManager().getMissingModel();
			models.put(location, new SelectingModel(base, item));
		}
	}

	@Nullable
	static ModelResourceLocation select(Item item, ItemStack stack) {
		ItemMeshDefinition mesh = MESHES.get(item);
		if (mesh != null) {
			ModelResourceLocation chosen = mesh.getModelLocation(stack);
			if (chosen != null)
				return chosen;
		}
		Int2ObjectOpenHashMap<ModelResourceLocation> metas = META_MODELS.get(item);
		if (metas != null) {
			ModelResourceLocation chosen = metas.get(stack.getDamageValue());
			return chosen != null ? chosen : metas.get(0);
		}
		return null;
	}

	private static class SelectingModel extends BakedModelWrapper<BakedModel> {
		private final ItemOverrides overrides;

		SelectingModel(BakedModel original, Item item) {
			super(original);
			this.overrides = new ItemOverrides() {
				@Nullable
				@Override
				public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level,
						@Nullable LivingEntity entity, int seed) {
					ModelResourceLocation chosen = select(item, stack);
					BakedModel target = original;
					if (chosen != null) {
						BakedModel found = Minecraft.getInstance().getModelManager().getModel(toModelPath(chosen));
						if (found != null && found != Minecraft.getInstance().getModelManager().getMissingModel())
							target = found;
					}
					BakedModel resolved = target.getOverrides().resolve(target, stack, level, entity, seed);
					return resolved != null ? resolved : target;
				}
			};
		}

		@Override
		public ItemOverrides getOverrides() {
			return this.overrides;
		}
	}
}
