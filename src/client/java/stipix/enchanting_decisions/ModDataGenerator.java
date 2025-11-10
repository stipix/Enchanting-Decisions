package stipix.enchanting_decisions;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancement.*;
import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.advancement.criterion.DefaultBlockUseCriterion;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.advancement.criterion.TickCriterion;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ModDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        pack.addProvider(EnchantingAdvancementProvider::new);

    }
    public static class EnchantingAdvancementProvider extends FabricAdvancementProvider {
        protected EnchantingAdvancementProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
            super(output, registryLookup);
        }

        @Override
        public void generateAdvancement(RegistryWrapper.WrapperLookup wrapperLookup, Consumer<AdvancementEntry> consumer) {
            AdvancementEntry rootAdvancement = Advancement.Builder.create()
                    .display(
                            Items.ENCHANTED_BOOK,
                            Text.literal("Enchanting Desicions"),
                            Text.literal("Advancements for Enchanting Decisions"),
                            Identifier.ofVanilla("gui/advancements/backgrounds/stone"),
                            AdvancementFrame.TASK,
                            false,
                            false,
                            false
                    ).criterion("enchanting_root", TickCriterion.Conditions.createTick())
                    .build(consumer, EnchantingDecisions.MOD_ID + "/root");
            AdvancementEntry grindDown = Advancement.Builder.create()
                    .display(
                            Items.GRINDSTONE, // The display icon
                            Text.literal("This is Mine Now"), // The title
                            Text.literal("Use a grindstone to harvest a natural enchantment"), // The description
                            null,
                            AdvancementFrame.TASK, // TASK, CHALLENGE, or GOAL
                            true, // Show the toast when completing it
                            true, // Announce it to chat
                            false // Hide it in the advancement tab until it's achieved
                    )
                    .parent(rootAdvancement)
                    .criterion("grind_down", ModCriteria.GRIND_DOWN_CRITERION.create(new GrindDownCriterion.Conditions(Optional.empty())))
                    // Give the advancement an id
                    .build(consumer, EnchantingDecisions.MOD_ID + ":grind_down");
            AdvancementEntry bookUp = Advancement.Builder.create()
                    .display(
                            Items.CHISELED_BOOKSHELF, // The display icon
                            Text.literal("Powered Up"), // The title
                            Text.literal("Add a new enchanted book to an enchantment table's library"), // The description
                            null,
                            AdvancementFrame.TASK, // TASK, CHALLENGE, or GOAL
                            true, // Show the toast when completing it
                            true, // Announce it to chat
                            false // Hide it in the advancement tab until it's achieved
                    )
                    .parent(rootAdvancement)
                    .criterion("power_up", ModCriteria.POWER_UP_CRITERION.create(new PowerUpCriterion.Conditions(Optional.empty())))
                    // Give the advancement an id
                    .build(consumer, EnchantingDecisions.MOD_ID + ":power_up");
        }
    }


}
