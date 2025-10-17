package stipix.enchanting_decisions;


import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.google.common.collect.Lists;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.EnchantingPhrases;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.InputUtil;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Unique;

@Environment(EnvType.CLIENT)
public class CustomEnchantmentScreen extends HandledScreen<CustomEnchantmentScreenHandler> {
    public CustomEnchantmentScreen(CustomEnchantmentScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        super.backgroundHeight = 169;
        super.backgroundWidth = 193;
        super.titleX = 4;
        super.titleY = 5;
        super.playerInventoryTitleX = 16;
        super.playerInventoryTitleY = this.backgroundHeight - 90;
        scroll = 0;
    }



    private final Random random = Random.create();
    private BookModel BOOK_MODEL;
    public float nextPageAngle;
    public float pageAngle;
    public float approximatePageAngle;
    public float pageRotationSpeed;
    public float nextPageTurningSpeed;
    public float pageTurningSpeed;
    private ItemStack stack = ItemStack.EMPTY;


    private static final Identifier NEWTEXTURE = Identifier.of(EnchantingDecisions.MOD_ID, "textures/gui/enchantinggui.png");
    private static final Identifier NEWBUTTONTEXTURE = Identifier.of(EnchantingDecisions.MOD_ID, "textures/gui/enchantinggui-button-enabled.png");
    private static final Identifier NEWBUTTONTEXTUREDISABLED = Identifier.of(EnchantingDecisions.MOD_ID, "textures/gui/enchantinggui-button-disabled.png");
    private static final Identifier NEWBUTTONTEXTUREHIGHLIGHTED = Identifier.of(EnchantingDecisions.MOD_ID, "textures/gui/enchantinggui-button-highlighted.png");
    private static final Identifier SCROLLBARTEXTURE = Identifier.of(EnchantingDecisions.MOD_ID, "textures/gui/enchantinggui-scroll-bar.png");
    private static final Identifier PIPTEXTUREON = Identifier.of(EnchantingDecisions.MOD_ID, "textures/gui/enchantinggui-pip-on.png");
    private static final Identifier PIPTEXTUREOFF = Identifier.of(EnchantingDecisions.MOD_ID, "textures/gui/enchantinggui-pip-off.png");
    private static final Identifier PIPTEXTURECURSED = Identifier.of(EnchantingDecisions.MOD_ID, "textures/gui/enchantinggui-pip-cursed.png");
//  private static final Identifier LEFTARROWTEXTURE = Identifier.of(EnchantingDecisions.MOD_ID, "textures/gui/left-arrow.png");
//  private static final Identifier LEFTARROWTEXTUREHIGHLIGHTED = Identifier.of(EnchantingDecisions.MOD_ID, "textures/gui/left-arrow-highlighted.png");
//  private static final Identifier RIGHTARROWTEXTURE = Identifier.of(EnchantingDecisions.MOD_ID, "textures/gui/right-arrow.png");
//  private static final Identifier RIGHTARROWTEXTUREHIGHLIGHTED = Identifier.of(EnchantingDecisions.MOD_ID, "textures/gui/right-arrow-highlighted.png");

    private static final Identifier BOOK_TEXTURE = Identifier.ofVanilla("textures/entity/enchanting_table_book.png");

    private static int scroll = 0;

    private final int boxHeight = 16;

    @Unique
    private boolean dragging = false;

    @Override
    protected void init() {
        super.init();
        assert this.client != null;
        this.BOOK_MODEL = new BookModel(this.client.getLoadedEntityModels().getModelPart(EntityModelLayers.BOOK));
    }

    @Override
    public void handledScreenTick() {
        super.handledScreenTick();
        assert Objects.requireNonNull(this.client).player != null;
        this.client.player.experienceBarDisplayStartTime = this.client.player.age;
        this.doTick();
    }

