
package stipix.enchanting_decisions.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import stipix.enchanting_decisions.CustomEnchantmentScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantingTableBlock.class)
public abstract class EnchantingTableBlockMixin {

    @Inject(method = "createScreenHandlerFactory",
            at = @At(value = "RETURN", ordinal = 0),
            cancellable = true
    )
    public void createScreenHandlerFactoryMixin(BlockState state, World world, BlockPos pos, CallbackInfoReturnable<NamedScreenHandlerFactory> cir, @Local Text text){

        //Return custom factory
        NamedScreenHandlerFactory retrunFactory = null;
        retrunFactory = new SimpleNamedScreenHandlerFactory((syncId, inventory, player) -> {
            return new CustomEnchantmentScreenHandler(syncId, inventory, ScreenHandlerContext.create(world, pos));
        }, text);

        cir.setReturnValue(retrunFactory);
    }
}
