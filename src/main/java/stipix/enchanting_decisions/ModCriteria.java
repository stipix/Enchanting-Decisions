package stipix.enchanting_decisions;

import net.minecraft.advancement.criterion.Criteria;

public class ModCriteria {
    public static final GrindDownCriterion GRIND_DOWN_CRITERION = Criteria.register(EnchantingDecisions.MOD_ID + ":grind_down", new GrindDownCriterion());
    public static final PowerUpCriterion POWER_UP_CRITERION = Criteria.register(EnchantingDecisions.MOD_ID + ":power_up", new PowerUpCriterion());
    public static void init(){}
}