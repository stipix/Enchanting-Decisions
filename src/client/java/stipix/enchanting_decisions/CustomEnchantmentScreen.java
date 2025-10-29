package stipix.enchanting_decisions;


import java.util.Arrays;
import java.util.Objects;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.InputUtil;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Unique;

@Environment(EnvType.CLIENT)
public class CustomEnchantmentScreen extends HandledScreen<CustomEnchantmentScreenHandler> {



    private static class ImageHolder {
        public final Identifier[] PNG;
        public final int width;
        public final  int height;
        public final  int x;
        public final  int y;
        public final  int rightX;
        public final  int bottomY;
        public ImageHolder(Identifier[] PNG, int width, int height, int x, int y) {
            this.PNG = PNG;
            this.width = width;
            this.height = height;
            this.x = x;
            this.y = y;
            this.rightX = x+width;
            this.bottomY = y+height;
        }

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

    ImageHolder background = new ImageHolder(
            new Identifier[]{Identifier.of(EnchantingDecisions.MOD_ID, "textures/gui/enchantinggui.png")},
            180,
            160,
            0,
            0
    );

    ImageHolder button = new ImageHolder(
            new Identifier[]{
                    Identifier.of(EnchantingDecisions.MOD_ID, "textures/gui/enchantinggui-button-enabled.png"),
                    Identifier.of(EnchantingDecisions.MOD_ID, "textures/gui/enchantinggui-button-disabled.png"),
                    Identifier.of(EnchantingDecisions.MOD_ID, "textures/gui/enchantinggui-button-highlighted.png")
            },
            70,
            16,
            50,
            4
    );

    ImageHolder pip = new ImageHolder(
            new Identifier[]{
                    Identifier.of(EnchantingDecisions.MOD_ID, "textures/gui/enchantinggui-pip-on.png"),
                    Identifier.of(EnchantingDecisions.MOD_ID, "textures/gui/enchantinggui-pip-off.png"),
                    Identifier.of(EnchantingDecisions.MOD_ID, "textures/gui/enchantinggui-pip-cursed.png")
            },
            4,
            4,
            button.x + 2,
            button.y + 11
    );

    ImageHolder scrollBar = new ImageHolder(
            new Identifier[]{
                    Identifier.of(EnchantingDecisions.MOD_ID, "textures/gui/enchantinggui-scroll-bar.png")
            },
            9,
            16,
            124,
            4
    );

    ImageHolder leftRight = new ImageHolder(
            new Identifier[]{
                    Identifier.of(EnchantingDecisions.MOD_ID, "textures/gui/enchantinggui-leftright-on.png"),
                    Identifier.of(EnchantingDecisions.MOD_ID, "textures/gui/enchantinggui-leftright-leftoff.png"),
                    Identifier.of(EnchantingDecisions.MOD_ID, "textures/gui/enchantinggui-leftright-rightoff.png")
            },
            14,
            5,
            button.x + 54,
            button.y + 10
    );
    private static final Identifier BOOK_TEXTURE = Identifier.ofVanilla("textures/entity/enchanting_table_book.png");

    private static int scroll = 0;
    private static int[] lefttRightState = new  int[16];

    @Unique
    private boolean dragging = false;

    public CustomEnchantmentScreen(CustomEnchantmentScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        super.backgroundHeight = background.height;
        super.backgroundWidth = background.width;
        super.titleX = 5;
        super.titleY = 5;
        super.playerInventoryTitleX = 10;
        super.playerInventoryTitleY = this.backgroundHeight - 80;
        scroll = 0;
        Arrays.fill(lefttRightState, 0);
    }

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
        int leftmost = (super.width - super.backgroundWidth) / 2;
        int topmost = (super.height - super.backgroundHeight) / 2;
        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                background.PNG[0],
                leftmost+background.x,
                topmost+background.y,
                0.0F,
                0.0F,
                super.backgroundWidth,
                super.backgroundHeight,
                background.width,
                background.height
        );

