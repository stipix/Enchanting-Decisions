package stipix.enchanting_decisions.mixin;

import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import stipix.enchanting_decisions.CustomAnvilScreenHandler;

@Mixin(AnvilBlock.class)
public class AnvilBlockMixin {

    @Shadow
    @Final
    private static Text TITLE = Text.translatable("container.repair");

    @Inject(method = "createScreenHandlerFactory", at = @At("HEAD"), cancellable = true)
    protected void createScreenHandlerFactory(BlockState state, World world, BlockPos pos, CallbackInfoReturnable<NamedScreenHandlerFactory> cir) {
        cir.setReturnValue(new SimpleNamedScreenHandlerFactory(
                (syncId, inventory, player) -> new CustomAnvilScreenHandler(syncId, inventory, ScreenHandlerContext.create(world, pos)), TITLE
        ));
        cir.cancel();
    }
}
