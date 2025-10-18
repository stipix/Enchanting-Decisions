package stipix.enchanting_decisions.mixin.client.hud;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.bar.Bar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Bar.class)
public interface RemoveXPLevelIndicator {

        //removes the XP level HUD indicator
        @Inject(method = "drawExperienceLevel", at = @At("HEAD"),cancellable = true)
        private static void drawExperienceLevel(DrawContext context, TextRenderer textRenderer, int level, CallbackInfo ci){
            ci.cancel();
        }
}
