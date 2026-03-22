package com.replacetool.network;

import com.replacetool.util.ReplaceTask;
import com.replacetool.util.ReplaceTaskQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

import java.util.*;
import java.util.function.Supplier;

public class ReplaceBlocksPacket {

    private final BlockPos pos1;
    private final BlockPos pos2;
    private final String blockId;

    public ReplaceBlocksPacket(BlockPos pos1, BlockPos pos2, String blockId) {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.blockId = blockId;
    }

    public static void encode(ReplaceBlocksPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos1);
        buf.writeBlockPos(packet.pos2);
        buf.writeUtf(packet.blockId, 32767);
    }

    public static ReplaceBlocksPacket decode(FriendlyByteBuf buf) {
        BlockPos p1 = buf.readBlockPos();
        BlockPos p2 = buf.readBlockPos();
        String id = buf.readUtf(32767);
        return new ReplaceBlocksPacket(p1, p2, id);
    }

    public static void handle(ReplaceBlocksPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null || !player.isCreative()) return;

            ServerLevel level = player.getLevel();

            // Resolve target block state
            BlockState targetState;
            if ("minecraft:air".equals(packet.blockId)) {
                targetState = Blocks.AIR.defaultBlockState();
            } else {
                ResourceLocation rl;
                try {
                    rl = new ResourceLocation(packet.blockId);
                } catch (Exception e) {
                    player.sendSystemMessage(Component.literal("§cInvalid block ID: " + packet.blockId));
                    return;
                }
                Optional<Block> blockOpt = Registry.BLOCK.getOptional(rl);
                if (blockOpt.isEmpty()) {
                    player.sendSystemMessage(Component.literal("§cBlock not found: " + packet.blockId));
                    return;
                }
                targetState = blockOpt.get().defaultBlockState();
            }

            int minX = Math.min(packet.pos1.getX(), packet.pos2.getX());
            int minY = Math.min(packet.pos1.getY(), packet.pos2.getY());
            int minZ = Math.min(packet.pos1.getZ(), packet.pos2.getZ());
            int maxX = Math.max(packet.pos1.getX(), packet.pos2.getX());
            int maxY = Math.max(packet.pos1.getY(), packet.pos2.getY());
            int maxZ = Math.max(packet.pos1.getZ(), packet.pos2.getZ());

            // Collect all chunk positions and force-load them
            Set<ChunkPos> chunks = new LinkedHashSet<>();
            for (int cx = minX >> 4; cx <= maxX >> 4; cx++) {
                for (int cz = minZ >> 4; cz <= maxZ >> 4; cz++) {
                    chunks.add(new ChunkPos(cx, cz));
                }
            }
            for (ChunkPos cp : chunks) {
                level.setChunkForced(cp.x, cp.z, true);
            }

            // Collect all positions
            List<BlockPos> positions = new ArrayList<>();
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        positions.add(new BlockPos(x, y, z));
                    }
                }
            }

            long volume = positions.size();
            player.sendSystemMessage(Component.literal(
                "§aStarting replacement of §e" + String.format("%,d", volume)
                + " §ablocks → §e" + packet.blockId));

            // Enqueue the task — processed tick by tick via the ServerTickEvent
            ReplaceTaskQueue.INSTANCE.enqueue(new ReplaceTask(level, player, positions, targetState, chunks));
        });
        ctx.setPacketHandled(true);
    }
}
