package stipix.enchanting_decisions.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Optional;

@Mixin(LootTable.class)
public class LootTableMixin {


    @ModifyArg(method = "supplyInventory",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Inventory;setStack(ILnet/minecraft/item/ItemStack;)V"),
            index = 1
    )
    private ItemStack injected(ItemStack itemStack) {
        ItemStack itemStack2 = itemStack.copy();

        EnchantmentHelper.apply(
                itemStack2, components -> components.remove(enchantment -> !enchantment.isIn(EnchantmentTags.CURSE)));
        if(itemStack.isOf(Items.ENCHANTED_BOOK)){

            Object enchant = itemStack.getOrDefault(DataComponentTypes.STORED_ENCHANTMENTS, 0);
            if(enchant instanceof ItemEnchantmentsComponent) {
                for (RegistryEntry<Enchantment> enchantmentRegistry : ((ItemEnchantmentsComponent) enchant).getEnchantments()) {
                    if(!enchantmentRegistry.isIn(EnchantmentTags.CURSE)) {
                        itemStack2.addEnchantment(enchantmentRegistry, 1);
                    }
                }
            }


        }
        return itemStack2;
    }
}
