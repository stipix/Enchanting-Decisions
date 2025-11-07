package stipix.enchanting_decisions;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MiscToolEnchantabilities {
    public static final MiscToolEnchantabilityStat BOW = new MiscToolEnchantabilityStat(Items.BOW, 28);
    public static final MiscToolEnchantabilityStat CROSSBOW = new MiscToolEnchantabilityStat(Items.CROSSBOW, 28);
    public static final MiscToolEnchantabilityStat ELYTRA = new MiscToolEnchantabilityStat(Items.ELYTRA, 20);
    public static final MiscToolEnchantabilityStat FISHING_ROD = new MiscToolEnchantabilityStat(Items.FISHING_ROD, 24);
    public static final MiscToolEnchantabilityStat FLINT_AND_STEEL = new MiscToolEnchantabilityStat(Items.FLINT_AND_STEEL, 16);
    public static final MiscToolEnchantabilityStat MACE = new MiscToolEnchantabilityStat(Items.MACE, 28);
    public static final MiscToolEnchantabilityStat SHEARS = new MiscToolEnchantabilityStat(Items.SHEARS, 16);
    public static final MiscToolEnchantabilityStat SHIELD = new MiscToolEnchantabilityStat(Items.SHIELD, 24);
    public static final MiscToolEnchantabilityStat TRIDENT = new MiscToolEnchantabilityStat(Items.TRIDENT, 28);

    private static final Map<Item, Integer> enchantabilities = new HashMap<>();

    public MiscToolEnchantabilities(){
        register(BOW);
        register(CROSSBOW);
        register(ELYTRA);
        register(FISHING_ROD);
        register(FLINT_AND_STEEL);
        register(MACE);
        register(SHEARS);
        register(SHIELD);
        register(TRIDENT);
    }
    public void register(MiscToolEnchantabilityStat newEnchantability){
        enchantabilities.put(newEnchantability.item(), newEnchantability.enchantability());
    }

    static public Optional<Integer> getEnchantability(Item item){
        return Optional.of(enchantabilities.get(item));
    }

}