    @Override
    public void drawBackground(DrawContext context, float deltaTicks, int mouseX, int mouseY) {
        int x = (super.width - super.backgroundWidth) / 2;
        int y = (super.height - super.backgroundHeight) / 2;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, NEWTEXTURE, x, y, 0.0F, 0.0F, super.backgroundWidth, super.backgroundHeight, 193, 169);
        this.drawBook(context, x-10, y);
        int k = super.handler.getLapisCount();
        context.drawTexture(RenderPipelines.GUI_TEXTURED, SCROLLBARTEXTURE,x + 133, y+4+(scroll*49)/192, 0, 0, 12, 15, 12, 15);
        context.enableScissor(x+50, y+5,x+50+70,y+4+16*4);

        assert super.client != null;
        assert super.client.world != null;
        Registry<Enchantment> EnchantRegistry =  super.client.world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        //Iterate over all 16 buttons
        for(int i = 0; i < 16; i++){
            //access custom mixin variables
            //only check for buttons that can be rendered
            if((boxHeight*i - scroll >= boxHeight*-1) && (boxHeight*i - scroll < boxHeight*5) ){

                int enchantmentID = handler.getEnchants()[i];
                int enchantmentMaxTier = handler.getEnchantsTier()[i];
                int selectedTier = handler.getSelectedTier()[i];
                //chack for if there is an enchantment to display
                if( enchantmentID != -1){

                    //adaptively change drawn texture if the user is hovering over it
                    Identifier curBox;
                    if(mouseY - y -4 >= boxHeight*i-scroll && mouseY - y -4 < boxHeight*(i+1)-scroll
                            && mouseX - x >= 50 && mouseX - x < 120){
                        curBox = NEWBUTTONTEXTUREHIGHLIGHTED;
                    } else {
                        curBox = NEWBUTTONTEXTURE;
                    }
                    context.drawTexture(RenderPipelines.GUI_TEXTURED,
                            curBox,
                            x + 50,
                            y + 4 + boxHeight * i - scroll,
                            0, 0,
                            70, 16,
                            70, 16);

                    for(int j = 0; j < enchantmentMaxTier; j++){
                        Identifier curPip;
                        if(selectedTier > j){
                            if(EnchantRegistry.getEntry(EnchantRegistry.get(enchantmentID)).isIn(EnchantmentTags.CURSE)){
                                curPip = PIPTEXTURECURSED;
                            } else {
                                curPip = PIPTEXTUREON;
                            }
                        }else {
                            curPip = PIPTEXTUREOFF;
                        }

                        context.drawTexture(RenderPipelines.GUI_TEXTURED,
                                curPip,
                                x + 52 + j*5,
                                y + 15 + boxHeight * i - scroll,
                                0, 0,
                                4, 4,
                                4, 4);
                    }

                    //massive process just to get the enchantment as text in the user's language and render it
                    String enchantString = EnchantRegistry.getEntry(EnchantRegistry.get(enchantmentID)).getIdAsString();
                    enchantString = enchantString.replaceFirst("minecraft:", "");
                    enchantString = "enchantment.minecraft.".concat(enchantString);
                    Text enchantText = Text.translatable(enchantString);
                    enchantString = enchantText.asTruncatedString(11);
                    if(!Objects.equals(enchantString, enchantText.getString())){
                        enchantString = enchantString.concat("...");
                    }
                    context.drawText(this.textRenderer, enchantString, x+52, y+7+boxHeight*i-scroll,ColorHelper.fullAlpha((-9937334 & 16711422)), false);


                } else {

                    context.drawTexture(RenderPipelines.GUI_TEXTURED, NEWBUTTONTEXTUREDISABLED, x + 50, y + 4 + boxHeight * i - scroll, 0, 0, 70, 16, 70, 16);
                }
            }
        }

