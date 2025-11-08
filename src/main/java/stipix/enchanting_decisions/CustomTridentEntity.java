package stipix.enchanting_decisions;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ProjectileDeflection;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import stipix.enchanting_decisions.enchantments.EnchantmentRegistry;

public class CustomTridentEntity extends PersistentProjectileEntity {
    private static final TrackedData<Byte> LOYALTY = DataTracker.registerData(CustomTridentEntity.class, TrackedDataHandlerRegistry.BYTE);
    private int IMPALING=0;
    private int WRATH=0;
    private boolean INFINITY=false;
    private int KNOCKBACK=0;

    private static final TrackedData<Boolean> ENCHANTED = DataTracker.registerData(CustomTridentEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final float DRAG_IN_WATER = 0.99F;
    private static final boolean DEFAULT_DEALT_DAMAGE = false;
    private boolean dealtDamage = false;
    public int returnTimer;

    public CustomTridentEntity(EntityType<? extends net.minecraft.entity.projectile.TridentEntity> entityType, World world) {
        super(entityType, world);
    }

    public CustomTridentEntity(World world, LivingEntity owner, ItemStack stack) {
        super(EntityType.TRIDENT, owner, world, stack, null);
        this.dataTracker.set(LOYALTY, this.getLoyalty(stack));
        this.dataTracker.set(ENCHANTED, stack.hasGlint());
        if(EnchantmentHelper.getLevel(world.getRegistryManager().getEntryOrThrow(Enchantments.IMPALING), stack)!=0){ // DECLARES/INITIALIZES IMPALING LEVEL
            IMPALING=EnchantmentHelper.getLevel(world.getRegistryManager().getEntryOrThrow(Enchantments.IMPALING), stack);
        }
        if(EnchantmentHelper.getLevel(world.getRegistryManager().getEntryOrThrow(EnchantmentRegistry.WRATH), stack)!=0){ // DECLARES/INITIALIZES WRATH LEVEL
            WRATH=EnchantmentHelper.getLevel(world.getRegistryManager().getEntryOrThrow(EnchantmentRegistry.WRATH), stack);
        }
        if(EnchantmentHelper.getLevel(world.getRegistryManager().getEntryOrThrow(Enchantments.INFINITY), stack)!=0){ // DECLARES/INITIALIZES INFINITY LEVEL
            INFINITY=true;
        }
        if(EnchantmentHelper.getLevel(world.getRegistryManager().getEntryOrThrow(Enchantments.KNOCKBACK), stack)!=0){ // DECLARES/INITIALIZES INFINITY LEVEL
            KNOCKBACK=EnchantmentHelper.getLevel(world.getRegistryManager().getEntryOrThrow(Enchantments.KNOCKBACK), stack);
        }
    }

    public CustomTridentEntity(World world, double x, double y, double z, ItemStack stack) {
        super(EntityType.TRIDENT, x, y, z, world, stack, stack);
        this.dataTracker.set(LOYALTY, this.getLoyalty(stack));
        this.dataTracker.set(ENCHANTED, stack.hasGlint());
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(LOYALTY, (byte)0);
        builder.add(ENCHANTED, false);
    }

    @Override
    public void tick() {
        if(this.inGroundTime > 0 && INFINITY) {
                this.discard();
        }

        if (this.inGroundTime > 4) {
            if (WRATH == 0) {
                this.dealtDamage = true;
            }
        }



        //TODO - Get WRATH to work
        try{
            this.getEntityWorld().sendEntityDamage(this.getEntityCollision(this.getEntityPos(),this.getEntityPos()).getEntity(), new DamageSource(null));


        }catch (NullPointerException nullPointerException){
            //oops
        }

        Entity entity = this.getOwner();
        int loyaltyLevel = this.dataTracker.get(LOYALTY);
        if (loyaltyLevel > 0 && (this.dealtDamage || this.isNoClip()) && entity != null) {
            if (!this.isOwnerAlive()) {
                if (this.getEntityWorld() instanceof ServerWorld serverWorld && this.pickupType == PersistentProjectileEntity.PickupPermission.ALLOWED) {
                    this.dropStack(serverWorld, this.asItemStack(), 0.1F);
                }

                this.discard();
            } else {
                if (!(entity instanceof PlayerEntity) && this.getEntityPos().distanceTo(entity.getEyePos()) < entity.getWidth() + 1.0) {
                    this.discard();
                    return;
                }

                this.setNoClip(true);
                this.setGlowing(true);
                Vec3d vec3d = entity.getEyePos().subtract(this.getEntityPos());
                this.setPos(this.getX(), this.getY() + vec3d.y * 0.015 * loyaltyLevel, this.getZ());
                double d = 0.05 * loyaltyLevel;
                this.setVelocity(this.getVelocity().multiply(0.95).add(vec3d.normalize().multiply(d)));
                if (this.returnTimer == 0) {
                    this.playSound(SoundEvents.ITEM_TRIDENT_RETURN, 10.0F, 1.0F);
                }

                this.returnTimer++;
            }
        }

        super.tick();
    }

    private boolean isOwnerAlive() {
        Entity entity = this.getOwner();
        return entity == null || !entity.isAlive() ? false : !(entity instanceof ServerPlayerEntity) || !entity.isSpectator();
    }

    public boolean isEnchanted() {
        return this.dataTracker.get(ENCHANTED);
    }

    @Nullable
    @Override
    protected EntityHitResult getEntityCollision(Vec3d currentPosition, Vec3d nextPosition) {
        return WRATH == 0 && this.dealtDamage ? null : super.getEntityCollision(currentPosition, nextPosition);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        //this.getEntityWorld().spawnEntity(CustomTridentEntity)


        Entity entity = entityHitResult.getEntity();

        float attackDamage;
        if(entity.isTouchingWaterOrRain() && IMPALING>0){ //+ Impaling effect
            attackDamage = 8.0F+(IMPALING*2);
            ((ServerWorld)getEntityWorld()).spawnParticles(ParticleTypes.CRIT,
                    entity.getX(), entity.getY(), entity.getZ(), 2*(int)attackDamage, 0, 1, 0, 0.4f);
            getEntityWorld().playSound(this, entity.getBlockPos(),SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.MASTER);
        }else{
            attackDamage = 8.0F; // no Impaling
        }

        Entity entity2 = this.getOwner();
        DamageSource damageSource = this.getDamageSources().trident(this, (Entity)(entity2 == null ? this : entity2));
        if (this.getEntityWorld() instanceof ServerWorld serverWorld) {
            attackDamage = EnchantmentHelper.getDamage(serverWorld, this.getWeaponStack(), entity, damageSource, attackDamage);
        }

        if (entity.sidedDamage(damageSource, attackDamage)) {
            if (entity.getType() == EntityType.ENDERMAN) {
                return;
            }

            if (this.getEntityWorld() instanceof ServerWorld serverWorld) {
                EnchantmentHelper.onTargetDamaged(serverWorld, entity, damageSource, this.getWeaponStack(), item -> this.kill(serverWorld));
            }

            if (entity instanceof LivingEntity livingEntity) {
                knockback(livingEntity, damageSource);
                this.onHit(livingEntity);
            }
        }

        this.deflect(ProjectileDeflection.SIMPLE, entity, this.owner, false);
        this.setVelocity(this.getVelocity().multiply(0.02, 0.2, 0.02));
        this.playSound(SoundEvents.ITEM_TRIDENT_HIT, 1.0F, 1.0F);

        if(WRATH>0) {//TODO - rapid fire/recursion
            if (this.getEntityWorld() instanceof ServerWorld serverWorld) {
                if (this.getOwner() instanceof LivingEntity livingOwner) {
                    CustomTridentEntity tridentEntity;
                    tridentEntity = ProjectileEntity.spawnWithVelocity(CustomTridentEntity::new, serverWorld, this.getWeaponStack(), livingOwner, 0.0F, 2.5F, 0.0F);
                    tridentEntity.setRotation(this.getYaw(), this.getPitch());
                    tridentEntity.setGlowing(true);
                    tridentEntity.setPosition(entityHitResult.getEntity().getX(), entityHitResult.getEntity().getY() + 2, entityHitResult.getEntity().getZ());
                    if (livingOwner instanceof PlayerEntity playerEntity) {
                        //tridentEntity.changeLookDirection(playerEntity.getHeadYaw(), playerEntity.getPitch());
                    }
                }
            }
        }

        if(INFINITY){
            this.discard();
        }

    }

    @Override   //Somehow necessary in order for it to apply the knockback to the projectile based off Knockback level
    protected void knockback(LivingEntity target, DamageSource source) {
        double d = this.getItemStack() != null && this.getEntityWorld() instanceof ServerWorld serverWorld
                ? EnchantmentHelper.modifyKnockback(serverWorld, this.getItemStack(), target, source, 0.0F)
                : 0.0F;
        if (d > 0.0) {
            double e = Math.max(0.0, 1.0 - target.getAttributeValue(EntityAttributes.KNOCKBACK_RESISTANCE));
            Vec3d vec3d = this.getVelocity().multiply(1.0, 0.0, 1.0).normalize().multiply(d * 0.6 * e);
            if (vec3d.lengthSquared() > 0.0) {
                target.addVelocity(vec3d.x, 0.1, vec3d.z);
            }
        }
    }



    @Override
    protected void onBlockHitEnchantmentEffects(ServerWorld world, BlockHitResult blockHitResult, ItemStack weaponStack) {
        Vec3d vec3d = blockHitResult.getBlockPos().clampToWithin(blockHitResult.getPos());
        EnchantmentHelper.onHitBlock(
                world,
                weaponStack,
                this.getOwner() instanceof LivingEntity livingEntity ? livingEntity : null,
                this,
                null,
                vec3d,
                world.getBlockState(blockHitResult.getBlockPos()),
                item -> this.kill(world)
        );
    }

    @Override
    public ItemStack getWeaponStack() {
        return this.getItemStack();
    }

    @Override
    protected boolean tryPickup(PlayerEntity player) {
        return super.tryPickup(player) || this.isNoClip() && this.isOwner(player) && player.getInventory().insertStack(this.asItemStack());
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        return new ItemStack(Items.TRIDENT);
    }

    @Override
    protected SoundEvent getHitSound() {
        return SoundEvents.ITEM_TRIDENT_HIT_GROUND;
    }

    @Override
    public void onPlayerCollision(PlayerEntity player) {
        if (this.isOwner(player) || this.getOwner() == null) {
            super.onPlayerCollision(player);
        }
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.dealtDamage = view.getBoolean("DealtDamage", false);
        this.dataTracker.set(LOYALTY, this.getLoyalty(this.getItemStack()));
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putBoolean("DealtDamage", this.dealtDamage);
    }

    private byte getLoyalty(ItemStack stack) {
        return this.getEntityWorld() instanceof ServerWorld serverWorld
                ? (byte)MathHelper.clamp(EnchantmentHelper.getTridentReturnAcceleration(serverWorld, stack, this), 0, 127)
                : 0;
    }

    @Override
    public void age() {
        int i = this.dataTracker.get(LOYALTY);
        if (this.pickupType != PersistentProjectileEntity.PickupPermission.ALLOWED || i <= 0) {
            super.age();
        }
    }

    @Override
    protected float getDragInWater() {
        return 0.99F;
    }

    @Override
    public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
        return true;
    }
}

