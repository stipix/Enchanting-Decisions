package stipix.enchanting_decisions.mixin;

import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.ArmorMaterials;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorMaterial.class)
public class ArmorMaterialMixin {

    @Inject(method = "enchantmentValue", at = @At("HEAD"), cancellable = true)
    private void modifiedEnchantability(CallbackInfoReturnable<Integer> cir) {
        ArmorMaterial material = (ArmorMaterial)(Object)this;
        if (material == ArmorMaterials.LEATHER) {
            cir.setReturnValue(32);
        }
        else if (material == ArmorMaterials.COPPER){
            cir.setReturnValue(28);
        }
        else if (material == ArmorMaterials.CHAIN) {
            cir.setReturnValue(32);
        }
        else if (material == ArmorMaterials.IRON) {
            cir.setReturnValue(24);
        }
        else if (material == ArmorMaterials.DIAMOND) {
            cir.setReturnValue(28);
        }
        else if (material == ArmorMaterials.GOLD) {
            cir.setReturnValue(40);
        }
        else if (material == ArmorMaterials.NETHERITE) {
            cir.setReturnValue(24);
        }
        else if (material == ArmorMaterials.ARMADILLO_SCUTE) {
            cir.setReturnValue(24);
        }
    }

}
