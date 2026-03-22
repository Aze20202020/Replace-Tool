package com.replacetool.util;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Set;

/**
 * Represents a pending block-replacement job.
 * Each server tick, a batch of BLOCKS_PER_TICK blocks is processed.
 */
public class ReplaceTask {

    /** How many blocks to place per server tick (20 tps → ~1.3M blocks/sec at 65536/tick). */
    private static final int BLOCKS_PER_TICK = 65536;

    private final ServerLevel level;
    private final ServerPlayer player;
    private final List<BlockPos> positions;
    private final BlockState targetState;
    private final Set<ChunkPos> forcedChunks;

    private int cursor = 0;
    private final long total;

    public ReplaceTask(ServerLevel level, ServerPlayer player,
                       List<BlockPos> positions, BlockState targetState,
                       Set<ChunkPos> forcedChunks) {
        this.level = level;
        this.player = player;
        this.positions = positions;
        this.targetState = targetState;
        this.forcedChunks = forcedChunks;
        this.total = positions.size();
    }

    /**
     * Called once per tick. Returns true when the task is complete.
     */
    public boolean tick() {
        if (cursor >= positions.size()) return true;

        int end = Math.min(cursor + BLOCKS_PER_TICK, positions.size());
        for (int i = cursor; i < end; i++) {
            level.setBlock(positions.get(i), targetState, Block.UPDATE_ALL);
        }

        int prevBatch = cursor / BLOCKS_PER_TICK;
        cursor = end;
        int newBatch = cursor / BLOCKS_PER_TICK;

        // Send progress every 8 batches (~every ~0.5M blocks)
        if (newBatch != prevBatch && newBatch % 8 == 0 && cursor < total) {
            long pct = cursor * 100 / total;
            player.sendSystemMessage(Component.literal(
                "§7Replace progress: §e" + pct + "% §7("
                + String.format("%,d", cursor) + "/" + String.format("%,d", total) + ")"));
        }

        if (cursor >= positions.size()) {
            // Un-force chunks and notify completion
            for (ChunkPos cp : forcedChunks) {
                level.setChunkForced(cp.x, cp.z, false);
            }
            player.sendSystemMessage(Component.literal(
                "§a✔ Replacement complete! §e"
                + String.format("%,d", total) + " §ablocks replaced."));
            return true;
        }
        return false;
    }
}
