package com.replacetool.gui;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import javax.annotation.Nullable;

public class ReplaceMenuProvider implements MenuProvider {

    private final BlockPos pos1;
    private final BlockPos pos2;

    public ReplaceMenuProvider(BlockPos pos1, BlockPos pos2) {
        this.pos1 = pos1;
        this.pos2 = pos2;
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Replace Tool");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
        return new ReplaceMenu(windowId, inv, pos1, pos2);
    }

    public static void register() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ReplaceMenuProvider::registerClient);
    }

    private static void registerClient() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ReplaceMenuProvider::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        net.minecraft.client.gui.screens.MenuScreens.register(
            ReplaceMenu.TYPE,
            ReplaceScreen::new
        );
    }
}
