package stipix.enchanting_decisions;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.*;

public class EnchantabilityCosts {
    public static final EnchantabilityCost PROTECTION = new EnchantabilityCost(Enchantments.PROTECTION, new int[]{2, 5, 8, 10});
    public static final EnchantabilityCost FIRE_PROTECTION = new EnchantabilityCost(Enchantments.FIRE_PROTECTION, new int[]{2, 4, 6, 8});
    public static final EnchantabilityCost FEATHER_FALLING = new EnchantabilityCost(Enchantments.FEATHER_FALLING, new int[]{2, 4, 6, 8});
    public static final EnchantabilityCost BLAST_PROTECTION = new EnchantabilityCost(Enchantments.BLAST_PROTECTION, new int[]{2, 4, 6, 8});
    public static final EnchantabilityCost PROJECTILE_PROTECTION = new EnchantabilityCost(Enchantments.PROJECTILE_PROTECTION, new int[]{2, 4, 6, 8});
    public static final EnchantabilityCost RESPIRATION = new EnchantabilityCost(Enchantments.RESPIRATION, new int[]{2, 5, 8});
    public static final EnchantabilityCost AQUA_AFFINITY = new EnchantabilityCost(Enchantments.AQUA_AFFINITY, new int[]{8});
    public static final EnchantabilityCost THORNS = new EnchantabilityCost(Enchantments.THORNS, new int[]{2,5,8});
    public static final EnchantabilityCost DEPTH_STRIDER = new EnchantabilityCost(Enchantments.DEPTH_STRIDER, new int[]{2,5,8});
    public static final EnchantabilityCost FROST_WALKER = new EnchantabilityCost(Enchantments.FROST_WALKER, new int[]{2,5,8});
    public static final EnchantabilityCost BINDING_CURSE = new EnchantabilityCost(Enchantments.BINDING_CURSE, new int[]{-10});
    public static final EnchantabilityCost SOUL_SPEED = new EnchantabilityCost(Enchantments.SOUL_SPEED, new int[]{2, 4, 6});
    public static final EnchantabilityCost SWIFT_SNEAK = new EnchantabilityCost(Enchantments.SWIFT_SNEAK, new int[]{2, 4, 6});
    public static final EnchantabilityCost SHARPNESS = new EnchantabilityCost(Enchantments.SHARPNESS, new int[]{2, 4, 6, 8, 10});
    public static final EnchantabilityCost SMITE = new EnchantabilityCost(Enchantments.SMITE, new int[]{2, 3, 5, 7, 8});
    public static final EnchantabilityCost BANE_OF_ARTHROPODS = new EnchantabilityCost(Enchantments.BANE_OF_ARTHROPODS, new int[]{2, 5, 8});
    public static final EnchantabilityCost KNOCKBACK = new EnchantabilityCost(Enchantments.KNOCKBACK, new int[]{4, 8});
    public static final EnchantabilityCost FIRE_ASPECT = new EnchantabilityCost(Enchantments.FIRE_ASPECT, new int[]{4, 8});
    public static final EnchantabilityCost LOOTING = new EnchantabilityCost(Enchantments.LOOTING, new int[]{3, 6, 10});
    public static final EnchantabilityCost SWEEPING_EDGE = new EnchantabilityCost(Enchantments.SWEEPING_EDGE, new int[]{2, 4, 6});
    public static final EnchantabilityCost EFFICIENCY = new EnchantabilityCost(Enchantments.EFFICIENCY, new int[]{2, 4, 6, 8, 10});
    public static final EnchantabilityCost SILK_TOUCH = new EnchantabilityCost(Enchantments.SILK_TOUCH, new int[]{6});
    public static final EnchantabilityCost UNBREAKING = new EnchantabilityCost(Enchantments.UNBREAKING, new int[]{3, 6, 10});
    public static final EnchantabilityCost FORTUNE = new EnchantabilityCost(Enchantments.FORTUNE, new int[]{3, 6, 10});
    public static final EnchantabilityCost POWER = new EnchantabilityCost(Enchantments.POWER, new int[]{3, 6, 10});
    public static final EnchantabilityCost PUNCH = new EnchantabilityCost(Enchantments.PUNCH, new int[]{4, 8});
    public static final EnchantabilityCost FLAME = new EnchantabilityCost(Enchantments.FLAME, new int[]{4, 8});
    public static final EnchantabilityCost INFINITY = new EnchantabilityCost(Enchantments.INFINITY, new int[]{10});
    public static final EnchantabilityCost LUCK_OF_THE_SEA = new EnchantabilityCost(Enchantments.LUCK_OF_THE_SEA, new int[]{2, 5, 8});
    public static final EnchantabilityCost LURE = new EnchantabilityCost(Enchantments.LURE, new int[]{2, 5, 8});
    public static final EnchantabilityCost LOYALTY = new EnchantabilityCost(Enchantments.LOYALTY, new int[]{3, 6, 10});
    public static final EnchantabilityCost IMPALING = new EnchantabilityCost(Enchantments.IMPALING, new int[]{2, 3, 5, 7, 8});
    public static final EnchantabilityCost RIPTIDE = new EnchantabilityCost(Enchantments.RIPTIDE, new int[]{5, 10});
    public static final EnchantabilityCost CHANNELING = new EnchantabilityCost(Enchantments.CHANNELING, new int[]{8});
    public static final EnchantabilityCost MULTISHOT = new EnchantabilityCost(Enchantments.MULTISHOT, new int[]{6});
    public static final EnchantabilityCost QUICK_CHARGE = new EnchantabilityCost(Enchantments.QUICK_CHARGE, new int[]{4, 8});
    public static final EnchantabilityCost PIERCING = new EnchantabilityCost(Enchantments.PIERCING, new int[]{2, 4, 6, 8});
    public static final EnchantabilityCost DENSITY = new EnchantabilityCost(Enchantments.DENSITY, new int[]{2, 4, 6, 8, 10});
    public static final EnchantabilityCost BREACH = new EnchantabilityCost(Enchantments.BREACH, new int[]{2, 5, 8, 10});
    public static final EnchantabilityCost WIND_BURST = new EnchantabilityCost(Enchantments.WIND_BURST, new int[]{3, 6, 10});
    public static final EnchantabilityCost MENDING = new EnchantabilityCost(Enchantments.MENDING, new int[]{4, 8});
    public static final EnchantabilityCost VANISHING_CURSE = new EnchantabilityCost(Enchantments.VANISHING_CURSE, new int[]{-10});



