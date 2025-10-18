package name.modid.mixin;

import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ToolMaterial.class)
public abstract class ToolMaterialMixin {
    @Shadow                                                                                     //  59     2.0F     0.0F     15
    public static final ToolMaterial WOOD = new ToolMaterial(BlockTags.INCORRECT_FOR_WOODEN_TOOL, 59, 2.0F, 0.0F, 32, ItemTags.WOODEN_TOOL_MATERIALS);
    @Shadow                                                                                     //  131     4.0F     1.0F    5
    public static final ToolMaterial STONE = new ToolMaterial(BlockTags.INCORRECT_FOR_STONE_TOOL, 131, 4.0F, 1.0F, 24, ItemTags.STONE_TOOL_MATERIALS);
    @Shadow                                                                                     //    190     5.0F     1.0F    13
    public static final ToolMaterial COPPER = new ToolMaterial(BlockTags.INCORRECT_FOR_COPPER_TOOL, 190, 5.0F, 1.0F, 32, ItemTags.COPPER_TOOL_MATERIALS);
    @Shadow                                                                                     //250     6.0F     2.0F    14
    public static final ToolMaterial IRON = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 250, 6.0F, 2.0F, 28, ItemTags.IRON_TOOL_MATERIALS);
    @Shadow                                                                                     //      1561     8.0F     3.0F     10
    public static final ToolMaterial DIAMOND = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1241, 8.0F, 3.0F, 28, ItemTags.DIAMOND_TOOL_MATERIALS);
    @Shadow                                                                                     // 32    12.0F     0.0F     22
    public static final ToolMaterial GOLD = new ToolMaterial(BlockTags.INCORRECT_FOR_GOLD_TOOL, 49, 12.0F, 0.0F, 40, ItemTags.GOLD_TOOL_MATERIALS);
    @Shadow                                                                                     //          2031     9.0F     4.0F     15
    public static final ToolMaterial NETHERITE = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2351, 9.0F, 4.0F, 24, ItemTags.NETHERITE_TOOL_MATERIALS);

}
