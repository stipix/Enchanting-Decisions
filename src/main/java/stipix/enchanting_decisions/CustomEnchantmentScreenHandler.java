
package stipix.enchanting_decisions;


import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.minecraft.advancement.criterion.Criteria;
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
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CustomEnchantmentScreenHandler extends ScreenHandler{
    static final Identifier EMPTY_LAPIS_LAZULI_SLOT_TEXTURE = Identifier.ofVanilla("container/slot/lapis_lazuli");
    private final Inventory inventory;

    private final ScreenHandlerContext context;

    public final int[] enchantmentPower = new int[3];
    public final int[] enchantmentId = new int[]{-1, -1, -1};
    public final int[] enchantmentLevel = new int[]{-1, -1, -1};


    //both properties hold 16 enchantments, far above the max number of enchants one type of armor can hold, should be swapped to be adaptive later
    private static int[] enchantment;//carries the ID's fo each enchantment that can be used
    private static int[] enchantmentTier;//carries the tier of the enchantment (like the II in Unbreaking II)
    private static int[] selectedTier;//carries the tier of the enchantment (like the II in Unbreaking II)
    public Property usedEnchantable = Property.create();
    public Property fuel = Property.create();

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
        inventory = new SimpleInventory(3){
            @Override
            public void markDirty() {
                super.markDirty();
                handler.onContentChanged(this);
            }
        };
        for(int i = 0; i < 16; i++) {
            enchantment[i] = -1;
            enchantmentTier[i] = 0;
            selectedTier[i] = 0;
        }
        BlockEntity entity =  this.context.get((World::getBlockEntity), null);
        if(entity != null) {
            if(entity instanceof EtableFuelLevelInterface fuelLevelInterface){
                fuel.set(fuelLevelInterface.enchantingDecisions$getFuelLevel());
            }
        }
        //Input stack
        this.addSlot(new Slot(this.inventory, 0, 15, 46) {
            @Override
            public int getMaxItemCount() {
                return 1;
            }

            @Override
            public void setStack(ItemStack stack, ItemStack previousStack) {
                onSetInput(stack);
                super.setStack(stack, previousStack);
            }

            @Override
            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                onTakeInput();
                super.onTakeItem(player, stack);
            }
        });
        //lapis fuel stack
        this.addSlot(new Slot(this.inventory, 1, 158, 5) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(Items.LAPIS_LAZULI);
            }


            @Override
            public Identifier getBackgroundSprite() {
                return CustomEnchantmentScreenHandler.EMPTY_LAPIS_LAZULI_SLOT_TEXTURE;
            }

            @Override
            public void setStack(ItemStack stack, ItemStack previousStack) {
                onRefuel(stack);
                super.setStack(stack, previousStack);
            }
        });
        //output stack
        this.addSlot(new Slot(this.inventory, 2, 158, 46) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }
