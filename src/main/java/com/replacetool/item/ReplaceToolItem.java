package com.replacetool.item;

import com.replacetool.gui.ReplaceMenuProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

public class ReplaceToolItem extends Item {

    public static final String TAG_POS1 = "Pos1";
    public static final String TAG_POS2 = "Pos2";
    public static final String TAG_STAGE = "Stage";

    public ReplaceToolItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        Level level = ctx.getLevel();
        if (player == null) return InteractionResult.PASS;

        if (!player.isCreative()) {
            if (!level.isClientSide) {
                player.sendSystemMessage(Component.literal("§cThe Replace Tool is only usable in Creative Mode!"));
            }
            return InteractionResult.FAIL;
        }

        BlockPos clicked = ctx.getClickedPos();
        ItemStack stack = ctx.getItemInHand();
        CompoundTag tag = stack.getOrCreateTag();
        int stage = tag.getInt(TAG_STAGE);

        if (stage == 0) {
            savePos(tag, TAG_POS1, clicked);
            tag.putInt(TAG_STAGE, 1);
            if (!level.isClientSide) {
                player.sendSystemMessage(Component.literal("§aPosition 1 set: " + formatPos(clicked)));
            }
            return InteractionResult.SUCCESS;
        } else if (stage == 1) {
            savePos(tag, TAG_POS2, clicked);
            tag.putInt(TAG_STAGE, 2);
            if (!level.isClientSide) {
                player.sendSystemMessage(Component.literal("§aPosition 2 set: " + formatPos(clicked)));
                player.sendSystemMessage(Component.literal("§eRight-click any block again to open the Replace GUI."));
            }
            return InteractionResult.SUCCESS;
        } else {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                BlockPos pos1 = loadPos(tag, TAG_POS1);
                BlockPos pos2 = loadPos(tag, TAG_POS2);
                if (pos1 != null && pos2 != null) {
                    NetworkHooks.openScreen(serverPlayer,
                        new ReplaceMenuProvider(pos1, pos2),
                        buf -> {
                            buf.writeBlockPos(pos1);
                            buf.writeBlockPos(pos2);
                        });
                }
            }
            tag.putInt(TAG_STAGE, 0);
            tag.remove(TAG_POS1);
            tag.remove(TAG_POS2);
            return InteractionResult.SUCCESS;
        }
    }

    private void savePos(CompoundTag tag, String key, BlockPos pos) {
        CompoundTag posTag = new CompoundTag();
        posTag.putInt("X", pos.getX());
        posTag.putInt("Y", pos.getY());
        posTag.putInt("Z", pos.getZ());
        tag.put(key, posTag);
    }

    @Nullable
    private BlockPos loadPos(CompoundTag tag, String key) {
        if (!tag.contains(key)) return null;
        CompoundTag posTag = tag.getCompound(key);
        return new BlockPos(posTag.getInt("X"), posTag.getInt("Y"), posTag.getInt("Z"));
    }

    private String formatPos(BlockPos pos) {
        return "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Right-click to set corner 1").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Right-click again to set corner 2").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Right-click once more to open Replace GUI").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Creative Mode only").withStyle(ChatFormatting.RED));

        CompoundTag tag = stack.getTag();
        if (tag != null) {
            int stage = tag.getInt(TAG_STAGE);
            if (stage >= 1) {
                CompoundTag p = tag.getCompound(TAG_POS1);
                tooltip.add(Component.literal("Pos1: " + p.getInt("X") + "," + p.getInt("Y") + "," + p.getInt("Z")).withStyle(ChatFormatting.GREEN));
            }
            if (stage >= 2) {
                CompoundTag p = tag.getCompound(TAG_POS2);
                tooltip.add(Component.literal("Pos2: " + p.getInt("X") + "," + p.getInt("Y") + "," + p.getInt("Z")).withStyle(ChatFormatting.GREEN));
            }
        }
    }
}
