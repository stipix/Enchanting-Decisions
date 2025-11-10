package stipix.enchanting_decisions;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

import java.util.Objects;

public class CustomGrindstoneScreenHandler extends ScreenHandler {
    private final Inventory result = new CraftingResultInventory();
    final Inventory input = new SimpleInventory(2) {
        @Override
        public void markDirty() {
            super.markDirty();
            CustomGrindstoneScreenHandler.this.onContentChanged(this);
        }
    };
    private final ScreenHandlerContext context;
    private ItemStack originator;
    private boolean modifyInput = false;
    private SoundEvent sound = SoundEvents.BLOCK_GRINDSTONE_USE;

    public CustomGrindstoneScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public CustomGrindstoneScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(ScreenHandlerType.GRINDSTONE, syncId);
        this.context = context;
        this.addSlot(new Slot(this.input, 0, 49, 19) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return EnchantmentHelper.hasEnchantments(stack) || stack.isOf(Items.BOOK);
            }
        });
        this.addSlot(new Slot(this.input, 1, 49, 40) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return EnchantmentHelper.hasEnchantments(stack) || stack.isOf(Items.BOOK);
            }
        });
        this.addSlot(new Slot(this.result, 2, 129, 34) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }

            @Override
            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                context.run((world, pos) -> {
                    //world.syncWorldEvent(WorldEvents.LECTERN_BOOK_PAGE_TURNED, pos, 0);  //AUDIO (grindstone_used)
                    //world.playSoundClient(SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 0.8f, MathHelper.clamp((float)Math.random(),0.7f,1.2f));
                    world.playSound(null, pos, sound, SoundCategory.BLOCKS, 1.0f, world.random.nextFloat() * 0.1F + 0.9F);
                });
                if (!input.getStack(0).isEmpty() && !input.getStack(1).isEmpty()){
                    for (int i = 0; i < 2; i++) {
                        if (input.getStack(i).isOf(Items.BOOK)) {
                            input.getStack(i).decrement(1);
                            if (input.getStack(1 - i).isOf(Items.ENCHANTED_BOOK)) {
                                //TODO - remove first enchantment from book
                                //CustomGrindstoneScreenHandler.this.input.setStack(0, ItemStack.EMPTY);
                                if (modifyInput) {
                                    input.setStack(1 - i, originator);
                                    onContentChanged(inventory);
                                }
                            } else if (input.getStack(1 - i).hasEnchantments()) {
                                if (modifyInput) {
                                    if(player instanceof ServerPlayerEntity) {
                                        ModCriteria.GRIND_DOWN_CRITERION.trigger((ServerPlayerEntity) player);
                                    }

                                    input.setStack(1 - i, ItemStack.EMPTY);
                                    onContentChanged(inventory);

                                }
                            }
                        }
                    }
                } else {
                    for (int i = 0; i < 2; i++) {
                        if (input.getStack(i).isOf(Items.ENCHANTED_BOOK)) {
                            input.getStack(i).decrement(1);
                            onContentChanged(inventory);
                        } else { //TODO - check if this is good
                            input.setStack(i, ItemStack.EMPTY);
                        }
                    }
                }

            } //end of onTakeItem()


        });
        this.addPlayerSlots(playerInventory, 8, 84);
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        super.onContentChanged(inventory);
        if (inventory == this.input) {
            this.updateResult();
        }
    }

    private void updateResult() {
        this.result.setStack(0, this.getOutputStack(this.input.getStack(0), this.input.getStack(1)));
        this.sendContentUpdates();
    }

    private ItemStack getOutputStack(ItemStack firstInput, ItemStack secondInput) {
        if ((firstInput.isOf(Items.BOOK) && secondInput.hasEnchantments() && !Boolean.TRUE.equals(secondInput.get(ModComponents.PLAYER_ENCHANTED))) || (secondInput.isOf(Items.BOOK) && firstInput.hasEnchantments() && !Boolean.TRUE.equals(firstInput.get(ModComponents.PLAYER_ENCHANTED)))) {
            return this.transmute(firstInput, secondInput); //THIS IS WHAT IS INVOKED WHEN TRANSMUTING
        }
        boolean bl = !firstInput.isEmpty() || !secondInput.isEmpty();
        if (!bl) {
            return ItemStack.EMPTY;
        } else if (firstInput.isOf(Items.ENCHANTED_BOOK) && secondInput.isOf(Items.BOOK)) {
            if (Objects.requireNonNull(firstInput.get(DataComponentTypes.STORED_ENCHANTMENTS)).getSize()>1) {
                return this.transferEnchantment(firstInput, secondInput);
            } else {
                return ItemStack.EMPTY;
            }
        } else if (secondInput.isOf(Items.ENCHANTED_BOOK) && firstInput.isOf(Items.BOOK)) {
            if (Objects.requireNonNull(secondInput.get(DataComponentTypes.STORED_ENCHANTMENTS)).getSize()>1) {
                return this.transferEnchantment(secondInput, firstInput);
            } else {
                return ItemStack.EMPTY;
            }
        } else if (firstInput.getCount() <= 1 && secondInput.getCount() <= 1) {
            boolean bl2 = !firstInput.isEmpty() && !secondInput.isEmpty();
            if (!bl2) {
                ItemStack itemStack = !firstInput.isEmpty() ? firstInput : secondInput;
                return !EnchantmentHelper.hasEnchantments(itemStack) ? ItemStack.EMPTY : this.grind(itemStack.copy());
            } else {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
    }

    private ItemStack transmute(ItemStack firstInput, ItemStack secondInput){
        if (firstInput.isOf(Items.BOOK) && secondInput.hasEnchantments() && !Boolean.TRUE.equals(secondInput.get(ModComponents.PLAYER_ENCHANTED))) {
            ItemStack book = Items.ENCHANTED_BOOK.getDefaultStack();
            System.out.println(secondInput.getEnchantments());

            for (RegistryEntry<Enchantment> e : secondInput.getEnchantments().getEnchantments()){
                book.addEnchantment(e,1);
            }
            modifyInput = true;
            sound = SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE;
            return book;

        } else if (secondInput.isOf(Items.BOOK) && firstInput.hasEnchantments() && !Boolean.TRUE.equals(firstInput.get(ModComponents.PLAYER_ENCHANTED))) {
            ItemStack book = Items.ENCHANTED_BOOK.getDefaultStack();
            for (RegistryEntry<Enchantment> e : firstInput.getEnchantments().getEnchantments()){
                book.addEnchantment(e,1);
            }
            modifyInput = true;
            sound = SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE;
            return book;
        } else {
            return ItemStack.EMPTY;
        }
    }

    private ItemStack transferEnchantment(ItemStack firstInput, ItemStack secondInput) {
        //TODO - UNSHIT THIS FUCK
        ItemStack resultant = Items.ENCHANTED_BOOK.getDefaultStack();
        //ItemStack originator = Items.ENCHANTED_BOOK.getDefaultStack();
        originator = Items.ENCHANTED_BOOK.getDefaultStack();
        modifyInput = true;

        EnchantmentHelper.apply(resultant, components -> {
            ItemEnchantmentsComponent itemEnchantmentsComponent = EnchantmentHelper.getEnchantments(firstInput);

            for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : itemEnchantmentsComponent.getEnchantmentEntries()) {
                RegistryEntry<Enchantment> registryEntry = entry.getKey();
                if (components.getLevel(registryEntry) == 0) {
                    components.add(registryEntry, entry.getIntValue());
                    return;
                    //components.remove();
                }
            }
        });

        EnchantmentHelper.apply(originator, components -> {
            ItemEnchantmentsComponent itemEnchantmentsComponent = EnchantmentHelper.getEnchantments(firstInput);
            int i = 0;
            for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : itemEnchantmentsComponent.getEnchantmentEntries()) {
                RegistryEntry<Enchantment> registryEntry = entry.getKey();
                if (components.getLevel(registryEntry) == 0) {
                    if(i>0) {
                        components.add(registryEntry, entry.getIntValue());
                    }
                    //components.remove();
                    i++;
                }
            }
        });
        sound = SoundEvents.ITEM_BOOK_PAGE_TURN;
        return resultant;

    }



    private ItemStack grind(ItemStack item) {
        ItemEnchantmentsComponent itemEnchantmentsComponent = EnchantmentHelper.apply(
                item, components -> components.remove(enchantment -> !enchantment.isIn(EnchantmentTags.CURSE) || enchantment.isIn(EnchantmentTags.CURSE))
        );
        if (item.isOf(Items.ENCHANTED_BOOK) && itemEnchantmentsComponent.isEmpty()) {
            item = item.withItem(Items.BOOK);
        }
        sound = SoundEvents.BLOCK_GRINDSTONE_USE;
        return item;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.context.run((world, pos) -> this.dropInventory(player, this.input));
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, Blocks.GRINDSTONE);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot2 = this.slots.get(slot);
        if (slot2 != null && slot2.hasStack()) {
            ItemStack itemStack2 = slot2.getStack();
            itemStack = itemStack2.copy();
            ItemStack itemStack3 = this.input.getStack(0);
            ItemStack itemStack4 = this.input.getStack(1);
            if (slot == 2) {
                if (!this.insertItem(itemStack2, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }

                slot2.onQuickTransfer(itemStack2, itemStack);
            } else if (slot != 0 && slot != 1) {
                if (!itemStack3.isEmpty() && !itemStack4.isEmpty()) {
                    if (slot >= 3 && slot < 30) {
                        if (!this.insertItem(itemStack2, 30, 39, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (slot >= 30 && slot < 39 && !this.insertItem(itemStack2, 3, 30, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.insertItem(itemStack2, 0, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(itemStack2, 3, 39, false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack2.isEmpty()) {
                slot2.setStack(ItemStack.EMPTY);
            } else {
                slot2.markDirty();
            }

            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot2.onTakeItem(player, itemStack2);
        }

        return itemStack;
    }

}