//            @Override
//            public ItemStack getStack() {
//                if(fuel.get() >= usedEnchantable.get()){
//                    return super.getStack();
//                }else {
//                    return ItemStack.EMPTY;
//                }
//            }
//            @Override
//            public boolean isEnabled(){
//
//                if(fuel.get() >= usedEnchantable.get()){
//                    return super.isEnabled();
//                }else {
//                    return false;
//                }
//            }
            @Override
            public boolean canTakeItems(PlayerEntity player) {
                if(fuel.get() >= usedEnchantable.get()){
                    return super.isEnabled();
                }else {
                    return false;
                }
            }

            @Override
            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                onTakeOutput(player, stack);
                super.onTakeItem(player, stack);
            }
        });
        this.addPlayerSlots(playerInventory, 10, 80);
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
        this.addProperty(fuel);
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

    private void onRefuel(ItemStack fuelStack) {

        if(fuelStack.isEmpty()) {
            return;
        }
        this.context.run((world, pos) -> {
            BlockEntity entity = world.getBlockEntity(pos);
            if(entity instanceof EtableFuelLevelInterface fuelInterface){
                int fuelAdded = Math.min(fuelStack.getCount(), (64-fuel.get())/2);
                fuelInterface.enchantingDecisions$setFuelLevel(fuel.get() + 2*fuelAdded);
                fuel.set(fuelInterface.enchantingDecisions$getFuelLevel());
                fuelStack.decrement(fuelAdded);
                if(fuelStack.isEmpty()){
                    inventory.removeStack(1);
                }
            }
        });

    }


    /*
            Function: onSetInput
            Brief: used to read the type of item and enchantments when a new input is selected, and find compatible and available enchantments
            return: none
            parameters:
                ItemStack, input item slot to be scanned
     */
    protected void onSetInput(ItemStack inputStack) {

        if (!inputStack.isEmpty() && getItemEnchantability(inputStack) != 0 && !inputStack.isOf(Items.BOOK)) {
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

                //iterate over the available list to
                availableList.sort(Comparator.comparing(enchant -> enchant.description().getString()));
                for (Enchantment available : availableList) {
                    if(inputStack.canBeEnchantedWith(EnchantRegistry.getEntry(available), EnchantingContext.ACCEPTABLE)){
                        boolean repeatflag = false;
                        //checks for and handles repeated enchantments
                        for (int i = 0; i < enchantment.length; i++) {
                            if(enchantment[i] ==  EnchantRegistry.getRawId(available)) {
                                enchantmentTier[i] += 1;
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
                for (RegistryEntry<Enchantment> enchantmentRegistryEntry : inputStack.getEnchantments().getEnchantments()){
                    int level = enchants.getLevel(EnchantRegistry.getEntry(enchantmentRegistryEntry.value()));
                    boolean intableflag = false;
                    for( int i = 0; i < enchantment.length; i++){
                        if(EnchantRegistry.get(enchantment[i]) == enchantmentRegistryEntry.value()){
                            selectedTier[i] = level;
                            intableflag = true;
                        }
                    }
                    if(!intableflag){
                        enchantment[latest] = EnchantRegistry.getRawId(enchantmentRegistryEntry.value());
                        selectedTier[latest] = level;
                        latest++;
                    }
                }

                //create the proposed item
                ItemStack proposed = inputStack.copy();
                for(int i = 0; i < enchantment.length; i++) {
                    if(selectedTier[i] > 0) {
                        proposed.addEnchantment(EnchantRegistry.getEntry(EnchantRegistry.get(enchantment[i])), selectedTier[i]);
                    }
                }
                inventory.setStack(2, proposed);


                this.sendContentUpdates();
            });
        }

    }



    /*
            Function: onTakeInput
            Brief: used to reset the enchanting table when the input is removed
            return: none
            parameters:
                none
     */
    protected void onTakeInput(){

        this.context.run((world, pos) -> {

            for (int i = 0; i < enchantment.length; i++) {
                enchantment[i] = -1;
                enchantmentTier[i] = 0;
                selectedTier[i] = 0;
            }
            inventory.removeStack(2);
            this.sendContentUpdates();
        });
    }



    /*
            Function: onTakeOutput
            Brief: resets the enchanting table, updates player stats, consumes fuel, and plays sound after taking output
            return: none
            parameters:
                PlayerEntity, the player that interacted with the output slot
                ItemStack, the item stack stored in the output slot
     */
    protected void onTakeOutput(PlayerEntity player, ItemStack output){

        this.context.run((world, pos) -> {
            ItemStack inputStack = inventory.getStack(0);
            this.enchantmentPower[0] = 0;
            for (int i = 0; i < enchantment.length; i++) {
                enchantment[i] = -1;
                enchantmentTier[i] = 0;
                selectedTier[i] = 0;
            }
            //Consume fuel
            int fuelConsumed = Math.max(usedEnchantable.get() - EnchantabilityCosts.getEnchantabilityUsed(inputStack),0);
            BlockEntity entity = world.getBlockEntity(pos);
            if(entity instanceof EtableFuelLevelInterface fuelInterface){
                fuelInterface.enchantingDecisions$setFuelLevel(fuelInterface.enchantingDecisions$getFuelLevel() - fuelConsumed);
                fuel.set(fuelInterface.enchantingDecisions$getFuelLevel());
            }

            //only count an enchantment when the player increases the enchantments on the item
            if(fuelConsumed > 0) {
                //unable to implement achievements and sfx(?) without player entity
                player.incrementStat(Stats.ENCHANT_ITEM);
                if (player instanceof ServerPlayerEntity) {
                    Criteria.ENCHANTED_ITEM.trigger((ServerPlayerEntity) player, output, 1);
                }

                world.playSound(null, pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, world.random.nextFloat() * 0.1F + 0.9F);
            }

            inventory.removeStack(0);//delete input as the user has the output now
            this.sendContentUpdates();
        });
    }


/*
    Function: checkAvailableEnchants
    Parameters: World, the world data
                BlockPos, the position of the enchanting table
                BlockPos, the offset from the table to the block to be checked
    return: Stream, all enchantments contained in the block
    Description: takes a block to be check and checks if the block has access to the table and if it contains any enchantments the table can use

 */

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
                        if(enchant instanceof ItemEnchantmentsComponent enchantmentsComponent) {
                            for (RegistryEntry<Enchantment> enchantmentRegistry : enchantmentsComponent.getEnchantments()) {
                                Enchantment enchantment = enchantmentRegistry.value();
                                //append the contained enchantment to the output steam
                                for(int i = 0; i < enchantmentsComponent.getLevel(enchantmentRegistry); i++){
                                    containedEnchantments = Stream.concat(containedEnchantments, Stream.of(enchantment));
                                }

                            }
                        }
                    }

                }
            }
        }
        return containedEnchantments;
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
        int selection = id>>16 & 0x3;
            if (enchantment[id & 0xFFFF] != -1) {

                this.context.run((world, pos) -> {
                    Registry<Enchantment> EnchantRegistry = player.getEntityWorld().getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
                    RegistryEntry<Enchantment> toBeAdded = EnchantRegistry.getEntry(EnchantRegistry.get(enchantment[buttonID]));

                    int enchantability = getItemEnchantability(getInput());
                    int prevTier = selectedTier[buttonID];

                    if (selection == 2) {//increase enchantment level

                        if (EnchantmentHelper.isCompatible(inventory.getStack(2).getEnchantments().getEnchantments(), toBeAdded)
                            || inventory.getStack(2).getEnchantments().getLevel(toBeAdded) > 0) {
                            selectedTier[buttonID]++;
                            if (selectedTier[buttonID] > enchantmentTier[buttonID]) {
                                selectedTier[buttonID] = prevTier;
                            }
                        }


                    } else if (selection == 1) {//decrement tier
                        selectedTier[buttonID]--;
                        if (selectedTier[buttonID] < 0) {
                            selectedTier[buttonID] = prevTier;
                        }

                    }
                    //create a copy of the input stack and remove all enchantments that are not curses, the reapply all enchants that are selected by the player
                    ItemStack proposed = inventory.getStack(0).copy();
                    EnchantmentHelper.apply(
                            proposed, components -> components.remove(enchantment -> !enchantment.isIn(EnchantmentTags.CURSE)));
                    for (int i = 0; i < enchantment.length; i++) {
                        if (selectedTier[i] > 0) {
                            proposed.addEnchantment(EnchantRegistry.getEntry(EnchantRegistry.get(enchantment[i])), selectedTier[i]);
                        }
                    }
                    int enchantabilityUsed = EnchantabilityCosts.getEnchantabilityUsed(proposed);
                    proposed.set(ModComponents.PLAYER_ENCHANTED, Boolean.TRUE);//Mark the item as player enchanted to prevent player from grinding it into books on the grindstone

                    if(     enchantability >= enchantabilityUsed //does not exceed items enchantability
                            && selectedTier[buttonID] != prevTier //the tier is actually changing
//                            && fuel.get() >= Math.max(usedEnchantable.get() - EnchantabilityCosts.getEnchantabilityUsed(proposed),0)// the player as the fuel for the new selection
                    ) {
                        inventory.setStack(2, proposed);
                        usedEnchantable.set(enchantabilityUsed);
                        //click works
                        world.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.UI, 1.0F,  0.9F);

                    } else {
                        //click doesnt work
                        selectedTier[buttonID] = prevTier;
                        world.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.UI, 1.0F,  1.2F);
                    }


            });
            return true;

        } else {
            return false;
        }
    }


    public int getItemEnchantability(ItemStack item){
        EnchantableComponent comp  = item.getComponents().get(DataComponentTypes.ENCHANTABLE);
        int enchantability;

        try{
            enchantability = MiscToolEnchantabilities.getEnchantability(item.getItem()).get();
        } catch (NullPointerException e) {
            if(comp != null) {
                enchantability = comp.value();
            } else {
                enchantability = 0;
            }
        }


        return enchantability;

    }

    public ItemStack getproposed(){
        return inventory.getStack(2).copy();
    }

    public ItemStack getInput(){
        return inventory.getStack(0).copy();
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.context.run((world, pos) -> {
            for(int i = 0; i < 2; i++){
                ItemStack stack = inventory.removeStack(i);
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
        return fuel.get();
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


