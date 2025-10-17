package stipix.enchanting_decisions;

import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;


public class ModComponents {
    public static final ComponentType<Boolean> PLAYER_ENCHANTED = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(EnchantingDecisions.MOD_ID, "player_enchanted"),
            ComponentType.<Boolean>builder().codec(Codec.BOOL).build()
    );

    protected static void onInitialize(){
        EnchantingDecisions.LOGGER.info("Registering {} Components", EnchantingDecisions.MOD_ID);
    }
}
