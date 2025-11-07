package stipix.enchanting_decisions.mixin;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ProjectileDeflection;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(TridentEntity.class)
public abstract class TridentEntityMixin extends PersistentProjectileEntity {
    @Shadow
    private static final TrackedData<Byte> LOYALTY = DataTracker.registerData(TridentEntityMixin.class, TrackedDataHandlerRegistry.BYTE);
    @Shadow
    private static final TrackedData<Boolean> ENCHANTED = DataTracker.registerData(TridentEntityMixin.class, TrackedDataHandlerRegistry.BOOLEAN);
    @Shadow
    private static final float DRAG_IN_WATER = 0.99F;
    @Shadow
    private static final boolean DEFAULT_DEALT_DAMAGE = false;
    @Shadow
    private boolean dealtDamage = false;
    @Shadow
    public int returnTimer;

    protected TridentEntityMixin(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    protected TridentEntityMixin(EntityType<? extends PersistentProjectileEntity> type, double x, double y, double z, World world, ItemStack stack, @Nullable ItemStack weapon) {
        super(type, x, y, z, world, stack, weapon);
    }

    protected TridentEntityMixin(EntityType<? extends PersistentProjectileEntity> type, LivingEntity owner, World world, ItemStack stack, @Nullable ItemStack shotFrom) {
        super(type, owner, world, stack, shotFrom);
    }

    @Inject(method = "initDataTracker", at = @At("HEAD"), cancellable = true)
    protected void initDataTracker(DataTracker.Builder builder, CallbackInfo ci) {
        super.initDataTracker(builder);
        builder.add(LOYALTY, (byte)0);
        builder.add(ENCHANTED, false);
        ci.cancel();
    }

    @Inject(method = "onEntityHit", at = @At("HEAD"), cancellable = true)
    protected void onEntityHit(EntityHitResult entityHitResult, CallbackInfo ci) {
        Entity entity = entityHitResult.getEntity();
        float f = 8.0F;
        try {
            if(EnchantmentHelper.getLevel(this.getRegistryManager().getEntryOrThrow(Enchantments.INFINITY), this.getWeaponStack())>0){
                f = 6.0F;
            }
        } catch (NullPointerException npE){
            f = 8.0F;
        }

        float impalingDamage = 0;
        if(entity.isTouchingWaterOrRain()){
            try {
                impalingDamage = 1.0F*EnchantmentHelper.getLevel(this.getRegistryManager().getEntryOrThrow(Enchantments.IMPALING), this.getWeaponStack());
            }catch (NullPointerException npE){
                impalingDamage = 0;
            }
        }

        Entity entity2 = this.getOwner();
        DamageSource damageSource = this.getDamageSources().trident(this, (Entity)(entity2 == null ? this : entity2));
        if (this.getEntityWorld() instanceof ServerWorld serverWorld) {
            assert this.getWeaponStack() != null;
            f = EnchantmentHelper.getDamage(serverWorld, this.getWeaponStack(), entity, damageSource, f);
        }

        this.dealtDamage = true;
        if(this.getEntityWorld() instanceof ServerWorld serverWorld) {
            if (entity.damage(serverWorld, damageSource, (f+impalingDamage))) {
                if (entity.getType() == EntityType.ENDERMAN) {
                    return;
                }
                EnchantmentHelper.onTargetDamaged(serverWorld, entity, damageSource, this.getWeaponStack(), item -> this.kill(serverWorld));

                if (entity instanceof LivingEntity livingEntity) {
                    this.knockback(livingEntity, damageSource);
                    this.onHit(livingEntity);
                }
            }
        }

        this.deflect(ProjectileDeflection.REDIRECTED, entity, this.owner, false);
        this.setVelocity(this.getVelocity().multiply(0.02, 0.2, 0.02));
        this.playSound(SoundEvents.ITEM_TRIDENT_HIT, 1.0F, 1.0F);
        ci.cancel();
    }

}
