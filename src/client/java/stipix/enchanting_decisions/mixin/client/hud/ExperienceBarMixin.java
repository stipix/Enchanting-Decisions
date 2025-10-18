package stipix.enchanting_decisions.mixin.client.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.bar.ExperienceBar;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceBar.class)
public abstract class ExperienceBarMixin {

    //this removes the XP bar's appearance
    @Inject(method = "renderBar", at = @At("HEAD"), cancellable = true)
    public void renderBar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci){
        ci.cancel();
    }

}