        this.drawBook(context, leftmost -10, topmost);

        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                scrollBar.PNG[0],
                leftmost + scrollBar.x,
                topmost +scrollBar.y+(scroll/4),
                0,
                0,
                scrollBar.width,
                scrollBar.height,
                scrollBar.width,
                scrollBar.height);

        context.enableScissor(leftmost + button.x, topmost + button.y, leftmost + button.rightX, topmost+ button.y+ button.height*4);

        assert super.client != null;
        assert super.client.world != null;
        Registry<Enchantment> EnchantRegistry =  super.client.world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);

        //Iterate over all 16 buttons
        for(int i = 0; i < 16; i++){
            //access custom mixin variables
            //only check for buttons that can be rendered
            if( !((button.height*i - scroll >= button.height*-1) &&
                (button.height*i - scroll < button.height*5)) ) {
                continue;
            }
            int enchantmentID = handler.getEnchants()[i];
            int enchantmentMaxTier = handler.getEnchantsTier()[i];
            int selectedTier = handler.getSelectedTier()[i];

            //chack for if there is an enchantment to display
            if( enchantmentID == -1){

                context.drawTexture(
                        RenderPipelines.GUI_TEXTURED,
                        button.PNG[1],
                        leftmost + button.x,
                        topmost + button.y + button.height * i - scroll,
                        0, 0,
                        button.width, button.height,
                        button.width, button.height);
                continue;
            }


            //adaptively change drawn texture if the user is hovering over it
            Identifier curBox;
            if(
                mouseInBounds(
                    mouseX-leftmost, mouseY-topmost,
                    button.x,
                    button.rightX,
                    button.y+button.height*i - scroll,
                    button.bottomY+button.height*i - scroll
                )
                &&
                mouseInBounds(
                    mouseX-leftmost, mouseY-topmost,
                    button.x,
                    button.rightX,
                    button.y,
                    button.y +button.height*4
                )
            ){
                curBox = button.PNG[2];
            } else {
                curBox = button.PNG[0];
            }

            context.drawTexture(RenderPipelines.GUI_TEXTURED,
                    curBox,
                    leftmost + button.x,
                    topmost + button.y + button.height * i - scroll,
                    0, 0,
                    button.width, button.height,
                    button.width, button.height);

            context.drawTexture(RenderPipelines.GUI_TEXTURED,
                    leftRight.PNG[lefttRightState[i]],
                    leftmost + leftRight.x,
                    topmost + leftRight.y + button.height * i - scroll,
                    0, 0,
                    leftRight.width, leftRight.height,
                    leftRight.width, leftRight.height);

            for(int j = 0; j < Math.max(enchantmentMaxTier, selectedTier); j++){
                Identifier curPip;
                if(selectedTier > j){
                    if(EnchantRegistry.getEntry(EnchantRegistry.get(enchantmentID)).isIn(EnchantmentTags.CURSE)
                    ||  selectedTier > enchantmentMaxTier  && enchantmentMaxTier <= j){
                        curPip = pip.PNG[2];//cursed
                    } else {
                        curPip = pip.PNG[0];//on
                    }
                }else{
                    curPip = pip.PNG[1];//off
                }

                context.drawTexture(RenderPipelines.GUI_TEXTURED,
                        curPip,
                        leftmost + pip.x + j*(pip.width +1),
                        topmost + pip.y + button.height * i - scroll,
                        0, 0,
                        pip.width, pip.height,
                        pip.width, pip.height);
            }

            //massive process just to get the enchantment as text in the user's language and render it
//            String enchantString = EnchantRegistry.getEntry(EnchantRegistry.get(enchantmentID)).getIdAsString();
//            enchantString = enchantString.replaceFirst("minecraft:", "");
//            enchantString = "enchantment.minecraft.".concat(enchantString);
//            Text enchantText = Text.translatable(enchantString);
//            enchantString = enchantText.asTruncatedString(11);
//            if(!Objects.equals(enchantString, enchantText.getString())){
//                enchantString = enchantString.concat("...");
//            }

            String string = Objects.requireNonNull(EnchantRegistry.getId(EnchantRegistry.get(enchantmentID))).toShortTranslationKey();
            Text text = Text.translatable(string);
            string = text.asTruncatedString(11);
            if(!Objects.equals(string, text.getString())){
                string = string.concat("...");
            }
            context.drawText(
                    this.textRenderer,
                    string,
                    leftmost +52, topmost +7+ button.height*i-scroll,
                    ColorHelper.fullAlpha((-9937334 & 16711422)), false
            );

        }

        context.disableScissor();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        assert this.client != null;
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
            int leftmost = (super.width - super.backgroundWidth) / 2;
            int topmost = (super.height - super.backgroundHeight) / 2;

            if(mouseInBounds((int)click.x()-leftmost,(int)click.y()-topmost,
                    button.x, button.x+ button.width,
                    button.y, button.y+button.height*4)){

                for (int i = 0; i < 16; i++) {
                    if(mouseInBounds(
                            (int)click.x()-leftmost, (int)click.y()-topmost- button.y,
                            button.x, button.x+button.width,
                            button.height*i-scroll,button.height*(i+1)-scroll
                        )
                    ){
                        int enchantID = handler.getEnchants()[i];
                        if (click.x() - leftmost > button.x && click.x() - leftmost < button.rightX && enchantID != -1) {
                            assert this.client != null;
                            int addsub = 0;
                            if(mouseInBounds(
                                    (int)click.x()-leftmost, (int)click.y()-topmost,
                                    leftRight.x, leftRight.x + leftRight.width/2,
                                    button.height*i-scroll+leftRight.y,button.height*(i)-scroll+leftRight.bottomY)
                            ){
                                lefttRightState[i] = 1;
                                addsub = 1;
                            } else if(mouseInBounds(
                                    (int)click.x()-leftmost, (int)click.y()-topmost,
                                    leftRight.x + leftRight.width/2, leftRight.rightX,
                                    button.height*i-scroll+leftRight.y,button.height*(i)-scroll+leftRight.bottomY)
                            ){
                                lefttRightState[i] = 2;
                                addsub = 2;
                            } else {
                                if (click.button() == InputUtil.GLFW_MOUSE_BUTTON_LEFT) {

                                    addsub = 2;
                                } else if (click.button() == InputUtil.GLFW_MOUSE_BUTTON_RIGHT) {
                                    addsub = 1;
                                }
                            }
                            if (this.handler.onButtonClick(this.client.player, (addsub << 16) | i)) {
                                if (this.client.interactionManager != null) {
                                    this.client.interactionManager.clickButton(this.handler.syncId, (addsub << 16) | i);
                                }
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
        int leftmost = (super.width - super.backgroundWidth) / 2;
        int topmost = (super.height - super.backgroundHeight) / 2;

        if(mouseInBounds(
                (int)click.x()-leftmost,
                (int)click.y()-topmost,
                scrollBar.x,
                scrollBar.x+scrollBar.width,
                scrollBar.y+scroll/4,
                scrollBar.y+scrollBar.height+scroll/4
            )
            ||
            (dragging && mouseInBounds(
                (int)click.x()-leftmost,
                (int)click.y()-topmost,
                0, background.width,
                0, background.height
                )
            )
        ){
            dragging = true;
            scroll += (int) (offsetY*4);
            scroll = Math.clamp(scroll, 0, button.height*(16-4));//subtract 4 for the 4 buttons displayed at any time
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override

    public boolean mouseReleased(Click click){
        dragging = false;
        Arrays.fill(lefttRightState, 0);
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scroll -= (int) (verticalAmount*6);
        scroll = Math.clamp(scroll, 0, button.height*(16-4));//subtract 4 for the 4 buttons displayed at any time
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private boolean mouseInBounds(int mouseX, int mouseY, int left, int right, int top, int bottom) {
        return (mouseX > left && mouseX < right && mouseY > top && mouseY < bottom );
    }
}
