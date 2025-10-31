package stipix.enchanting_decisions.mixin;

import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.RepairableComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class MiscToolRepairMixin {

    @Shadow
    public abstract Item getItem();

    @Shadow
    public abstract ComponentMap getComponents();

    @Shadow
    public abstract boolean isOf(Item item);

    @Inject(method = "canRepairWith", at = @At("RETURN"), cancellable = true)
    public void modifiedRepair(ItemStack ingredient, CallbackInfoReturnable<Boolean> cir) {
        //RepairableComponent repairableComponent = this.getComponents().get(DataComponentTypes.REPAIRABLE);
        if (
                (this.isOf(Items.BOW) && ingredient.isOf(Items.STRING)) ||
                (this.isOf(Items.CROSSBOW) && ingredient.isOf(Items.STRING)) ||
                (this.isOf(Items.SHEARS) && ingredient.isOf(Items.IRON_INGOT)) ||
                (this.isOf(Items.FLINT_AND_STEEL) && ingredient.isOf(Items.FLINT)) ||
                (this.isOf(Items.FISHING_ROD) && ingredient.isOf(Items.STRING)) ||
                (this.isOf(Items.SHIELD) && ingredient.isOf(Items.IRON_INGOT)) ||
                (this.isOf(Items.MACE) && ingredient.isOf(Items.IRON_BLOCK)) ||
                (this.isOf(Items.TRIDENT) && ingredient.isOf(Items.PRISMARINE_CRYSTALS))
        ){
            cir.setReturnValue(true);
            cir.cancel();
        }
        //cir.setReturnValue(repairableComponent != null && repairableComponent.matches(ingredient));
    }

}
