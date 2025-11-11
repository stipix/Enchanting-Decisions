package stipix.enchanting_decisions.mixin;

import com.mojang.logging.LogUtils;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.ForgingSlotsManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.StringHelper;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import stipix.enchanting_decisions.EnchantabilityCosts;
import stipix.enchanting_decisions.MiscToolEnchantabilities;

import java.util.Objects;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {

    @Shadow
    private static final Logger LOGGER = LogUtils.getLogger();

    @Nullable
    @Shadow
    private String newItemName;

    @Unique
    int materialLeftover=0;


    public AnvilScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, ForgingSlotsManager forgingSlotsManager) {
        super(type, syncId, playerInventory, context, forgingSlotsManager);
    }

    @Inject(method = "canTakeOutput", at=@At("HEAD"),cancellable = true)
    public void canTakeOutput(PlayerEntity player, boolean present, CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(true);
    }

    @Inject(method = "onTakeOutput", at=@At("HEAD"),cancellable = true)
    protected void onTakeOutput(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        if (player instanceof ServerPlayerEntity serverPlayerEntity
                && !StringHelper.isBlank(this.newItemName)
                && !this.input.getStack(0).getName().getString().equals(this.newItemName)) {
            serverPlayerEntity.getTextStream().filterText(this.newItemName);
        }

        this.input.setStack(0, ItemStack.EMPTY);
        this.input.getStack(1).setCount(materialLeftover);

        this.context.run((world, pos) -> {
            BlockState blockState = world.getBlockState(pos);
            if (!player.isInCreativeMode() && blockState.isIn(BlockTags.ANVIL) && player.getRandom().nextFloat() < 0.12F) {
                BlockState blockState2 = AnvilBlock.getLandingState(blockState);
                if (blockState2 == null) {
                    world.removeBlock(pos, false);
                    world.syncWorldEvent(WorldEvents.ANVIL_DESTROYED, pos, 0);
                } else {
                    world.setBlockState(pos, blockState2, Block.NOTIFY_LISTENERS);
                    world.syncWorldEvent(WorldEvents.ANVIL_USED, pos, 0);
                }
            } else {
                world.syncWorldEvent(WorldEvents.ANVIL_USED, pos, 0);
            }
        });
        ci.cancel();
    }

    @Inject(method = "updateResult", at=@At("HEAD"), cancellable = true)
    public void updateResult(CallbackInfo ci) {
        // TODO - 4 sections
        // 1. Repair
        // 2. Merge enchanted books
        // 3. Rename
        // 4. NOP

        this.context.run((world, pos) -> {

            ItemStack originalInput = this.input.getStack(0);
            int operationCount = 0;
        ItemStack targetCopy = originalInput.copy();

        // 1. Repairing (if invalid)
        if (!originalInput.isEmpty() && originalInput.isDamaged() && originalInput.canRepairWith(this.input.getStack(1))) {
            ItemStack repairStack = this.input.getStack(1);

            materialLeftover = repairStack.getCount();

            //Mending level to yield a repair bonus
            int mendingLevel = EnchantmentHelper.getLevel(world.getRegistryManager().getEntryOrThrow(Enchantments.MENDING), targetCopy);

            //Repair tac to make repairs less efficient the more enchanted an item is
            float repairTax;
            if(targetCopy.get(DataComponentTypes.ENCHANTABLE)!= null) {
                float numerator = (float) EnchantabilityCosts.getEnchantabilityUsed(targetCopy);
                float denominator;

                try{
                    denominator = MiscToolEnchantabilities.getEnchantability(targetCopy.getItem()).get();
                } catch (NullPointerException e) {
                    denominator = (float) Objects.requireNonNull(targetCopy.get(DataComponentTypes.ENCHANTABLE)).value();
                }
                repairTax = (numerator/denominator)*0.18f;
            }else{
                repairTax = 0.0f;
            }
            
            float numerator = (float)(targetCopy.getDamage());
            float denominator = (float)targetCopy.getMaxDamage()*((mendingLevel*0.06f)+(0.34f-repairTax));
            int costToFull = Math.min((int)Math.ceil(numerator/denominator), repairStack.getCount());
            if (costToFull > 0){
                operationCount++;
                materialLeftover -= costToFull;
                targetCopy.setDamage(targetCopy.getDamage()-((int)denominator*costToFull));
            }


        // 2. Combine enchanted books
        }  else if (this.input.getStack(0).isOf(Items.ENCHANTED_BOOK) && this.input.getStack(1).isOf(Items.ENCHANTED_BOOK)) {
            targetCopy = Items.ENCHANTED_BOOK.getDefaultStack();
            ItemEnchantmentsComponent input_1 = Objects.requireNonNull(this.input.getStack(0).get(DataComponentTypes.STORED_ENCHANTMENTS));
            ItemEnchantmentsComponent input_2 = Objects.requireNonNull(this.input.getStack(1).get(DataComponentTypes.STORED_ENCHANTMENTS));

            //Making sure both books have only 1 enchantment, and it's the same kind
            if ((input_1.getEnchantments().size() == 1) && (input_2.getEnchantments().size() == 1)){
                for (RegistryEntry<Enchantment> entry : input_1.getEnchantments()){
                    if(input_2.getEnchantments().contains(entry)){
                        //Making sure enchantments can't exceed their max level
                        if(input_1.getLevel(entry) < entry.value().getMaxLevel() && input_2.getLevel(entry) < entry.value().getMaxLevel()){
                            targetCopy.addEnchantment(entry, Math.min((input_1.getLevel(entry) + (input_2.getLevel(entry))), entry.value().getMaxLevel()));
                            //out.addEnchantment(entry,3);
                            //out.set(DataComponentTypes.STORED_ENCHANTMENTS, (Enchantments.MENDING,3))
                            //EnchantmentHelper.
                            operationCount++;
                            break;
                        }
                    }
                }
            }

        }

            // 3. Renaming
            if (this.newItemName != null && !StringHelper.isBlank(this.newItemName)) {
                if (!this.newItemName.equals(originalInput.getName().getString())) {
                    operationCount++;
                    this.output.getStack(0).set(DataComponentTypes.CUSTOM_NAME, Text.literal(this.newItemName));
                }
            } else if (originalInput.contains(DataComponentTypes.CUSTOM_NAME)) {
                operationCount++;
                this.output.getStack(0).remove(DataComponentTypes.CUSTOM_NAME);
            }

        if (!StringHelper.isBlank(this.newItemName)){
            operationCount++;
            targetCopy.remove(DataComponentTypes.CUSTOM_NAME);
            targetCopy.set(DataComponentTypes.CUSTOM_NAME, Text.literal(this.newItemName));
        }

        // Apply output
        this.output.setStack(0, targetCopy);

        // 4. No Operation
        if (operationCount <= 0) {
            this.output.setStack(0, ItemStack.EMPTY);
        }

        });
    ci.cancel();
    }

}
