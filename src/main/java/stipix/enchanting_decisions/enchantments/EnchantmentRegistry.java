package stipix.enchanting_decisions.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import stipix.enchanting_decisions.EnchantingDecisions;


public class EnchantmentRegistry {
    public static final RegistryKey<Enchantment> CLOUD_HOPPER = of("enchantment.enchanting-decisions.cloud_hopper");
    //public static MapCodec<LightningEnchantmentEffect> LIGHTNING_EFFECT = register("lightning_effect", LightningEnchantmentEffect.CODEC);
    public static final RegistryKey<Enchantment> WRATH = of("wrath");


    private static RegistryKey<Enchantment> of(String path) {
        Identifier id = Identifier.of(EnchantingDecisions.MOD_ID, path);
        return RegistryKey.of(RegistryKeys.ENCHANTMENT, id);
    }

    //private static <T extends EnchantmentEntityEffect> MapCodec<T> register(String id, MapCodec<T> codec) {
    //    return Registry.register(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, Identifier.of(ExampleMod.MOD_ID, id), codec);
    //}

    public static void registerModEnchantments() {
        EnchantingDecisions.LOGGER.info("Registering EnchantmentEffects for" + EnchantingDecisions.MOD_ID);
    }

}
