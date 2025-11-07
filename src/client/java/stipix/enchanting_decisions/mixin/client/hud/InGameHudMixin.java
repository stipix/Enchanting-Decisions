package stipix.enchanting_decisions.mixin.client.hud;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profilers;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin
{


    @Shadow
    @Nullable
    protected abstract PlayerEntity getCameraPlayer();

    @Shadow
    private long heartJumpEndTick;

    @Shadow
    private int ticks;

    @Shadow
    private int lastHealthValue;

    @Shadow
    private long lastHealthCheckTime;

    @Shadow
    private int renderHealthValue;

    @Shadow
    @Final
    private Random random;

    //this is necessary in order to invoke the method later when rendering the HUD, and not allowed to be abstract
    @Shadow
    private static void renderArmor(DrawContext context, PlayerEntity player, int y, int i, int healthBarLines, int x) {
        int j = player.getArmor();
        if (j > 0) {
            int k = y - (i - 1) * healthBarLines - 10;

            for (int l = 0; l < 10; l++) {
                int m = x + l * 8;
                if (l * 2 + 1 < j) {
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, ARMOR_FULL_TEXTURE, m, k, 9, 9);
                }

                if (l * 2 + 1 == j) {
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, ARMOR_HALF_TEXTURE, m, k, 9, 9);
                }

                if (l * 2 + 1 > j) {
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, ARMOR_EMPTY_TEXTURE, m, k, 9, 9);
                }
            }
        }
    }

    @Shadow
    protected abstract void renderHealthBar(DrawContext context, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking);

    @Shadow
    protected abstract int getHeartCount(@Nullable LivingEntity entity);

    @Shadow
    @Nullable
    protected abstract LivingEntity getRiddenEntity();

    @Shadow
    protected abstract void renderFood(DrawContext context, PlayerEntity player, int top, int right);

    @Shadow
    protected abstract void renderAirBubbles(DrawContext context, PlayerEntity player, int heartCount, int top, int left);

    @Shadow
    @Final
    private static Identifier ARMOR_FULL_TEXTURE;

    @Shadow
    @Final
    private static Identifier ARMOR_HALF_TEXTURE;

    @Shadow
    @Final
    private static Identifier ARMOR_EMPTY_TEXTURE;

    //moving the health/armor/hunger/air bars down as if the XP bar never existed
    @Inject(method = "renderStatusBars", at = @At("HEAD"), cancellable = true)
    private void renderStatusBars(DrawContext context, CallbackInfo ci){
        int heightOffset = 32; //was 39
        PlayerEntity playerEntity = this.getCameraPlayer();
        if (playerEntity != null) {
            if(playerEntity.getVehicle()!=null) //in case the player is riding something like a horse where they need the bar
            {
                heightOffset=39; //shifting the offset to account for mount status bar
            }

            int i = MathHelper.ceil(playerEntity.getHealth());
            boolean bl = this.heartJumpEndTick > this.ticks && (this.heartJumpEndTick - this.ticks) / 3L % 2L == 1L;
            long l = Util.getMeasuringTimeMs();
            if (i < this.lastHealthValue && playerEntity.timeUntilRegen > 0) {
                this.lastHealthCheckTime = l;
                this.heartJumpEndTick = this.ticks + 20;
            } else if (i > this.lastHealthValue && playerEntity.timeUntilRegen > 0) {
                this.lastHealthCheckTime = l;
                this.heartJumpEndTick = this.ticks + 10;
            }

            if (l - this.lastHealthCheckTime > 1000L) {
                this.renderHealthValue = i;
                this.lastHealthCheckTime = l;
            }

            this.lastHealthValue = i;
            int j = this.renderHealthValue;
            this.random.setSeed(this.ticks * 312871L);
            int k = context.getScaledWindowWidth() / 2 - 91;
            int m = context.getScaledWindowWidth() / 2 + 91;
            int n = context.getScaledWindowHeight() - heightOffset;
            float f = Math.max((float)playerEntity.getAttributeValue(EntityAttributes.MAX_HEALTH), Math.max(j, i));
            int o = MathHelper.ceil(playerEntity.getAbsorptionAmount());
            int p = MathHelper.ceil((f + o) / 2.0F / 10.0F);
            int q = Math.max(10 - (p - 2), 3);
            int r = n - 10;
            int s = -1;
            if (playerEntity.hasStatusEffect(StatusEffects.REGENERATION)) {
                s = this.ticks % MathHelper.ceil(f + 5.0F);
            }

            Profilers.get().push("armor");
            renderArmor(context, playerEntity, n, p, q, k);
            Profilers.get().swap("health");
            this.renderHealthBar(context, playerEntity, k, n, q, s, f, i, j, o, bl);
            LivingEntity livingEntity = this.getRiddenEntity();
            int t = this.getHeartCount(livingEntity);
            if (t == 0) {
                Profilers.get().swap("food");
                this.renderFood(context, playerEntity, n, m);
                r -= 10;
            }

            Profilers.get().swap("air");
            this.renderAirBubbles(context, playerEntity, t, r, m);
            Profilers.get().pop();
        }

        ci.cancel();
    }
}