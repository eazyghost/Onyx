package net.onyx.client.utils;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.explosion.Explosion;
import net.onyx.client.OnyxClient;

import java.util.List;


public enum CrystalUtils
{
    ;




    public static boolean canPlaceCrystalServer(BlockPos block)
    {
        BlockState blockState = OnyxClient.getClient().world.getBlockState(block);
        if (!blockState.isOf(Blocks.OBSIDIAN) && !blockState.isOf(Blocks.BEDROCK))
            return false;
        BlockPos blockPos2 = block.up();
        if (!OnyxClient.getClient().world.isAir(blockPos2))
            return false;
        double d = blockPos2.getX();
        double e = blockPos2.getY();
        double f = blockPos2.getZ();
        List<Entity> list = OnyxClient.getClient().world.getOtherEntities(null, new Box(d, e, f, d + 1.0D, e + 2.0D, f + 1.0D));
        //list.removeIf(entity -> entity instanceof ItemEntity); // items will be picked up by the nearby player, crystals can be placed down a lot faster in citying
        list.removeIf(entity ->
        {
            if (!(entity instanceof EndCrystalEntity))
                return false;
            return OnyxClient.getInstance().getCrystalDataTracker().isCrystalAttacked(entity);
        }); // crystal placement will be faster since on the server side the crystal have already been removed (probably)
        return list.isEmpty();
    }
    public static boolean canPlaceCrystalClient(BlockPos block)
    {
        BlockState blockState = OnyxClient.getClient().world.getBlockState(block);
        if (!blockState.isOf(Blocks.OBSIDIAN) && !blockState.isOf(Blocks.BEDROCK))
            return false;
        return canPlaceCrystalClientAssumeObsidian(block);
    }
    public static boolean canPlaceCrystalClientAssumeObsidian(BlockPos block)
    {
        BlockPos blockPos2 = block.up();
        if (!OnyxClient.getClient().world.isAir(blockPos2))
            return false;
        double d = blockPos2.getX();
        double e = blockPos2.getY();
        double f = blockPos2.getZ();
        List<Entity> list = OnyxClient.getClient().world.getOtherEntities(null, new Box(d, e, f, d + 1.0D, e + 2.0D, f + 1.0D));
        return list.isEmpty();
    }
    public static boolean canPlaceCrystalClientAssumeObsidian(BlockPos block, Box bb)
    {
        BlockPos blockPos2 = block.up();
        if (!OnyxClient.getClient().world.isAir(blockPos2))
            return false;
        double d = blockPos2.getX();
        double e = blockPos2.getY();
        double f = blockPos2.getZ();
        Box crystalBox = new Box(d, e, f, d + 1.0D, e + 2.0D, f + 1.0D);
        if (crystalBox.intersects(bb))
            return false;
        List<Entity> list = OnyxClient.getClient().world.getOtherEntities(null, crystalBox);
        return list.isEmpty();
    }












    public static double crystalDamage(PlayerEntity player, Vec3d playerPos, Vec3d crystal, BlockPos obsidianPos, boolean ignoreTerrain) {
        if (player == null) return 0;

        double modDistance = Math.sqrt(playerPos.squaredDistanceTo(crystal));
        if (modDistance > 12) return 0;

        double exposure = getExposure(crystal, player, playerPos, obsidianPos, ignoreTerrain);
        double impact = (1 - (modDistance / 12)) * exposure;
        double damage = ((impact * impact + impact) / 2 * 7 * (6 * 2) + 1);

        damage = getDamageForDifficulty(damage);
        damage = DamageUtil.getDamageLeft((float) damage, (float) player.getArmor(), (float) player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue());
        damage = resistanceReduction(player, damage);

        Explosion explosion = new Explosion(OnyxClient.getClient().world, null, crystal.x, crystal.y, crystal.z, 6);
        damage = blastProtReduction(player, damage, explosion);

        return damage < 0 ? 0 : damage;
    }

