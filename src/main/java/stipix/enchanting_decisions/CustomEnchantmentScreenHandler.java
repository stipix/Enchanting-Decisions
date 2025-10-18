
package stipix.enchanting_decisions;


import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChiseledBookshelfBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EnchantableComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CustomEnchantmentScreenHandler extends ScreenHandler{
    static final Identifier EMPTY_LAPIS_LAZULI_SLOT_TEXTURE = Identifier.ofVanilla("container/slot/lapis_lazuli");
    private final Inventory newinventory;

    private final ScreenHandlerContext context;

    public final int[] enchantmentPower = new int[3];
    public final int[] enchantmentId = new int[]{-1, -1, -1};
    public final int[] enchantmentLevel = new int[]{-1, -1, -1};


    private Inventory inventory;
    //both properties hold 16 enchantments, far above the max number of enchants one type of armor can hold, should be swapped to be adaptive later
    private static int[] enchantment;//carries the ID's fo each enchantment that can be used
    private static int[] enchantmentTier;//carries the tier of the enchantment (like the II in Unbreaking II)
    private static int[] selectedTier;//carries the tier of the enchantment (like the II in Unbreaking II)
    public Property usedEnchantable = Property.create();


    public CustomEnchantmentScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos pos) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);

    }

    public CustomEnchantmentScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public CustomEnchantmentScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(ModScreenHandlers.CUSTOM_ENCHANTMENT_SCREEN_HANDLER, syncId);
        this.context = context;

        CustomEnchantmentScreenHandler handler = this;

        enchantment = new int[16];
        enchantmentTier = new int[16];
        selectedTier = new int[16];
        newinventory = new SimpleInventory(3){
            @Override
            public void markDirty() {
                super.markDirty();
                handler.onContentChanged(this);
            }
        };;
        for(int i = 0; i < 16; i++) {
            enchantment[i] = -1;
            enchantmentTier[i] = 0;
            selectedTier[i] = 0;
        }
        this.addSlot(new Slot(this.newinventory, 0, 15, 46) {
            @Override
            public int getMaxItemCount() {
                return 1;
            }
        });
        this.addSlot(new Slot(this.newinventory, 1, 171, 4) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(Items.LAPIS_LAZULI);
            }


            @Override
            public Identifier getBackgroundSprite() {
                return CustomEnchantmentScreenHandler.EMPTY_LAPIS_LAZULI_SLOT_TEXTURE;
            }
        });
        this.addSlot(new Slot(this.newinventory, 2, 171, 46) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }
        });
        this.addPlayerSlots(playerInventory, 16, 89);
        for(int i = 0; i < 16; i++) {
            this.addProperty(Property.create(enchantment, i));
        }
        for(int i = 0; i < 16; i++) {
            this.addProperty(Property.create(enchantmentTier, i));
        }
        for(int i = 0; i < 16; i++) {
            this.addProperty(Property.create(selectedTier, i));
        }
        this.addProperty(usedEnchantable);
        this.addProperty(Property.create(this.enchantmentPower, 0));
        this.addProperty(Property.create(this.enchantmentPower, 1));
        this.addProperty(Property.create(this.enchantmentPower, 2));
        this.addProperty(Property.create(this.enchantmentId, 0));
        this.addProperty(Property.create(this.enchantmentId, 1));
        this.addProperty(Property.create(this.enchantmentId, 2));
        this.addProperty(Property.create(this.enchantmentLevel, 0));
        this.addProperty(Property.create(this.enchantmentLevel, 1));
        this.addProperty(Property.create(this.enchantmentLevel, 2));
    }




    @Override
    public void onContentChanged(Inventory inventory){
        CustomEnchantmentScreenHandler handler = this;
        if (inventory == this.newinventory) {
            ItemStack inputStack = inventory.getStack(0);
            ItemStack fuelStack = inventory.getStack(1);
            ItemStack outputstack = inventory.getStack(2);

            if(!fuelStack.isEmpty()){
                this.context.run((world, pos) -> {
                    BlockEntity entity = world.getBlockEntity(pos);
                    if(entity instanceof EtableFuelLevelInterface fuelInterface){
                        fuelInterface.enchantingDecisions$setFuelLevel(fuelInterface.enchantingDecisions$getFuelLevel() + 12*fuelStack.getCount());
                        EnchantingDecisions.LOGGER.info("Fuel Level {}", fuelInterface.enchantingDecisions$getFuelLevel());
                        inventory.removeStack(1);
                    }
                });
            }
            //there are 4 possible states
            //1) both slots are empty -> enchanting table not in use, do nothing
            //2) input is full but output is empty -> new item is placed in the input slot, must generate new enchantments or output has been taken and the fuel is consumed and input is to be deleted
            //3) output is full but input is empty -> input was taken out to refuse the enchantment, delete the proposed item
            //4) both are full -> enchanting table in use, do nothing
            if(!inputStack.isEmpty() && !outputstack.isEmpty()){
                if(inputStack.getName() != outputstack.getName()){
                    if((inputStack.isEnchantable()||inputStack.hasEnchantments()) && !inputStack.isOf(Items.BOOK)){
                        checkNewItem(inventory, inputStack);
                    }
                }
            }
            if (!inputStack.isEmpty() && outputstack.isEmpty() &&
                    (inputStack.isEnchantable()||inputStack.hasEnchantments()) && !inputStack.isOf(Items.BOOK)) {
                if(enchantment[0] == -1){//table has not been setup for use
                    checkNewItem(inventory, inputStack);
                }
                else{//handles the completion of the enchanting process

                    this.context.run((world, pos) -> {
                        handler.enchantmentPower[0] = 0;
                        for (int i = 0; i < enchantment.length; i++) {
                            enchantment[i] = -1;
                            enchantmentTier[i] = 0;
                            selectedTier[i] = 0;
                        }
                        //unable to implement achievements and sfx(?) without player entity
                        //                    player.incrementStat(Stats.ENCHANT_ITEM);
                        //                    if (player instanceof ServerPlayerEntity) {
                        //                        Criteria.ENCHANTED_ITEM.trigger((ServerPlayerEntity)player, itemStack3, i);
                        //                    }

                        //TODO: Add Lapis fuel consumption
                        int fuelConsumed = 10;
                        BlockEntity entity = world.getBlockEntity(pos);
                        if(entity instanceof EtableFuelLevelInterface fuelInterface){
                            fuelInterface.enchantingDecisions$setFuelLevel(fuelInterface.enchantingDecisions$getFuelLevel() - fuelConsumed);
                            EnchantingDecisions.LOGGER.info("Fuel Level {}", fuelInterface.enchantingDecisions$getFuelLevel());
                        }
                        inventory.removeStack(0);//delete input as the user has the output now
                        handler.sendContentUpdates();
                    });
                }
            }
            if (inputStack.isEmpty() && !outputstack.isEmpty()) {

                this.context.run((world, pos) -> {

                    for (int i = 0; i < enchantment.length; i++) {
                        enchantment[i] = -1;
                        enchantmentTier[i] = 0;
                        selectedTier[i] = 0;
                    }
                    inventory.removeStack(2);
                    handler.sendContentUpdates();
                });
            }

        }
    }