    private static final Map<RegistryKey<Enchantment>, EnchantabilityCost> enchantabilityCosts = new HashMap<>();


    public EnchantabilityCosts(){
        register(PROTECTION);
        register(FIRE_PROTECTION);
        register(FEATHER_FALLING);
        register(BLAST_PROTECTION);
        register(PROJECTILE_PROTECTION);
        register(RESPIRATION);
        register(AQUA_AFFINITY);
        register(THORNS);
        register(DEPTH_STRIDER);
        register(FROST_WALKER);
        register(BINDING_CURSE);
        register(SOUL_SPEED);
        register(SWIFT_SNEAK);
        register(SHARPNESS);
        register(SMITE);
        register(BANE_OF_ARTHROPODS);
        register(KNOCKBACK);
        register(FIRE_ASPECT);
        register(LOOTING);
        register(SWEEPING_EDGE);
        register(EFFICIENCY);
        register(SILK_TOUCH);
        register(UNBREAKING);
        register(FORTUNE);
        register(POWER);
        register(PUNCH);
        register(FLAME);
        register(INFINITY);
        register(LUCK_OF_THE_SEA);
        register(LURE);
        register(LOYALTY);
        register(IMPALING);
        register(RIPTIDE);
        register(CHANNELING);
        register(MULTISHOT);
        register(QUICK_CHARGE);
        register(PIERCING);
        register(DENSITY);
        register(BREACH);
        register(WIND_BURST);
        register(MENDING);
        register(VANISHING_CURSE);


    }
    public void register(EnchantabilityCost newEnchantability){
        enchantabilityCosts.put(newEnchantability.enchantment(), newEnchantability);
    }

    static public Optional<EnchantabilityCost> getEnchantabilityCost(RegistryKey<Enchantment> enchantment){
        Optional<EnchantabilityCost> optional = Optional.empty();
        optional = Optional.of(enchantabilityCosts.get(enchantment));
        return optional;
    }

    static public int getEnchantabilityUsed(ItemStack itemStack){
        int used = 0;
        for(RegistryEntry<Enchantment> enchantment : itemStack.getEnchantments().getEnchantments()){
            int level = itemStack.getEnchantments().getLevel(enchantment);

            Optional<RegistryKey<Enchantment>> keyOptional = enchantment.getKey();
            if (keyOptional.isEmpty()) {
                continue;
            }
            Optional<EnchantabilityCost> cost = EnchantabilityCosts.getEnchantabilityCost(keyOptional.get());
            if (cost.isEmpty()) {
                //so that non-registered mods have a defined enchantability
                used += level * 2;
                continue;
            }
            if( level <= cost.get().levelValues().length && level > 0){
                used += cost.get().levelValues()[level - 1];
            } else if (level >cost.get().levelValues().length ){
                //in cases of enchantment above the max enchantment
                used += 15;
            }


        }
        return used;
    }
}
