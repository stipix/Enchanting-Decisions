package name.modid.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.provider.EnchantmentProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.item.EnchantmentsPredicate;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin { //"extends Entity"

    @Shadow @Nullable public abstract LivingEntity getAttacker();

    //public LivingEntityMixin(EntityType<?> type, World world) {
    //    super(type, world);
    //}

    @Shadow
    public abstract @Nullable LivingEntity getEntity();

    @Redirect(method = "damageEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;damage(ILnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;)V"))
    protected void reduceDurability(ItemStack instance, int amount, LivingEntity entity, EquipmentSlot slot){
        if(slot.isArmorSlot()) {
                World world = entity.getEntityWorld();
                if(EnchantmentHelper.getLevel(world.getRegistryManager().getEntryOrThrow(Enchantments.THORNS), instance)!=0){
                    instance.damage(50, entity, slot);
                    return;
                }
                instance.damage(amount, entity, slot);
                return;
        }
        instance.damage(amount, entity, slot);
    }

    //modify standard armor scaling
    @Redirect(method = "applyArmorToDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/DamageUtil;getDamageLeft(Lnet/minecraft/entity/LivingEntity;FLnet/minecraft/entity/damage/DamageSource;FF)F"))
    protected float applyArmorToDamage(LivingEntity armorWearer, float damage, DamageSource damageSource, float armor, float armorToughness) {
        if (!damageSource.isIn(DamageTypeTags.BYPASSES_ARMOR)) {
            damage = Math.round(damage*(1-(armor/(armor+4f+(damage/(1f+(float) Math.log(1f+armorToughness))))))*2)/2.0f;
        }
        //=ROUND($B$10/($B$10+(4+($A$10/(1+(log(1+$C$10)))))),3)
        /*
        float t1 = 1f+(float) Math.log(1f+armorToughness);
        float t2 = (armor+4f+(damage/t1));
        float t3 = (1-(armor/t2));
        return Math.round(damage*t3*2)/2.0f;
        */
        return damage;
    }

    // Modify Protection armor enchantment
    @Redirect(method = "modifyAppliedDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/DamageUtil;getInflictedDamage(FF)F"))
    protected float applyEnchantmentsToDamage(float damage, float protection, DamageSource source) {
        if (source.isIn(DamageTypeTags.BYPASSES_EFFECTS)) {
            return damage;
        }
        float t1 = Math.min(damage/(damage+16),.50f);
        return Math.round(damage*(1-t1));

    }

    @Inject(method = "dropExperience", at = @At("HEAD"), cancellable = true)
    protected void dropExperience(ServerWorld world, Entity attacker, CallbackInfo ci){
        ci.cancel();
    }



/*
    @Inject(method = "damageArmor", at = @At("HEAD"), cancellable = true)
    private void hurtAndBreak(DamageSource source, float amount, CallbackInfo ci) {


        ci.cancel();
    }
*/


}


