
package stipix.enchanting_decisions.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChiseledBookshelfBlockEntity;
import net.minecraft.block.entity.EnchantingTableBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.injection.Redirect;
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
import stipix.enchanting_decisions.ModCriteria;

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

    @Redirect(method = "randomDisplayTick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/EnchantingTableBlock;canAccessPowerProvider(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Z")
    )
    public boolean randomDisplayTickRedirect(World world, BlockPos tablePos, BlockPos providerOffset){
        if(world.getBlockState(tablePos.add(providerOffset)).isOf(Blocks.CHISELED_BOOKSHELF)
                && world.getBlockState(tablePos.add(providerOffset.getX() / 2, providerOffset.getY(), providerOffset.getZ() / 2))
                .isIn(BlockTags.ENCHANTMENT_POWER_TRANSMITTER)){
            //get the bookshelf inventory
            BlockEntity inventory = world.getBlockEntity(tablePos.add(providerOffset));
            if(inventory instanceof ChiseledBookshelfBlockEntity) {
                DefaultedList<ItemStack> books = ((ChiseledBookshelfBlockEntity) inventory).getHeldStacks();
                for(ItemStack book : books){
                    if(book.getItem() == Items.BOOK || book.getItem() == Items.ENCHANTED_BOOK){
                        return true;
                    }
                }
                return false;
            }
        }
        return false;
    }
}
