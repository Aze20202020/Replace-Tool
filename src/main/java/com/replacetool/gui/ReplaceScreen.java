package com.replacetool.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.replacetool.network.NetworkHandler;
import com.replacetool.network.ReplaceBlocksPacket;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

public class ReplaceScreen extends AbstractContainerScreen<ReplaceMenu> {

    private EditBox blockInput;
    private String statusMessage = "";
    private int statusColor = 0xFFFFFF;

    private final BlockPos pos1;
    private final BlockPos pos2;

    public ReplaceScreen(ReplaceMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.pos1 = menu.pos1;
        this.pos2 = menu.pos2;
        this.imageWidth = 300;
        this.imageHeight = 180;
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        this.blockInput = new EditBox(this.font, x + 10, y + 80, 220, 20,
            Component.literal("Block ID"));
        this.blockInput.setMaxLength(200);
        this.blockInput.setHint(Component.literal("e.g. minecraft:stone"));
        this.addWidget(this.blockInput);
        this.setInitialFocus(this.blockInput);

        Button replaceButton = new Button(x + 10, y + 110, 140, 20,
            Component.literal("Replace with Block"), btn -> performReplace(false));
        this.addRenderableWidget(replaceButton);

        Button replaceAirButton = new Button(x + 160, y + 110, 130, 20,
            Component.literal("Replace with Air"), btn -> performReplace(true));
        this.addRenderableWidget(replaceAirButton);

        Button cancelButton = new Button(x + 10, y + 140, 80, 20,
            Component.literal("Cancel"), btn -> this.onClose());
        this.addRenderableWidget(cancelButton);
    }

    private void performReplace(boolean useAir) {
        String blockId;
        if (useAir) {
            blockId = "minecraft:air";
        } else {
            blockId = blockInput.getValue().trim();
            if (blockId.isEmpty()) {
                statusMessage = "Please enter a block ID!";
                statusColor = 0xFF4444;
                return;
            }
            // Validate block ID format
            ResourceLocation rl;
            try {
                rl = new ResourceLocation(blockId);
            } catch (Exception e) {
                statusMessage = "Invalid block ID format!";
                statusColor = 0xFF4444;
                return;
            }
            Optional<Block> blockOpt = Registry.BLOCK.getOptional(rl);
            if (blockOpt.isEmpty()) {
                statusMessage = "Block not found: " + blockId;
                statusColor = 0xFF4444;
                return;
            }
        }

        NetworkHandler.CHANNEL.sendToServer(new ReplaceBlocksPacket(pos1, pos2, blockId));
        this.onClose();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.renderTooltip(poseStack, mouseX, mouseY);
        this.blockInput.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Background panel
        fill(poseStack, x, y, x + imageWidth, y + imageHeight, 0xEE1A1A2E);
        fill(poseStack, x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, 0xEE16213E);

        // Top accent bar
        fill(poseStack, x, y, x + imageWidth, y + 4, 0xFFFFD700);

        // Bottom accent bar
        fill(poseStack, x, y + imageHeight - 4, x + imageWidth, y + imageHeight, 0xFFFFD700);

        // Title
        drawCenteredString(poseStack, this.font, "§l§6Replace Tool", this.width / 2, y + 10, 0xFFFFFF);

        // Divider
        fill(poseStack, x + 10, y + 22, x + imageWidth - 10, y + 23, 0x88AAAAAA);

        // Selection info
        long volume = (long)(Math.abs(pos2.getX() - pos1.getX()) + 1)
                    * (Math.abs(pos2.getY() - pos1.getY()) + 1)
                    * (Math.abs(pos2.getZ() - pos1.getZ()) + 1);

        this.font.draw(poseStack, "§7Pos1: §f" + pos1.getX() + ", " + pos1.getY() + ", " + pos1.getZ(), x + 10, y + 28, 0xFFFFFF);
        this.font.draw(poseStack, "§7Pos2: §f" + pos2.getX() + ", " + pos2.getY() + ", " + pos2.getZ(), x + 10, y + 40, 0xFFFFFF);
        this.font.draw(poseStack, "§7Volume: §e" + String.format("%,d", volume) + " blocks", x + 10, y + 52, 0xFFFFFF);

        // Block input label
        this.font.draw(poseStack, "§7Block to place (mod:block_name):", x + 10, y + 68, 0xFFFFFF);

        // Input box background
        fill(poseStack, x + 8, y + 78, x + 238, y + 102, 0xFF000000);
        fill(poseStack, x + 9, y + 79, x + 237, y + 101, 0xFF222222);

        // Status message
        if (!statusMessage.isEmpty()) {
            drawCenteredString(poseStack, this.font, statusMessage, this.width / 2, y + 162, statusColor);
        }
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        // Suppress default label rendering (title + inventory)
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Allow ESC to close, but let the edit box capture other keys
        if (keyCode == 256) { // ESC
            this.onClose();
            return true;
        }
        if (this.blockInput.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
