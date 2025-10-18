package name.modid.mixin.noXP;

import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrbEntity.class)
public class ExperienceOrbEntityMixin {

    @Inject(method = "spawn(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/Vec3d;I)V", at = @At("HEAD"),cancellable = true)
    private static void spawn(ServerWorld world, Vec3d pos, int amount, CallbackInfo ci){
        ci.cancel();
    }

    @Inject(method = "spawn(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;I)V", at = @At("HEAD"),cancellable = true)
    private static void spawn(ServerWorld world, Vec3d pos, Vec3d velocity, int amount, CallbackInfo ci){
        ci.cancel();
    }
}