    public static double crystalDamage(PlayerEntity player, Vec3d crystal, boolean predictMovement, BlockPos obsidianPos, boolean ignoreTerrain) {
        if (player == null) return 0;

        Vec3d playerPos = new Vec3d(player.getPos().x, player.getPos().y, player.getPos().z);

        if (predictMovement)
            playerPos = new Vec3d(playerPos.x + player.getVelocity().x, playerPos.y + player.getVelocity().y, playerPos.z + player.getVelocity().z);

        double modDistance = Math.sqrt(playerPos.squaredDistanceTo(crystal));
        if (modDistance > 12) return 0;

        double exposure = getExposure(crystal, player, predictMovement, obsidianPos, ignoreTerrain);
        double impact = (1 - (modDistance / 12)) * exposure;
        double damage = ((impact * impact + impact) / 2 * 7 * (6 * 2) + 1);

        damage = getDamageForDifficulty(damage);
        damage = DamageUtil.getDamageLeft((float) damage, (float) player.getArmor(), (float) player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue());
        damage = resistanceReduction(player, damage);

        Explosion explosion = new Explosion(OnyxClient.getClient().world, null, crystal.x, crystal.y, crystal.z, 6);
        damage = blastProtReduction(player, damage, explosion);

        return damage < 0 ? 0 : damage;
    }

    public static double crystalDamage(PlayerEntity player, Vec3d crystal) {
        return crystalDamage(player, crystal, false, null, false);
    }

    public static double crystalDamage(PlayerEntity player, Vec3d playerPos, Vec3d crystal) {
        return crystalDamage(player, playerPos, crystal, null, false);
    }










    private static double normalProtReduction(Entity player, double damage) {
        int protLevel = EnchantmentHelper.getProtectionAmount(player.getArmorItems(), DamageSource.GENERIC);
        if (protLevel > 20) protLevel = 20;

        damage *= 1 - (protLevel / 25.0);
        return damage < 0 ? 0 : damage;
    }

    private static double blastProtReduction(Entity player, double damage, Explosion explosion) {
        int protLevel = EnchantmentHelper.getProtectionAmount(player.getArmorItems(), DamageSource.explosion(explosion));
        if (protLevel > 20) protLevel = 20;

        damage *= (1 - (protLevel / 25.0));
        return damage < 0 ? 0 : damage;
    }

    private static double resistanceReduction(LivingEntity player, double damage) {
        if (player.hasStatusEffect(StatusEffects.RESISTANCE)) {
            int lvl = (player.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1);
            damage *= (1 - (lvl * 0.2));
        }

        return damage < 0 ? 0 : damage;
    }

    private static double getDamageForDifficulty(double damage) {
        return switch (OnyxClient.getClient().world.getDifficulty()) {
            case PEACEFUL -> 0;
            case EASY     -> Math.min(damage / 2 + 1, damage);
            case HARD     -> damage * 3 / 2;
            default       -> damage;
        };
    }

    private static double getExposure(Vec3d source, Entity entity, boolean predictMovement, BlockPos obsidianPos, boolean ignoreTerrain) {
        Box box = entity.getBoundingBox();
        if (predictMovement) {
            Vec3d v = entity.getVelocity();
            box.offset(v.x, v.y, v.z);
        }

        double d = 1 / ((box.maxX - box.minX) * 2 + 1);
        double e = 1 / ((box.maxY - box.minY) * 2 + 1);
        double f = 1 / ((box.maxZ - box.minZ) * 2 + 1);
        double g = (1 - Math.floor(1 / d) * d) / 2;
        double h = (1 - Math.floor(1 / f) * f) / 2;

        if (!(d < 0) && !(e < 0) && !(f < 0)) {
            int i = 0;
            int j = 0;

            for (double k = 0; k <= 1; k += d) {
                for (double l = 0; l <= 1; l += e) {
                    for (double m = 0; m <= 1; m += f) {
                        double n = MathHelper.lerp(k, box.minX, box.maxX);
                        double o = MathHelper.lerp(l, box.minY, box.maxY);
                        double p = MathHelper.lerp(m, box.minZ, box.maxZ);

                        Vec3d vec3d = new Vec3d(n + g, o, p + h);

                        RaycastContext raycastContext = new RaycastContext(vec3d, source, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity);

                        if (raycast(raycastContext, obsidianPos, ignoreTerrain).getType() == HitResult.Type.MISS) i++;

                        j++;
                    }
                }
            }

            return (double) i / j;
        }

        return 0;
    }

