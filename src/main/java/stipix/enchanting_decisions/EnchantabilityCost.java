package stipix.enchanting_decisions;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;

public record EnchantabilityCost(RegistryKey<Enchantment> enchantment, int[] levelValues) {

}
