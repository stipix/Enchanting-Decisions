package stipix.enchanting_decisions;


import net.minecraft.block.AnvilBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.ForgingSlotsManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.StringHelper;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class CustomAnvilScreenHandler extends ForgingScreenHandler {

    private int materialLeftover = 0;
    private @Nullable String newItemName;

    public CustomAnvilScreenHandler(int syncId, PlayerInventory inventory) {
        this(syncId, inventory, ScreenHandlerContext.EMPTY);
    }


    public CustomAnvilScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(ModScreenHandlers.CUSTOM_ANVIL_SCREEN_HANDLER, syncId, playerInventory, context, getForgingSlotsManager());
    }


    private static ForgingSlotsManager getForgingSlotsManager() {
        return ForgingSlotsManager.builder().input(0, 27, 47, stack -> true).input(1, 76, 47, stack -> true).output(2, 134, 47).build();
    }

    @Nullable
    private static String sanitize(String name) {
        String string = StringHelper.stripInvalidChars(name);
        return string.length() <= 50 ? string : null;
    }

    //TODO - Have renaming work again (gotta prioritize enabling output for single items)
    public boolean setNewItemName(String newItemName) {
        String string = sanitize(newItemName);
        if (string != null && !string.equals(this.newItemName)) {
            this.newItemName = string;
            if (this.getSlot(2).hasStack()) {
                ItemStack itemStack = this.getSlot(2).getStack();
                if (StringHelper.isBlank(string)) {
                    itemStack.remove(DataComponentTypes.CUSTOM_NAME);
                } else {
                    itemStack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(string));
                }
            }

            this.updateResult();
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onTakeOutput(PlayerEntity player, ItemStack stack) {
   
        this.input.getStack(1).setCount(materialLeftover);


        if (player instanceof ServerPlayerEntity serverPlayerEntity && //TODO - WHAT
                !StringHelper.isBlank(this.newItemName)
                && !this.input.getStack(0).getName().getString().equals(this.newItemName)) {
            serverPlayerEntity.getTextStream().filterText(this.newItemName);


        }






        this.input.setStack(0, ItemStack.EMPTY);
        this.context.run((world, pos) -> {
            BlockState blockState = world.getBlockState(pos);
            if (!player.isInCreativeMode() && blockState.isIn(BlockTags.ANVIL) && player.getRandom().nextFloat() < 0.10F) { //base 12% chance of damaging
                BlockState blockState2 = AnvilBlock.getLandingState(blockState);
                if (blockState2 == null) {
                    world.removeBlock(pos, false);
                    world.syncWorldEvent(WorldEvents.ANVIL_DESTROYED, pos, 0);
                } else {
                    world.setBlockState(pos, blockState2, Block.NOTIFY_LISTENERS);
                    world.syncWorldEvent(WorldEvents.ANVIL_USED, pos, 0);         //AUDIO (anvil_used)
                }
            } else {
                world.syncWorldEvent(WorldEvents.ANVIL_USED, pos, 0);           //AUDIO
            }
        });
    }

    @Override
    protected boolean canUse(BlockState state) {
        return state.isIn(BlockTags.ANVIL);
    }

    @Override
    public void updateResult() {
        //this.output.setStack(0, ItemStack.EMPTY);
        ItemStack originalInput = this.input.getStack(0);
        int i = 0;
        if (!originalInput.isEmpty() &&
                originalInput.isDamaged() &&
                !this.input.getStack(1).isEmpty() &&
                originalInput.canRepairWith(this.input.getStack(1))) {

            ItemStack targetCopy = originalInput.copy();
            ItemStack repairItem = this.input.getStack(1);
            if (!repairItem.isEmpty()) {
                if (targetCopy.isDamageable() && originalInput.canRepairWith(repairItem)) {

                    this.materialLeftover=repairItem.getCount();

                    float mendingBonus=0;
                    for(RegistryEntry<Enchantment> e : targetCopy.getEnchantments().getEnchantments()){
                        if(e.matchesKey(Enchantments.MENDING)){
                            mendingBonus = 0.06f* targetCopy.getEnchantments().getLevel(e);
                        }
                    }
                    float repairTax;
                    if(targetCopy.get(DataComponentTypes.ENCHANTABLE)!= null) {
                        float numerator = (float)EnchantabilityCosts.getEnchantabilityUsed(targetCopy);
                        float denominator;

                        try{
                            denominator = MiscToolEnchantabilities.getEnchantability(targetCopy.getItem()).get();
                        } catch (NullPointerException e) {
                            denominator = (float)Objects.requireNonNull(targetCopy.get(DataComponentTypes.ENCHANTABLE)).value();
                        }

                        repairTax = (numerator/denominator)*0.18f;
                    }else{
                        repairTax = 0.0f;
                    }
                    //k is the lesser durability between 1/4 dur and current amount
                    int k = Math.min(targetCopy.getDamage(), (int)(targetCopy.getMaxDamage()*(mendingBonus+0.3f-repairTax)));

                    if (k <= 0) {
                        this.output.setStack(0, ItemStack.EMPTY);
                        return;
                    }

                    //counts out how much you can repair, based off how many materials you have
                    for (int m = 0; k > 0 &&
                            m < repairItem.getCount(); m++) {
                        this.materialLeftover -= 1;
                        int n = targetCopy.getDamage() - k;
                        targetCopy.setDamage(n);
                        i++;
                        k = Math.min(targetCopy.getDamage(), (int)(targetCopy.getMaxDamage()*(mendingBonus+0.3f-repairTax)));
                    }

                }

            }//end of repair section

            if (i <= 0) {
                targetCopy = ItemStack.EMPTY;
            }

            //Handles book combination
            this.output.setStack(0, targetCopy);
            this.sendContentUpdates();
        } else if (this.input.getStack(0).isOf(Items.ENCHANTED_BOOK) && this.input.getStack(1).isOf(Items.ENCHANTED_BOOK)) {
            ItemStack out = Items.ENCHANTED_BOOK.getDefaultStack();
            ItemEnchantmentsComponent input_1 = Objects.requireNonNull(this.input.getStack(0).get(DataComponentTypes.STORED_ENCHANTMENTS));
            ItemEnchantmentsComponent input_2 = Objects.requireNonNull(this.input.getStack(1).get(DataComponentTypes.STORED_ENCHANTMENTS));

            //Making sure both books have only 1 enchantment, and it's the same kind
            if ((input_1.getEnchantments().size() == 1) && (input_2.getEnchantments().size() == 1)){
                for (RegistryEntry<Enchantment> entry : input_1.getEnchantments()){
                    if(input_2.getEnchantments().contains(entry)){
                        //Making sure enchantments can't exceed their max level
                        if(input_1.getLevel(entry) < entry.value().getMaxLevel() && input_2.getLevel(entry) < entry.value().getMaxLevel()){
                            out.addEnchantment(entry, Math.min((input_1.getLevel(entry) + (input_2.getLevel(entry))), entry.value().getMaxLevel()));
                            this.output.setStack(0, out);
                            break;
                        }
                    }
                    this.output.setStack(0, ItemStack.EMPTY);
                    break;
                }
            }

        } else {
            ItemStack targetCopy = this.input.getStack(0).copy();
            if (this.newItemName != null && !StringHelper.isBlank(this.newItemName) && this.input.getStack(1).isEmpty()) {
                if (!this.newItemName.equals(originalInput.getName().getString())) {
                    targetCopy.set(DataComponentTypes.CUSTOM_NAME, Text.literal(this.newItemName));
                    this.output.setStack(0,targetCopy);
                }
            } else if (originalInput.contains(DataComponentTypes.CUSTOM_NAME)) {
                targetCopy.remove(DataComponentTypes.CUSTOM_NAME);
                this.output.setStack(0,targetCopy);
            } else {
                this.output.setStack(0, ItemStack.EMPTY);
            }
        }

    }



}
