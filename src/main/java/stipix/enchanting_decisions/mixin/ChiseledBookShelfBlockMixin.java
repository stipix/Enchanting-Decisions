package stipix.enchanting_decisions.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.ChiseledBookshelfBlock;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.block.entity.EnchantingTableBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import stipix.enchanting_decisions.ModCriteria;
import stipix.enchanting_decisions.PowerUpCriterion;

@Mixin(ChiseledBookshelfBlock.class)
public class ChiseledBookShelfBlockMixin {
    @Inject(method = "onUseWithItem", at = @At("HEAD"))
    public void onUseWithItemMixin(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir){
        if(stack.isOf(Items.ENCHANTED_BOOK) && player instanceof ServerPlayerEntity) {
            for (BlockPos blockPos : EnchantingTableBlock.POWER_PROVIDER_OFFSETS) {
                if (world.getBlockEntity(blockPos.add(pos)) instanceof EnchantingTableBlockEntity) {
                    ModCriteria.POWER_UP_CRITERION.trigger((ServerPlayerEntity) player);
                }
            }
        }
    }
}
