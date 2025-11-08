package stipix.enchanting_decisions.mixin.client.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.bar.ExperienceBar;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceBar.class)
public abstract class ExperienceBarMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    //this removes the XP bar's appearance
    @Inject(method = "renderBar", at = @At("HEAD"), cancellable = true)
    public void renderBar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci){
        ClientPlayerEntity clientPlayerEntity = this.client.player;
        ci.cancel();

    }

}
