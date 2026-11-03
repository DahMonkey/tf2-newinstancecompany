package rafradek.TF2weapons.mixin;

import org.apache.commons.lang3.ArrayUtils;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.loot.RandomWeaponItemFunction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

@Mixin(LootEntryItem.class)
public class MixinLootEntryItem {

    @Mutable
    @Shadow @Final protected LootItemFunction[] functions;

    @Inject(at=@At("TAIL"), method = "<init>")
    public void LootEntryItem(Item item, int weightIn, int qualityIn, LootItemFunction[] functionsIn, LootItemCondition[] conditionsIn, String entryName, CallbackInfo callback) {
        if (item == TF2weapons.itemTF2) functions = ArrayUtils.add(functions, new RandomWeaponItemFunction());
    }


}