/*
        Function: checkNewItem
        Brief: used to read the type of item and enchantments of a new input selection, and find compatible and available enchantments
        return: none
        parameters:
            Inventory, enchanting table inventory
            ItemStack, input item slot to be scanned
 */
    private void checkNewItem(Inventory inventory, ItemStack inputStack) {
        this.context.run((world, pos) -> {
            ItemEnchantmentsComponent enchants = inputStack.getEnchantments();
            this.enchantmentPower[0] = 1;
            Registry<Enchantment> EnchantRegistry =  world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);

            Stream<Enchantment> availableEnchantments = Stream.empty();
            for (BlockPos blockPos : EnchantingTableBlock.POWER_PROVIDER_OFFSETS) {
                availableEnchantments = Stream.concat(availableEnchantments, checkAvailableEnchants(world, pos, blockPos));
            }

            List<Enchantment> availableList = new java.util.ArrayList<>(availableEnchantments.toList());


            int latest = 0;


            for (int i = 0; i < enchantment.length; i++) {
                enchantment[i] = -1;
                enchantmentTier[i] = 0;
                selectedTier[i] = 0;
            }
            //iterates over both arrays
            availableList.sort(Comparator.comparing(enchant ->
                    Text.translatable("enchantment.minecraft.".concat(EnchantRegistry.getEntry(enchant).getIdAsString().replaceFirst("minecraft:", ""))).getString()));
            for (Enchantment available : availableList) {
                if(inputStack.canBeEnchantedWith(EnchantRegistry.getEntry(available), EnchantingContext.ACCEPTABLE)){
                    boolean repeatflag = false;
                    //checks for and handles repeated enchantments
                    for (int i = 0; i < enchantment.length; i++) {
                        if(enchantment[i] ==  EnchantRegistry.getRawId(available)) {
                            enchantmentTier[i]++;
                            if(enchantmentTier[i] > available.getMaxLevel()){
                                enchantmentTier[i] = available.getMaxLevel();
                            }
                            repeatflag = true;
                        }
                    }
                    //if it is not a repeat, add it to the entry
                    if(!repeatflag) {
                        if(latest < 16) {
                            enchantment[latest] = EnchantRegistry.getRawId(available);
                            enchantmentTier[latest] = 1;
                            latest++;
                        }
                    }
                }
            }
            for(int i = 0; i < enchantment.length; i++) {
                int level = enchants.getLevel(EnchantRegistry.getEntry(EnchantRegistry.get(enchantment[i])));
                selectedTier[i] = level;
            }

            //create the proposed item
            ItemStack preposed = inputStack.copy();
            for(int i = 0; i < enchantment.length; i++) {
                if(selectedTier[i] > 0) {
                    preposed.addEnchantment(EnchantRegistry.getEntry(EnchantRegistry.get(enchantment[i])), selectedTier[i]);
                }
            }
            inventory.setStack(2, preposed);


            this.sendContentUpdates();
        });
    }


    /*
        Function: onButtonCLick
        Brief: handled Enchantment screen button presses to increment or decrement enchantment values
        return: bool, returns true if the button click is valid
        parameters:
            PlayerEntity, the player that clicked on the buttons
            int, a bit compressed int to carry the button ID (bits 0-16) and if the enchantment at that ID should be decremented or incremented (bits 17, 18)
    */
    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        int buttonID = id & 0xFFFF;
        int selection = (id>>16 & 0x3);
        if (enchantment[buttonID] != -1) {

            EnchantableComponent comp  = this.newinventory.getStack(0).getComponents().get(DataComponentTypes.ENCHANTABLE);
            int enchantability = 0;
            if(comp != null){
                enchantability = comp.value();
            }

            this.context.run((world, pos) -> {

            Registry<Enchantment> EnchantRegistry = player.getEntityWorld().getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
            RegistryEntry<Enchantment> toBeAdded = EnchantRegistry.getEntry(EnchantRegistry.get(enchantment[buttonID]));



                if (selection == 2 || selection == 3) {

                    if (EnchantmentHelper.isCompatible(newinventory.getStack(2).getEnchantments().getEnchantments(), toBeAdded)
                        || newinventory.getStack(2).getEnchantments().getLevel(toBeAdded) > 0) {
                        selectedTier[buttonID]++;
                        if (selectedTier[buttonID] > enchantmentTier[buttonID]) {
                            selectedTier[buttonID] = enchantmentTier[buttonID];
                        }
                    }


                } else if (selection == 1) {
                    selectedTier[buttonID]--;
                    if (selectedTier[buttonID] < 0) {
                        selectedTier[buttonID] = 0;
                    }

                }
                ItemStack proposed = newinventory.getStack(0).copy();
                EnchantmentHelper.apply(
                        proposed, components -> components.remove(enchantment -> !enchantment.isIn(EnchantmentTags.CURSE)));

                for (int i = 0; i < enchantment.length; i++) {
                    if (selectedTier[i] > 0) {
                        proposed.addEnchantment(EnchantRegistry.getEntry(EnchantRegistry.get(enchantment[i])), selectedTier[i]);
                    }
                }
                proposed.set(ModComponents.PLAYER_ENCHANTED, Boolean.TRUE);
                newinventory.setStack(2, proposed);
            });
            return true;
        } else {
            return false;
        }
    }




    //Function: checkAvailableEnchants
    //Parameters: World, the world data
    //            BlockPos, the position of the enchanting table
    //            BlockPos, the offset from the table to the block to be checked
    //return: Stream, all enchantments contained in the block
    //Description: takes a block to be check and checks if the block has access to the table and if it contains any enchantments the table can use

    private Stream<Enchantment> checkAvailableEnchants(World world, BlockPos tablePos, BlockPos providerOffset){
        //create container stream
        Stream<Enchantment> containedEnchantments = Stream.empty();

        //acquire the block state of the block being checked
        BlockState PowerSource = world.getBlockState(tablePos.add(providerOffset));

        //check if the block is a chiseled Bookshelf and has an air (ish) block between them and the enchanting table
        if(PowerSource.getBlock() == Blocks.CHISELED_BOOKSHELF &&
                world.getBlockState(tablePos.add(providerOffset.getX() / 2, providerOffset.getY(), providerOffset.getZ() / 2)).isIn(BlockTags.ENCHANTMENT_POWER_TRANSMITTER)){
            DefaultedList<ItemStack> books;
            //get the bookshelf inventory
            BlockEntity inventory = world.getBlockEntity(tablePos.add(providerOffset));
            if(inventory instanceof ChiseledBookshelfBlockEntity) {
                books = ((ChiseledBookshelfBlockEntity) inventory).getHeldStacks();


                //check every enchantment in every book on the shelf
                for (ItemStack itemStack : books) {
                    if (itemStack.getComponents().getTypes().contains(DataComponentTypes.STORED_ENCHANTMENTS)) {
                        Object enchant = itemStack.getOrDefault(DataComponentTypes.STORED_ENCHANTMENTS, 0);
                        if(enchant instanceof ItemEnchantmentsComponent) {
                            for (RegistryEntry<Enchantment> enchantmentRegistry : ((ItemEnchantmentsComponent) enchant).getEnchantments()) {
                                Enchantment enchantment = enchantmentRegistry.value();
                                //append the contained enchantment to the output steam
                                containedEnchantments = Stream.concat(containedEnchantments, Stream.of(enchantment));

                            }
                        }
                    }

                }
            }
        }
        return containedEnchantments;
    }
    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.context.run((world, pos) -> {
            for(int i = 0; i < 2; i++){
                ItemStack stack = newinventory.removeStack(i);
                boolean bl = player.isRemoved() && player.getRemovalReason() != Entity.RemovalReason.CHANGED_DIMENSION;
                boolean bl2 = player instanceof ServerPlayerEntity serverPlayerEntity && serverPlayerEntity.isDisconnected();
                if (bl || bl2) {
                    player.dropItem(stack, false);
                } else if (player instanceof ServerPlayerEntity) {
                    player.getInventory().offerOrDrop(stack);
                }
            }
        });
    }

    public int[] getEnchants(){
        return enchantment;
    }

    public int[] getEnchantsTier(){
        return enchantmentTier;
    }

    public int[] getSelectedTier(){
        return selectedTier;
    }


    public int getLapisCount() {
        ItemStack itemStack = this.newinventory.getStack(1);
        return itemStack.isEmpty() ? 0 : itemStack.getCount();
    }



    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, Blocks.ENCHANTING_TABLE);
    }


    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot2 = this.slots.get(slot);
        if (slot2 != null && slot2.hasStack()) {
            ItemStack itemStack2 = slot2.getStack();
            itemStack = itemStack2.copy();
            if (slot == 0) {
                if (!this.insertItem(itemStack2, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (slot == 1) {
                if (!this.insertItem(itemStack2, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (itemStack2.isOf(Items.LAPIS_LAZULI)) {
                if (!this.insertItem(itemStack2, 1, 2, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (this.slots.get(0).hasStack() || !this.slots.get(0).canInsert(itemStack2)) {
                    return ItemStack.EMPTY;
                }

                ItemStack itemStack3 = itemStack2.copyWithCount(1);
                itemStack2.decrement(1);
                this.slots.get(0).setStack(itemStack3);
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


