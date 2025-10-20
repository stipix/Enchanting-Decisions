package stipix.enchanting_decisions;


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
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.ForgingSlotsManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.StringHelper;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.stream.Stream;

public class CustomAnvilScreenHandler extends ForgingScreenHandler {

    private int materialLeftover = 0;
    private @Nullable String newItemName;

    public CustomAnvilScreenHandler(int syncId, PlayerInventory inventory) {
        this(syncId, inventory, ScreenHandlerContext.EMPTY);
    }


    public CustomAnvilScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {

        //TODO: kill myself
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


        if (player instanceof ServerPlayerEntity serverPlayerEntity
                && !StringHelper.isBlank(this.newItemName)
                && !this.input.getStack(0).getName().getString().equals(this.newItemName)) {
            serverPlayerEntity.getTextStream().filterText(this.newItemName);
        }

        this.input.setStack(0, ItemStack.EMPTY);
        this.context.run((world, pos) -> {
            BlockState blockState = world.getBlockState(pos);
            if (!player.isInCreativeMode() && blockState.isIn(BlockTags.ANVIL) && player.getRandom().nextFloat() < 0.12F) {
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
        this.output.setStack(0, ItemStack.EMPTY);
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
                        repairTax = 0.15f*((float)EnchantabilityCosts.getEnchantabilityUsed(targetCopy) / (float)targetCopy.get(DataComponentTypes.ENCHANTABLE).value());
                    }else{
                        repairTax = 0.0f;
                    }
                    //k is the lesser durability between 1/4 dur and current amount
                    int k = Math.min(targetCopy.getDamage(), (int)(targetCopy.getMaxDamage()*(mendingBonus+0.3f-repairTax)));

                    if (k <= 0) {
                        this.output.setStack(0, ItemStack.EMPTY);
                        return;
                    }

                    for (int m = 0; k > 0 &&
                            m < repairItem.getCount(); m++) {
                        this.materialLeftover -= 1;

                        int n = targetCopy.getDamage() - k;
                        targetCopy.setDamage(n);
                        i++;
                        //change this too
                        k = Math.min(targetCopy.getDamage(), (int)(targetCopy.getMaxDamage()*(mendingBonus+0.3f-repairTax)));
                    }

                }
            }

            if (this.newItemName != null && !StringHelper.isBlank(this.newItemName)) {
                if (!this.newItemName.equals(originalInput.getName().getString())) {
                    targetCopy.set(DataComponentTypes.CUSTOM_NAME, Text.literal(this.newItemName));
                }
            } else if (originalInput.contains(DataComponentTypes.CUSTOM_NAME)) {
                targetCopy.remove(DataComponentTypes.CUSTOM_NAME);
            }

            if (i <= 0) {
                targetCopy = ItemStack.EMPTY;
            }


            this.output.setStack(0, targetCopy);
            this.sendContentUpdates();
        } else if (this.input.getStack(0).getItem().asItem() == Items.ENCHANTED_BOOK &&
                this.input.getStack(0).getItem().asItem() == Items.ENCHANTED_BOOK) {
            ItemStack newBook = Items.ENCHANTED_BOOK.getDefaultStack();
            Set<RegistryEntry<Enchantment>> enchList = this.input.getStack(0).getEnchantments().getEnchantments();
            for (RegistryEntry<Enchantment> ench: this.input.getStack(1).getEnchantments().getEnchantments()){

                if(enchList.contains(ench)){
                    enchList.add(ench);
                    //this.input.getStack(1).getEnchantments().getLevel(ench)
                } else {
                    enchList.add(ench);
                }
                //newBook.addEnchantment(enchList.contains(ench));

            }
            //newBook.addEnchantment();
            this.output.setStack(0, newBook);


        } else {
            this.output.setStack(0, ItemStack.EMPTY);
        }
    }
}
