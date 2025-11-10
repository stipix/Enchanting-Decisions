package stipix.enchanting_decisions.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
    @Inject(method = "generateEnchantments(Lnet/minecraft/util/math/random/Random;Lnet/minecraft/item/ItemStack;ILjava/util/stream/Stream;)Ljava/util/List;", at = @At("RETURN"))
    private static void generateEnchantmentsFixBooks(
            Random random,
            ItemStack stack,
            int level,
            Stream<RegistryEntry<Enchantment>> possibleEnchantments,
            CallbackInfoReturnable<List<EnchantmentLevelEntry>> cir,
            @Local List<EnchantmentLevelEntry> list)
    {
        if(stack.isOf(Items.ENCHANTED_BOOK) || stack.isOf(Items.BOOK)) {
            list.replaceAll(enchantmentLevelEntry -> new EnchantmentLevelEntry(enchantmentLevelEntry.enchantment(), 1));
        }
        List<Text> lines = new java.util.ArrayList<>(Objects.requireNonNull(stack.getComponents().get(DataComponentTypes.LORE)).lines());
        lines.add(Text.translatable("enchanting-decisions.natural_enchantment1"));
        lines.add(Text.translatable("enchanting-decisions.natural_enchantment2"));
        LoreComponent newLore = new LoreComponent(lines);
        stack.set(DataComponentTypes.LORE, newLore);

    }
}