        context.disableScissor();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        float f = this.client.getRenderTickCounter().getTickProgress(false);
        super.render(context, mouseX, mouseY, f);
        this.drawMouseoverTooltip(context, mouseX, mouseY);

    }

    private void drawBook(DrawContext context, int x, int y) {
        assert this.client != null;
        float f = this.client.getRenderTickCounter().getTickProgress(false);
        float g = MathHelper.lerp(f, this.pageTurningSpeed, this.nextPageTurningSpeed);
        float h = MathHelper.lerp(f, this.pageAngle, this.nextPageAngle);
        int i = x + 14;
        int j = y + 14;
        int k = i + 38;
        int l = j + 31;
        context.addBookModel(this.BOOK_MODEL, BOOK_TEXTURE, 40.0F, g, h, i, j, k, l);
    }
    @Override
        public boolean mouseClicked(Click click, boolean doubled){
            int x = (super.width - super.backgroundWidth) / 2;
            int y = (super.height - super.backgroundHeight) / 2;
            for (int i = 0; i < 16; i++){

                if(click.y() - y -4 >= boxHeight*i-scroll && click.y() - y -4 < boxHeight*(i+1)-scroll){
                    int enchantID = handler.getEnchants()[i];
                    if(click.x() - x > 50 && click.x() - x < 120 && enchantID != -1){
                        assert this.client != null;
                        int addsub = 0;
                        if(click.button() == InputUtil.GLFW_MOUSE_BUTTON_LEFT){
                            addsub = 2;
                        }else if(click.button() == InputUtil.GLFW_MOUSE_BUTTON_RIGHT){
                            assert super.client.world != null;
                            Registry<Enchantment> EnchantRegistry =  super.client.world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
                            if(!EnchantRegistry.getEntry(EnchantRegistry.get(enchantID)).isIn(EnchantmentTags.CURSE)){
                                addsub = 1;
                            }
                        }
                        if(this.handler.onButtonClick(this.client.player, (addsub<<16)|i)){
                            if (this.client.interactionManager != null) {
                                this.client.interactionManager.clickButton(this.handler.syncId, i);
                            }
                        }
                    }
                }
            }
        return super.mouseClicked(click, doubled);
    }


    public void doTick() {
        ItemStack itemStack = this.handler.getSlot(0).getStack();
        if (!ItemStack.areEqual(itemStack, this.stack)) {
            this.stack = itemStack;

            do {
                this.approximatePageAngle = this.approximatePageAngle + (this.random.nextInt(4) - this.random.nextInt(4));
            } while (this.nextPageAngle <= this.approximatePageAngle + 1.0F && this.nextPageAngle >= this.approximatePageAngle - 1.0F);
        }

        this.pageAngle = this.nextPageAngle;
        this.pageTurningSpeed = this.nextPageTurningSpeed;
        boolean bl = false;

        for (int i = 0; i < 3; i++) {
            if (this.handler.enchantmentPower[i] != 0) {
                bl = true;
                break;
            }
        }

        if (bl) {
            this.nextPageTurningSpeed += 0.2F;
        } else {
            this.nextPageTurningSpeed -= 0.2F;
        }

        this.nextPageTurningSpeed = MathHelper.clamp(this.nextPageTurningSpeed, 0.0F, 1.0F);
        float f = (this.approximatePageAngle - this.nextPageAngle) * 0.4F;
        float g = 0.2F;
        f = MathHelper.clamp(f, -0.2F, 0.2F);
        this.pageRotationSpeed = this.pageRotationSpeed + (f - this.pageRotationSpeed) * 0.9F;
        this.nextPageAngle = this.nextPageAngle + this.pageRotationSpeed;
    }

    @Override
    public boolean mouseDragged (Click click, double offsetX, double offsetY) {
        int x = (super.width - super.backgroundWidth) / 2;
        int y = (super.height - super.backgroundHeight) / 2;
        if(((click.x() > x + 133) && (click.x() < (x+ 133 + 12)))
                && (click.y()> (y + 4 + ((scroll * 49) / 192))) && (click.y() < (y + 4 + ((scroll * 49) / 192) + 15))
                || (dragging && click.y() >= y+4 && click.y() < y+53) ){
            dragging = true;
            scroll += (int)(offsetY*4);
            if(scroll < 0){scroll = 0;}
            if(scroll > boxHeight*12){scroll = boxHeight*12;}
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override

    public boolean mouseReleased(Click click){
        dragging = false;
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scroll -= (int) (verticalAmount*6);
        if(scroll < 0){scroll = 0;}
        if(scroll > boxHeight*12){scroll = boxHeight*12;}
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}