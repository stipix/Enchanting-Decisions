package stipix.enchanting_decisions;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class ModScreenHandlers {
    public static final ScreenHandlerType<CustomEnchantmentScreenHandler> CUSTOM_ENCHANTMENT_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, Identifier.of(EnchantingDecisions.MOD_ID, "custom_enchanting_table_block"), new ScreenHandlerType<>(CustomEnchantmentScreenHandler::new, FeatureSet.empty()));


    public static void registerScreenHandlers() {
        EnchantingDecisions.LOGGER.info("Registering Screen Handlers for " + EnchantingDecisions.MOD_ID);
    }
}
