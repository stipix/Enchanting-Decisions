package stipix.enchanting_decisions;

import net.minecraft.block.BlockState;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityFlagsPredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Predicate;
import stipix.enchanting_decisions.enchantments.EnchantmentRegistry;

public class CustomTridentEntity extends PersistentProjectileEntity {
    private static final TrackedData<Byte> LOYALTY = DataTracker.registerData(CustomTridentEntity.class, TrackedDataHandlerRegistry.BYTE);
    private int IMPALING=0;
    private int WRATH=0;
    private int LOYAL2=0;
    private boolean INFINITY=false;
    private boolean CHANNELING=false;
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
        if(EnchantmentHelper.getLevel(world.getRegistryManager().getEntryOrThrow(Enchantments.CHANNELING), stack)!=0){ // DECLARES/INITIALIZES INFINITY LEVEL
            CHANNELING=true;
        }
        if(EnchantmentHelper.getLevel(world.getRegistryManager().getEntryOrThrow(Enchantments.LOYALTY), stack)!=0){ // DECLARES/INITIALIZES INFINITY LEVEL
            LOYAL2=EnchantmentHelper.getLevel(world.getRegistryManager().getEntryOrThrow(Enchantments.LOYALTY), stack);
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
        if (this.inGroundTime > 0 && INFINITY) {
            this.discard();
        }
        if (this.inGroundTime > 4) {
            if (WRATH > 0 && this.inGroundTime > ((WRATH+1) * 30)) {
                this.dealtDamage = true;
            } else if (WRATH == 0) {
                this.dealtDamage = true;
            }
        }

        //TODO - WRATH
        if (WRATH > 0) {

            int attackInterval=(44-(WRATH*4))*(CHANNELING?2:1);
            if (getEntityWorld() instanceof ServerWorld serverWorld) {


                // Marks enemies to receive damage momentarily
                if ((this.inGroundTime+30) % attackInterval == (attackInterval-9)) {

                    for (Entity badGuy : this.getEntityWorld().getOtherEntities(this.getEntity(), this.getBoundingBox().expand(0.6f * WRATH))) {
                        if (badGuy instanceof LivingEntity livingEntity && !badGuy.equals(getOwner())) {
                            serverWorld.spawnParticles(ParticleTypes.SONIC_BOOM,
                                    livingEntity.getX(), livingEntity.getY()+1, livingEntity.getZ(), 1, 0, 0.2f, 0, 2.4f);
                            livingEntity.addCommandTag("Wrath-targeted");
                        }
                    }

                    //Particle for the TridentEntity itself
                    serverWorld.spawnParticles(ParticleTypes.SONIC_BOOM,
                            this.getX(), this.getY(), this.getZ(), 1, 0, 0.08f, 0, 0.3f);
                }

                if ((this.inGroundTime+30) % attackInterval == (attackInterval-3)) {
                    this.playSound(SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL, 1f, 1.12f);
                }


                    // Applies damage, works with Channeling
                if ((this.inGroundTime+30) % attackInterval == 0 && this.inGroundTime > 0) {
                    for(Entity badGuy: serverWorld.getOtherEntities(this.getEntity(), this.getBoundingBox().expand(25), (Predicate<Entity>) entity -> {
                        assert entity != null;
                        return entity.getCommandTags().contains("Wrath-targeted");
                        }))
                    {

                        if (badGuy instanceof LivingEntity livingEntity && !badGuy.equals(getOwner())) {
                            serverWorld.spawnParticles(ParticleTypes.SPLASH,
                                    livingEntity.getX(), livingEntity.getY() + 1, livingEntity.getZ(), 50, 0, 1, 0, 0.7f);
                            badGuy.playSound(SoundEvents.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 0.3f, 1.08f);

                            livingEntity.damage(serverWorld, new DamageSource(serverWorld.getRegistryManager().getEntryOrThrow(DamageTypes.INDIRECT_MAGIC)), 2 + ((float) WRATH / 2));
                            livingEntity.removeCommandTag("Wrath-targeted");
                                //livingEntity.addVelocity(0, 0.5f, 0);
                                //livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS,40,4));

                            if (CHANNELING) {
                                BlockPos abovePos = livingEntity.getBlockPos().up();
                                    // Check if the block above is air or transparent
                                if (serverWorld.isSkyVisible(abovePos)) {
                                    EntityType.LIGHTNING_BOLT.spawn(serverWorld, livingEntity.getBlockPos(), SpawnReason.TRIGGERED);
                                }
                            }
                        }
                    }
                }
            }
    }

        Entity entity = this.getOwner();
        if (LOYAL2 > 0 && (this.dealtDamage || this.isNoClip()) && entity != null) { //Loyalty

            if (!this.isOwnerAlive()) {//Drop Trident because Loyalty won't apply
                if (this.getEntityWorld() instanceof ServerWorld serverWorld && this.pickupType == PersistentProjectileEntity.PickupPermission.ALLOWED) {
                    this.dropStack(serverWorld, this.asItemStack(), 0.1F);
                }

                this.discard();
            } else {//Loyalty applies
                if (!(entity instanceof PlayerEntity) && this.getEntityPos().distanceTo(entity.getEyePos()) < entity.getWidth() + 1.0) {
                    this.discard();
                    return;
                }


                this.setNoClip(true);
                this.setGlowing(true);
                Vec3d vec3d = entity.getEyePos().subtract(this.getEntityPos());
                this.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, entity.getEyePos());
                //this.setPos(this.getX(), this.getY() + vec3d.y * 0.015 * LOYAL2, this.getZ());
                double d = 0.05 * LOYAL2;
                this.setVelocity(this.getVelocity().multiply(0.95).add(vec3d.normalize().multiply(d)));
                if (this.returnTimer == 0) {
                    this.playSound(SoundEvents.ITEM_TRIDENT_RETURN, 8, 1.0f);
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
        Entity entity = entityHitResult.getEntity();

        float attackDamage;
        if(entity.isTouchingWaterOrRain() && IMPALING>0){ //+ Impaling effect
            attackDamage = 8.0F+(IMPALING*2);
            ((ServerWorld)getEntityWorld()).spawnParticles(ParticleTypes.ENCHANTED_HIT,
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

        if(WRATH==0){
            this.dealtDamage = true;
        }

        this.setVelocity(this.getVelocity().multiply(0.02, 0.2, 0.02).add(0,-0.3,0));
        this.deflect(ProjectileDeflection.SIMPLE, entity, this.owner, false);
        this.playSound(SoundEvents.ITEM_TRIDENT_HIT, 1.0F, 1.0F);

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

