package stipix.enchanting_decisions;

import com.mojang.serialization.Codec;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;

public class PowerUpCriterion extends AbstractCriterion<PowerUpCriterion.Conditions> {
    @Override
    public Codec<PowerUpCriterion.Conditions> getConditionsCodec() {
        return PowerUpCriterion.Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player) {
        trigger(player, PowerUpCriterion.Conditions::requirementsMet);
    }

    public record Conditions(Optional<LootContextPredicate> playerPredicate) implements AbstractCriterion.Conditions {
        public static Codec<PowerUpCriterion.Conditions> CODEC = LootContextPredicate.CODEC.optionalFieldOf("player")
                .xmap(PowerUpCriterion.Conditions::new, PowerUpCriterion.Conditions::player).codec();

        @Override
        public Optional<LootContextPredicate> player() {
            return playerPredicate;
        }
        public boolean requirementsMet() {
            return true; // AbstractCriterion#trigger helpfully checks the playerPredicate for us.
        }

    }
}