    private static double getExposure(Vec3d source, Entity entity, Vec3d playerPos, BlockPos obsidianPos, boolean ignoreTerrain) {
        Box box = entity.getBoundingBox();
        Vec3d v = playerPos.subtract(entity.getPos());
        box.offset(v.x, v.y, v.z);

        double d = 1 / ((box.maxX - box.minX) * 2 + 1);
        double e = 1 / ((box.maxY - box.minY) * 2 + 1);
        double f = 1 / ((box.maxZ - box.minZ) * 2 + 1);
        double g = (1 - Math.floor(1 / d) * d) / 2;
        double h = (1 - Math.floor(1 / f) * f) / 2;

        if (!(d < 0) && !(e < 0) && !(f < 0)) {
            int i = 0;
            int j = 0;

            for (double k = 0; k <= 1; k += d) {
                for (double l = 0; l <= 1; l += e) {
                    for (double m = 0; m <= 1; m += f) {
                        double n = MathHelper.lerp(k, box.minX, box.maxX);
                        double o = MathHelper.lerp(l, box.minY, box.maxY);
                        double p = MathHelper.lerp(m, box.minZ, box.maxZ);

                        Vec3d vec3d = new Vec3d(n + g, o, p + h);

                        RaycastContext raycastContext = new RaycastContext(vec3d, source, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity);

                        if (raycast(raycastContext, obsidianPos, ignoreTerrain).getType() == HitResult.Type.MISS) i++;

                        j++;
                    }
                }
            }

            return (double) i / j;
        }

        return 0;
    }

    private static BlockHitResult raycast(RaycastContext context, BlockPos obsidianPos, boolean ignoreTerrain) {
        return BlockView.raycast(context.getStart(), context.getEnd(), context, (raycastContext, blockPos) -> {
            BlockState blockState;
            if (blockPos.equals(obsidianPos)) blockState = Blocks.OBSIDIAN.getDefaultState();
            else {
                blockState = OnyxClient.getClient().world.getBlockState(blockPos);
                if (blockState.getBlock().getBlastResistance() < 600 && ignoreTerrain) blockState = Blocks.AIR.getDefaultState();
            }

            Vec3d vec3d = raycastContext.getStart();
            Vec3d vec3d2 = raycastContext.getEnd();

            VoxelShape voxelShape = raycastContext.getBlockShape(blockState, OnyxClient.getClient().world, blockPos);
            BlockHitResult blockHitResult = OnyxClient.getClient().world.raycastBlock(vec3d, vec3d2, blockPos, voxelShape, blockState);
            VoxelShape voxelShape2 = VoxelShapes.empty();
            BlockHitResult blockHitResult2 = voxelShape2.raycast(vec3d, vec3d2, blockPos);

            double d = blockHitResult == null ? Double.MAX_VALUE : raycastContext.getStart().squaredDistanceTo(blockHitResult.getPos());
            double e = blockHitResult2 == null ? Double.MAX_VALUE : raycastContext.getStart().squaredDistanceTo(blockHitResult2.getPos());

            return d <= e ? blockHitResult : blockHitResult2;
        }, (raycastContext) -> {
            Vec3d vec3d = raycastContext.getStart().subtract(raycastContext.getEnd());
            return BlockHitResult.createMissed(raycastContext.getEnd(), Direction.getFacing(vec3d.x, vec3d.y, vec3d.z), new BlockPos(raycastContext.getEnd()));
        });
    }

}
