package cope.cosmos.util.world;

import cope.cosmos.util.Wrapper;
import cope.cosmos.util.system.MathUtil;
import net.minecraft.block.BlockSlab;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class BlockUtil implements Wrapper {

    public static void placeBlock(BlockPos blockPos, boolean packet, boolean antiGlitch) {
        for (EnumFacing enumFacing : EnumFacing.values()) {
            if (!(getBlockResistance(blockPos.offset(enumFacing)) == BlockResistance.BLANK)) {
                for (Entity entity : mc.world.loadedEntityList) {
                    if (new AxisAlignedBB(blockPos).intersects(entity.getEntityBoundingBox()))
                        return;
                }

                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));

                if (packet) {
                    mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(blockPos.offset(enumFacing), enumFacing.getOpposite(), EnumHand.MAIN_HAND, 0, 0, 0));
                }

                else {
                    mc.playerController.processRightClickBlock(mc.player, mc.world, blockPos.offset(enumFacing), enumFacing.getOpposite(), new Vec3d(blockPos), EnumHand.MAIN_HAND);
                }

                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));

                if (antiGlitch) {
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, blockPos.offset(enumFacing), enumFacing.getOpposite()));
                }

                return;
            }
        }
    }

    public synchronized static List<BlockPos> getSurroundingBlocks(EntityPlayer player, double blockRange, boolean motion) {
        List<BlockPos> nearbyBlocks = new ArrayList<>();

        int rangeX = (int) (motion ? MathUtil.roundDouble(blockRange, 0) + player.motionX: MathUtil.roundDouble(blockRange, 0));
        int rangeY = (int) (motion ? MathUtil.roundDouble(blockRange, 0) + player.motionY: MathUtil.roundDouble(blockRange, 0));
        int rangeZ = (int) (motion ? MathUtil.roundDouble(blockRange, 0) + player.motionZ: MathUtil.roundDouble(blockRange, 0));
        for (int x = -rangeX; x <= rangeX; x++) {
            for (int y = -rangeY; y <= rangeY; y++) {
                for (int z = -rangeZ; z <= rangeZ; z++) {
                    nearbyBlocks.add(player.getPosition().add(x, y, z));
                }
            }
        }

        return nearbyBlocks.stream().filter(blockPos -> mc.player.getDistance(blockPos.getX() + 0.5, blockPos.getY() + 1, blockPos.getZ() + 0.5) <= blockRange).sorted(Comparator.comparing(blockPos -> mc.player.getDistanceSq(blockPos))).collect(Collectors.toList());
    }

    public static double getNearestBlockBelow() {
        for (double y = mc.player.posY; y > 0.0; y -= 0.001) {
            if (mc.world.getBlockState(new BlockPos(mc.player.posX, y, mc.player.posZ)).getBlock() instanceof BlockSlab || mc.world.getBlockState(new BlockPos(mc.player.posX, y, mc.player.posZ)).getBlock().getDefaultState().getCollisionBoundingBox(mc.world, new BlockPos(0, 0, 0)) == null) 
                continue;
            
            return y;
        }
        
        return -1;
    }

    @SuppressWarnings("deprecation")
    public static BlockResistance getBlockResistance(BlockPos block) {
        if (mc.world.isAirBlock(block)) {
            return BlockResistance.BLANK;
        }

        else if (mc.world.getBlockState(block).getBlock().getBlockHardness(mc.world.getBlockState(block), mc.world, block) != -1 && !(mc.world.getBlockState(block).getBlock().equals(Blocks.OBSIDIAN) || mc.world.getBlockState(block).getBlock().equals(Blocks.ANVIL) || mc.world.getBlockState(block).getBlock().equals(Blocks.ENCHANTING_TABLE) || mc.world.getBlockState(block).getBlock().equals(Blocks.ENDER_CHEST))) {
            return BlockResistance.BREAKABLE;
        }

        else if (mc.world.getBlockState(block).getBlock().equals(Blocks.OBSIDIAN) || mc.world.getBlockState(block).getBlock().equals(Blocks.ANVIL) || mc.world.getBlockState(block).getBlock().equals(Blocks.ENCHANTING_TABLE) || mc.world.getBlockState(block).getBlock().equals(Blocks.ENDER_CHEST)) {
            return BlockResistance.RESISTANT;
        }

        else if (mc.world.getBlockState(block).getBlock().equals(Blocks.BEDROCK) || mc.world.getBlockState(block).getBlock().equals(Blocks.BARRIER) || mc.world.getBlockState(block).getBlock().equals(Blocks.COMMAND_BLOCK) || mc.world.getBlockState(block).getBlock().equals(Blocks.CHAIN_COMMAND_BLOCK) || mc.world.getBlockState(block).getBlock().equals(Blocks.END_PORTAL_FRAME)) {
            return BlockResistance.UNBREAKABLE;
        }

        return BlockResistance.BLANK;
    }

    public enum BlockResistance {
        BLANK, BREAKABLE, RESISTANT, UNBREAKABLE
    }
}
