package com.pepperoni.mod.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.api.distmarker.Dist;
import com.pepperoni.mod.client.PepperoniClient;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class PepperoniEntity extends TamableAnimal {
    public static final int MODE_FOLLOW = 0, MODE_MINE = 1, MODE_FIGHT = 2;
    private static final EntityDataAccessor<Boolean> SLEEPING = SynchedEntityData.defineId(PepperoniEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> BOND = SynchedEntityData.defineId(PepperoniEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MODE = SynchedEntityData.defineId(PepperoniEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ARMOR_TIER = SynchedEntityData.defineId(PepperoniEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> CUDDLING = SynchedEntityData.defineId(PepperoniEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> EMOTE = SynchedEntityData.defineId(PepperoniEntity.class, EntityDataSerializers.INT);
    private BlockPos bedTarget;
    private int miningCooldown = 0;
    private int cuddleTicks = 0;
    private boolean sleepRequested = false;
    private boolean sleepStarted = false;
    private int emoteTicks = 0;
    private boolean cuddleRolled = false;

    public PepperoniEntity(EntityType<? extends TamableAnimal> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 26.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D).add(Attributes.FOLLOW_RANGE, 28.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D).add(Attributes.ARMOR, 2.0D);
    }

    @Override protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SLEEPING, false); entityData.define(BOND, 0);
        entityData.define(MODE, MODE_FOLLOW); entityData.define(ARMOR_TIER, 0);
        entityData.define(CUDDLING, false); entityData.define(EMOTE, 0);
    }

    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15D, true));
        goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.12D, 3.0F, 14.0F, false) {
            @Override public boolean canUse() { return getCompanionMode() == MODE_FOLLOW && super.canUse(); }
            @Override public boolean canContinueToUse() { return getCompanionMode() == MODE_FOLLOW && super.canContinueToUse(); }
        });
        goalSelector.addGoal(4, new MineOreGoal(this));
        goalSelector.addGoal(5, new BedtimeGoal(this));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.85D));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, true) {
            @Override public boolean canUse() { return isTame() && getCompanionMode() == MODE_FIGHT && super.canUse(); }
            @Override public boolean canContinueToUse() { return isTame() && getCompanionMode() == MODE_FIGHT && super.canContinueToUse(); }
        });
    }

    @Override public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!isTame() && (stack.is(Items.SWEET_BERRIES) || stack.is(Items.CAKE))) {
            if (!player.getAbilities().instabuild) stack.shrink(1);
            if (!level().isClientSide && random.nextInt(3) == 0) {
                tame(player); setOrderedToSit(true); setBond(10);
                level().broadcastEntityEvent(this, (byte) 7);
            } else if (!level().isClientSide) level().broadcastEntityEvent(this, (byte) 6);
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (isTame() && isOwnedBy(player)) {
            if (stack.getItem() instanceof ArmorItem) {
                if (!level().isClientSide) {
                    int tier = armorTier(stack);
                    setArmorTier(tier);
                    if (!player.getAbilities().instabuild) stack.shrink(1);
                    player.displayClientMessage(Component.literal("Pepperoni equipped armor! ♥"), true);
                }
                return InteractionResult.sidedSuccess(level().isClientSide);
            }
            if (level().isClientSide) {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> PepperoniClient.openScreen(getId(), getBond(), getModeName()));
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    private int armorTier(ItemStack stack) {
        String name = stack.getItem().toString();
        if (name.contains("netherite")) return 4;
        if (name.contains("diamond")) return 3;
        if (name.contains("golden")) return 2;
        if (name.contains("iron")) return 1;
        return 1;
    }

    public int getBond() { return entityData.get(BOND); }
    public void setBond(int value) { entityData.set(BOND, Math.max(0, Math.min(100, value))); }
    public int getArmorTier() { return entityData.get(ARMOR_TIER); }
    public void setArmorTier(int tier) {
        entityData.set(ARMOR_TIER, tier);
        if (getAttribute(Attributes.ARMOR) != null) getAttribute(Attributes.ARMOR).setBaseValue(2.0D + tier * 2.0D);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("PepperoniBond", getBond());
        tag.putInt("PepperoniMode", getCompanionMode());
        tag.putInt("PepperoniArmorTier", getArmorTier());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setBond(tag.getInt("PepperoniBond"));
        setCompanionMode(tag.contains("PepperoniMode") ? tag.getInt("PepperoniMode") : MODE_FOLLOW);
        setArmorTier(tag.getInt("PepperoniArmorTier"));
    }
    public boolean isCuddling() { return entityData.get(CUDDLING); }
    public boolean isWaving() { return entityData.get(EMOTE) == 1; }
    public boolean isSleeping() { return entityData.get(SLEEPING); }
    public void setSleeping(boolean value) { entityData.set(SLEEPING, value); }
    public int getCompanionMode() { return entityData.get(MODE); }
    public void setCompanionMode(int value) {
        entityData.set(MODE, value);
        setOrderedToSit(false);
        if (value != MODE_FIGHT) setTarget(null);
    }
    public String getModeName() { return switch (getCompanionMode()) { case MODE_MINE -> "Mining"; case MODE_FIGHT -> "Guard"; default -> "Following"; }; }

    public void talkTo(Player player) {
        setBond(getBond() + 1);
        entityData.set(EMOTE, 1); emoteTicks = 50;
        player.displayClientMessage(Component.literal("Pepperoni: Hehe~! I like hanging out with you. ♥  (Bond " + getBond() + "/100)"), true);
        if (level() instanceof ServerLevel server) server.sendParticles(ParticleTypes.HEART, getX(), getY() + 1.8, getZ(), 2, .3, .2, .3, .02);
    }

    public void acceptGift(Player player) {
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) { player.displayClientMessage(Component.literal("Pepperoni: You didn't bring me a gift!"), true); return; }
        boolean loved = held.is(Items.CAKE) || held.is(Items.SWEET_BERRIES) || held.is(Items.PINK_TULIP)
                || held.is(Items.POPPY) || held.is(Items.GLOW_BERRIES) || held.is(Items.COOKIE)
                || held.is(Items.HONEY_BOTTLE) || held.is(Items.AMETHYST_SHARD);
        if (!player.getAbilities().instabuild) held.shrink(1);
        setBond(getBond() + (loved ? 8 : 2));
        player.displayClientMessage(Component.literal(loved ? "Pepperoni: Eee! I love this gift! ♥  Bond " + getBond() : "Pepperoni: A gift for me? Thank you! ♥  Bond " + getBond()), true);
        if (level() instanceof ServerLevel server) server.sendParticles(ParticleTypes.HEART, getX(), getY() + 1.8, getZ(), loved ? 8 : 3, .4, .3, .4, .03);
    }

    public void requestBedtime(Player player) {
        if (player.isSleeping()) { player.displayClientMessage(Component.literal("Pepperoni: You're already tucked in! ♥"), true); return; }
        BlockPos playerBed = findNearbyBed(player.blockPosition(), false);
        if (playerBed == null) {
            player.displayClientMessage(Component.literal("Pepperoni: Place a bed for yourself nearby first!"), true);
            return;
        }
        BlockPos companionBed = findAdjacentFreeBed(playerBed);
        if (companionBed == null) {
            player.displayClientMessage(Component.literal("Pepperoni: Could you place a second bed beside yours?"), true);
            return;
        }
        bedTarget = companionBed;
        sleepRequested = true; sleepStarted = false;
        cuddleRolled = false;
        player.displayClientMessage(Component.literal("Pepperoni: I'll use the second bed! Climb into yours when you're ready. ♥"), true);
    }

    private BlockPos findNearbyBed(BlockPos center, boolean occupied) {
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-2,-1,-2), center.offset(2,1,2))) {
            BlockState state = level().getBlockState(pos);
            if (state.getBlock() instanceof BedBlock && state.getValue(BedBlock.PART) == BedPart.HEAD
                    && state.getValue(BedBlock.OCCUPIED) == occupied) return pos.immutable();
        }
        return null;
    }
    private BlockPos findAdjacentFreeBed(BlockPos bed) {
        for (BlockPos pos : new BlockPos[]{bed.north(), bed.south(), bed.east(), bed.west()}) {
            BlockState state = level().getBlockState(pos);
            if (state.getBlock() instanceof BedBlock && !state.getValue(BedBlock.OCCUPIED)
                    && state.getValue(BedBlock.PART) == BedPart.HEAD) return pos;
        }
        return null;
    }

    @Override public void tick() {
        super.tick();
        if (miningCooldown > 0) miningCooldown--;
        if (emoteTicks > 0 && --emoteTicks == 0) entityData.set(EMOTE, 0);
        if (sleepRequested && isTame() && !level().isDay() && getOwner() instanceof Player owner && owner.isSleeping() && bedTarget != null) {
            sleepStarted = true;
            if (isCuddling()) {
                getNavigation().stop();
                setPos(owner.getX(), owner.getY() + 0.18D, owner.getZ());
            } else if (distanceToSqr(Vec3.atCenterOf(bedTarget)) > 2.5D) {
                getNavigation().moveTo(bedTarget.getX()+.5, bedTarget.getY(), bedTarget.getZ()+.5, 1.0D);
            }
            if (distanceToSqr(Vec3.atCenterOf(bedTarget)) < 2.5D) {
                getNavigation().stop(); setSleeping(true); setOrderedToSit(false);
                setPos(bedTarget.getX() + .5, bedTarget.getY() + .15, bedTarget.getZ() + .5);
                if (!cuddleRolled) {
                    cuddleRolled = true;
                    if (random.nextInt(12) == 0 && getBond() >= 35) {
                        entityData.set(CUDDLING, true); cuddleTicks = 100;
                        if (level() instanceof ServerLevel server) server.sendParticles(ParticleTypes.HEART, getX(), getY()+1.2, getZ(), 5, .3,.2,.3,.02);
                    }
                }
            }
        }
        if (cuddleTicks > 0 && --cuddleTicks == 0) {
            entityData.set(CUDDLING, false);
            if (bedTarget != null) setPos(bedTarget.getX() + .5, bedTarget.getY() + .15, bedTarget.getZ() + .5);
        }
        if (level().isDay() || (sleepStarted && getOwner() instanceof Player owner && !owner.isSleeping())) {
            setSleeping(false); bedTarget = null; sleepRequested = false; sleepStarted = false; entityData.set(CUDDLING, false);
        }
    }

    @Override protected SoundEvent getAmbientSound() { return SoundEvents.CAT_PURR; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.CAT_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.CAT_DEATH; }
    @Override protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        if (random.nextBoolean()) spawnAtLocation(Items.RED_WOOL);
        if (random.nextInt(3) == 0) spawnAtLocation(Items.SWEET_BERRIES);
        if (random.nextInt(20) == 0) spawnAtLocation(Items.BELL);
    }
    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) { return null; }

    private static class BedtimeGoal extends Goal {
        private final PepperoniEntity mob;
        BedtimeGoal(PepperoniEntity mob) { this.mob = mob; setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }
        @Override public boolean canUse() { return mob.sleepRequested && mob.bedTarget != null && !mob.level().isDay() && !mob.isOrderedToSit() && mob.getOwner() instanceof Player p && p.isSleeping(); }
        @Override public boolean canContinueToUse() { return canUse() && !mob.isSleeping(); }
        @Override public void tick() {
            if (mob.bedTarget != null && mob.distanceToSqr(Vec3.atCenterOf(mob.bedTarget)) < 2.5D) {
                mob.getNavigation().stop(); mob.setSleeping(true);
            }
        }
    }

    private static class MineOreGoal extends Goal {
        private final PepperoniEntity mob;
        private BlockPos target;
        MineOreGoal(PepperoniEntity mob) { this.mob = mob; setFlags(EnumSet.of(Flag.MOVE)); }
        @Override public boolean canUse() {
            if (!mob.isTame() || mob.getCompanionMode() != MODE_MINE || mob.miningCooldown > 0 || mob.isOrderedToSit()) return false;
            target = findOre(); return target != null;
        }
        @Override public boolean canContinueToUse() { return target != null && mob.getCompanionMode() == MODE_MINE && mob.level().getBlockState(target).getDestroySpeed(mob.level(), target) >= 0; }
        @Override public void tick() {
            if (target == null) return;
            if (mob.distanceToSqr(Vec3.atCenterOf(target)) > 4) mob.getNavigation().moveTo(target.getX()+.5,target.getY(),target.getZ()+.5,1.0D);
            else if (mob.level() instanceof ServerLevel server) {
                BlockState state = server.getBlockState(target);
                if (isOre(state.getBlock())) {
                    server.destroyBlock(target, true, mob);
                    server.sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX()+.5, target.getY()+.5, target.getZ()+.5, 4, .2,.2,.2,.02);
                }
                target = null; mob.miningCooldown = 100;
            }
        }
        private BlockPos findOre() {
            BlockPos origin = mob.blockPosition();
            for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-6,-3,-6), origin.offset(6,3,6)))
                if (isOre(mob.level().getBlockState(pos).getBlock())) return pos.immutable();
            return null;
        }
        private boolean isOre(Block b) {
            return b == Blocks.COAL_ORE || b == Blocks.IRON_ORE || b == Blocks.COPPER_ORE || b == Blocks.GOLD_ORE
                    || b == Blocks.REDSTONE_ORE || b == Blocks.LAPIS_ORE || b == Blocks.DIAMOND_ORE || b == Blocks.EMERALD_ORE
                    || b == Blocks.DEEPSLATE_COAL_ORE || b == Blocks.DEEPSLATE_IRON_ORE || b == Blocks.DEEPSLATE_COPPER_ORE
                    || b == Blocks.DEEPSLATE_GOLD_ORE || b == Blocks.DEEPSLATE_REDSTONE_ORE || b == Blocks.DEEPSLATE_LAPIS_ORE
                    || b == Blocks.DEEPSLATE_DIAMOND_ORE || b == Blocks.DEEPSLATE_EMERALD_ORE;
        }
    }
}
