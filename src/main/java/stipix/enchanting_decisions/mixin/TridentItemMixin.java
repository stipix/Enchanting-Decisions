package stipix.enchanting_decisions.mixin;

import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TridentItem.class)
public abstract class TridentItemMixin extends Item implements ProjectileItem {

    @Unique
    int IMPALING_LEVEL=0;

    public TridentItemMixin(Item.Settings settings) {
        super(settings);
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (itemStack.willBreakNextUse()) {
            cir.setReturnValue(ActionResult.FAIL);
        } else {
            user.setCurrentHand(hand);
            cir.setReturnValue(ActionResult.CONSUME);
        }
        cir.cancel();
    }

    @Shadow
    public abstract int getMaxUseTime(ItemStack stack, LivingEntity user);

    @Shadow
    @Final
    public static float ATTACK_DAMAGE;


    @Inject(method = "onStoppedUsing", at = @At("HEAD"),cancellable = true)
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfoReturnable<Boolean> cir) {
        if (user instanceof PlayerEntity playerEntity) {
            int i = this.getMaxUseTime(stack, user) - remainingUseTicks;
            if (i < 10) {
                cir.setReturnValue(false);
                cir.cancel();
            } else {
                float f = EnchantmentHelper.getTridentSpinAttackStrength(stack, playerEntity);
                if (stack.willBreakNextUse()) {
                    cir.setReturnValue(false);
                    cir.cancel();
                } else {
                    RegistryEntry<SoundEvent> registryEntry = (RegistryEntry<SoundEvent>)EnchantmentHelper.getEffect(stack, EnchantmentEffectComponentTypes.TRIDENT_SOUND)
                            .orElse(SoundEvents.ITEM_TRIDENT_THROW);
                    playerEntity.incrementStat(Stats.USED.getOrCreateStat(this));

                    //RIPTIDE

                    if (f > 0.0F && playerEntity.isTouchingWaterOrRain()) {
                        float g = playerEntity.getYaw();
                        float h = playerEntity.getPitch();
                        float j = -MathHelper.sin(g * (float) (Math.PI / 180.0)) * MathHelper.cos(h * (float) (Math.PI / 180.0));
                        float k = -MathHelper.sin(h * (float) (Math.PI / 180.0));
                        float l = MathHelper.cos(g * (float) (Math.PI / 180.0)) * MathHelper.cos(h * (float) (Math.PI / 180.0));
                        float m = MathHelper.sqrt(j * j + k * k + l * l);
                        j *= f / m;
                        k *= f / m;
                        l *= f / m;
                        playerEntity.addVelocity(j, k, l);
                        playerEntity.useRiptide(20, 8.0F, stack);
                        if (playerEntity.isOnGround()) {
                            float n = 1.1999999F;
                            playerEntity.move(MovementType.SELF, new Vec3d(0.0, n, 0.0));
                        }

                        world.playSoundFromEntity(null, playerEntity, registryEntry.value(), SoundCategory.PLAYERS, 1.0F, 1.0F);
                        cir.setReturnValue(true);
                        cir.cancel();
                    }
                    else    //REGULAR TRIDENT THROW
                    {
                        if (world instanceof ServerWorld serverWorld) {

                            boolean hasInfinity = false;
                            for(RegistryEntry<Enchantment> e : stack.getEnchantments().getEnchantments()){
                                if(e.matchesKey(Enchantments.INFINITY)){
                                    hasInfinity = true;
                                    break;
                                }
                            }
                            TridentEntity tridentEntity;

                            if(!hasInfinity){
                                stack.damage(1, playerEntity);
                                ItemStack itemStack = stack.splitUnlessCreative(1, playerEntity);
                                tridentEntity = ProjectileEntity.spawnWithVelocity(TridentEntity::new, serverWorld, itemStack, playerEntity, 0.0F, 2.5F, 1.0F);
                                if (playerEntity.isInCreativeMode()) {
                                    tridentEntity.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
                                }
                            }else{
                                            stack.damage(2, playerEntity);
                                        tridentEntity = ProjectileEntity.spawnWithVelocity(TridentEntity::new, serverWorld, stack, playerEntity, 0.0F, 2.5F, 0.0F);
                                tridentEntity.pickupType = PersistentProjectileEntity.PickupPermission.DISALLOWED;
                            }
                                world.playSoundFromEntity(null, tridentEntity, registryEntry.value(), SoundCategory.PLAYERS, 1.0F, 1.0F);
                                cir.setReturnValue(true);
                                cir.cancel();
                        }
                    }

                }
            }
        } else {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Override
    @Nullable
    public DamageSource getDamageSource(LivingEntity user) {
        if(user.getMainHandStack().getItem()==Items.TRIDENT){
            IMPALING_LEVEL=EnchantmentHelper.getLevel(user.getRegistryManager().getEntryOrThrow(Enchantments.IMPALING),user.getMainHandStack());
        }
    return null;
    }


    @Override
    public float getBonusAttackDamage(Entity target, float baseAttackDamage, DamageSource damageSource) {
        //for(RegistryEntry<Enchantment> enchantment : itemStack.getEnchantments().getEnchantments()){
        //    int level = itemStack.getEnchantments().getLevel(enchantment);

        if(target.isTouchingWaterOrRain()){
            return IMPALING_LEVEL*1.5F;

        }else{
            return 0.0F;
        }


    }



}
