package stipix.enchanting_decisions;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stipix.enchanting_decisions.enchantments.EnchantmentRegistry;

/*TODO: Replace villager behavior
        Edit Loot tables

 */

public class EnchantingDecisions implements ModInitializer {
	public static final String MOD_ID = "enchanting-decisions";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

//    public static final ScreenHandlerType<CustomEnchantmentScreenHandler> CUSTOM_ENCHANTMENT_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER, Identifier.of(MOD_ID, "custom_enchanting_table_block"), new ScreenHandlerType<>(CustomEnchantmentScreenHandler::new, FeatureSet.empty()));


    @Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
        ModComponents.onInitialize();
        ModScreenHandlers.registerScreenHandlers();
        ModCriteria.init();
        EnchantmentRegistry.registerModEnchantments();
        EnchantabilityCosts costs = new EnchantabilityCosts();
        MiscToolEnchantabilities miscToolEnchantabilities = new MiscToolEnchantabilities();
		LOGGER.info("Enchanting decisions initialized!");
    }
}