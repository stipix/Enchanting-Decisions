package stipix.enchanting_decisions.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.EnchantingTableBlockEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import stipix.enchanting_decisions.EtableFuelLevelInterface;

@Mixin(EnchantingTableBlockEntity.class)
public abstract class EnchantingTableBlockEntityMixin extends BlockEntity implements EtableFuelLevelInterface {
    @Unique
    private static int fuelLevel;

    public EnchantingTableBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    @Inject(method = "writeData", at = @At("TAIL"))
    private void writeData(WriteView view, CallbackInfo ci) {
        view.putInt("fuelLevel", fuelLevel);
    }

    @Inject(method = "readData", at= @At("TAIL"))
    private void readData(ReadView view, CallbackInfo ci) {
        fuelLevel = view.getInt("fuelLevel", 0);
    }

    public int enchantingDecisions$getFuelLevel(){
        return fuelLevel;
    }
    public void enchantingDecisions$setFuelLevel(int fuelLevel){
        EnchantingTableBlockEntityMixin.fuelLevel = fuelLevel;
        markDirty();
    }

}
