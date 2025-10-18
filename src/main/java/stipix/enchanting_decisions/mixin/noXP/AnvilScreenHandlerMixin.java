package name.modid.mixin.noXP;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
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
import net.minecraft.screen.*;
import net.minecraft.screen.slot.ForgingSlotsManager;
import net.minecraft.text.Text;
import net.minecraft.util.StringHelper;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {

    @Shadow
    private boolean keepSecondSlot;

    @Shadow
    @Final
    private Property levelCost;

    @Shadow
    private int repairItemUsage;

    @Shadow
    private @Nullable String newItemName;

    public AnvilScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, ForgingSlotsManager forgingSlotsManager) {
        super(type, syncId, playerInventory, context, forgingSlotsManager);
    }

    //removes XP scaling from previous work cost
    @Inject(method = "getNextCost", at = @At("HEAD"), cancellable = true)
    private static void getNextCost(int cost, CallbackInfoReturnable<Integer> cir){
        cir.setReturnValue(0);
        cir.cancel();
    }

    //removes basic level cost for operating on tools
    @Inject(method = "getLevelCost", at = @At("HEAD"), cancellable = true)
    public void getLevelCost(CallbackInfoReturnable<Integer> cir){
        cir.setReturnValue(0);
        cir.cancel();
    }

    //removes the level restrictions that disable the ability to take the output
    @Inject(method = "canTakeOutput", at = @At("HEAD"), cancellable = true)
    protected void canTakeOutput(PlayerEntity player, boolean present, CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(true);
        cir.cancel();
    }

    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    public void updateResult(CallbackInfo ci){
        ItemStack itemStack = this.input.getStack(0);
        this.keepSecondSlot = false;
        this.levelCost.set(0);
        int i = 0;
        long l = 0L;
        int j = 0;
        if (!itemStack.isEmpty() && EnchantmentHelper.canHaveEnchantments(itemStack)) {
            ItemStack itemStack2 = itemStack.copy();
            ItemStack itemStack3 = this.input.getStack(1);
            ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(EnchantmentHelper.getEnchantments(itemStack2));
            l += (long)itemStack.getOrDefault(DataComponentTypes.REPAIR_COST, 0).intValue() + itemStack3.getOrDefault(DataComponentTypes.REPAIR_COST, 0).intValue();
            this.repairItemUsage = 0;
            if (!itemStack3.isEmpty()) {
                boolean bl = itemStack3.contains(DataComponentTypes.STORED_ENCHANTMENTS);
                if (itemStack2.isDamageable() && itemStack.canRepairWith(itemStack3)) {


                    float mendingBonus=0;
                    for(RegistryEntry<Enchantment> e : itemStack2.getEnchantments().getEnchantments()){
                        if(e.matchesKey(Enchantments.MENDING)){
                            mendingBonus = 0.09f*itemStack2.getEnchantments().getLevel(e);
                        }
                    }
                    //k is the lesser durability between 1/4 dur and current amount
                    int k = Math.min(itemStack2.getDamage(), (int)(itemStack2.getMaxDamage()*(mendingBonus +0.3f)));

                    if (k <= 0) {
                        this.output.setStack(0, ItemStack.EMPTY);
                        this.levelCost.set(0);
                        return;
                    }

                    int m;
                    for (m = 0; k > 0 && m < itemStack3.getCount(); m++) {
                        int n = itemStack2.getDamage() - k;
                        itemStack2.setDamage(n);
                        i++;
                        //change this too
                        k = Math.min(itemStack2.getDamage(), (int)(itemStack2.getMaxDamage()*0.3f));
                    }

                    this.repairItemUsage = m;
                } else {
                    if (!bl && (!itemStack2.isOf(itemStack3.getItem()) || !itemStack2.isDamageable())) {
                        this.output.setStack(0, ItemStack.EMPTY);
                        this.levelCost.set(0);
                        return;
                    }

                    if (itemStack2.isDamageable() && !bl) {
                        int kx = itemStack.getMaxDamage() - itemStack.getDamage();
                        int m = itemStack3.getMaxDamage() - itemStack3.getDamage();
                        int n = m + itemStack2.getMaxDamage() * 12 / 100;
                        int o = kx + n;
                        int p = itemStack2.getMaxDamage() - o;
                        if (p < 0) {
                            p = 0;
                        }

                        if (p < itemStack2.getDamage()) {
                            itemStack2.setDamage(p);
                            i += 2;
                        }
                    }

                    ItemEnchantmentsComponent itemEnchantmentsComponent = EnchantmentHelper.getEnchantments(itemStack3);
                    boolean bl2 = false;
                    boolean bl3 = false;

                    for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : itemEnchantmentsComponent.getEnchantmentEntries()) {
                        RegistryEntry<Enchantment> registryEntry = (RegistryEntry<Enchantment>)entry.getKey();
                        int q = builder.getLevel(registryEntry);
                        int r = entry.getIntValue();
                        r = q == r ? r + 1 : Math.max(r, q);
                        Enchantment enchantment = registryEntry.value();
                        boolean bl4 = enchantment.isAcceptableItem(itemStack);
                        if (this.player.isInCreativeMode() || itemStack.isOf(Items.ENCHANTED_BOOK)) {
                            bl4 = true;
                        }

                        for (RegistryEntry<Enchantment> registryEntry2 : builder.getEnchantments()) {
                            if (!registryEntry2.equals(registryEntry) && !Enchantment.canBeCombined(registryEntry, registryEntry2)) {
                                bl4 = false;
                                i++;
                            }
                        }

                        if (!bl4) {
                            bl3 = true;
                        } else {
                            bl2 = true;
                            if (r > enchantment.getMaxLevel()) {
                                r = enchantment.getMaxLevel();
                            }

                            builder.set(registryEntry, r);
                            int s = enchantment.getAnvilCost();
                            if (bl) {
                                s = Math.max(1, s / 2);
                            }

                            i += s * r;
                            if (itemStack.getCount() > 1) {
                                i = 40;
                            }
                        }
                    }

                    if (bl3 && !bl2) {
                        this.output.setStack(0, ItemStack.EMPTY);
                        this.levelCost.set(0);
                        return;
                    }
                }
            }

            if (this.newItemName != null && !StringHelper.isBlank(this.newItemName)) {
                if (!this.newItemName.equals(itemStack.getName().getString())) {
                    j = 1;
                    i += j;
                    itemStack2.set(DataComponentTypes.CUSTOM_NAME, Text.literal(this.newItemName));
                }
            } else if (itemStack.contains(DataComponentTypes.CUSTOM_NAME)) {
                j = 1;
                i += j;
                itemStack2.remove(DataComponentTypes.CUSTOM_NAME);
            }

            int t = i <= 0 ? 0 : (int) MathHelper.clamp(l + i, 0L, 2147483647L);
            this.levelCost.set(t);
            if (i <= 0) {
                itemStack2 = ItemStack.EMPTY;
            }

            if (j == i && j > 0) {
                if (this.levelCost.get() >= 40) {
                    this.levelCost.set(39);
                }

                this.keepSecondSlot = true;
            }

            if (this.levelCost.get() >= 40 && !this.player.isInCreativeMode()) {
                itemStack2 = ItemStack.EMPTY;
            }

            if (!itemStack2.isEmpty()) {
                int kxx = itemStack2.getOrDefault(DataComponentTypes.REPAIR_COST, 0);
                if (kxx < itemStack3.getOrDefault(DataComponentTypes.REPAIR_COST, 0)) {
                    kxx = itemStack3.getOrDefault(DataComponentTypes.REPAIR_COST, 0);
                }
                /*
                if (j != i || j == 0) {
                    kxx = getNextCost(kxx, 1);
                }
                */
                itemStack2.set(DataComponentTypes.REPAIR_COST, kxx);
                EnchantmentHelper.set(itemStack2, builder.build());
            }

            this.output.setStack(0, itemStack2);
            this.sendContentUpdates();
        } else {
            this.output.setStack(0, ItemStack.EMPTY);
            this.levelCost.set(0);
        }
        ci.cancel();
    }

/*
    @Redirect(
            method = "getNextCost", at = @At(
                    value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;addExperienceLevels(I)V"))
    private static int getNextCost(int cost) {
        return (int)Math.min(cost * 2L + 1L, 2147483647L);
    }

 */
}
