package com.replacetool.gui;

import com.replacetool.ReplaceTool;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ReplaceMenu extends AbstractContainerMenu {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
        DeferredRegister.create(ForgeRegistries.MENU_TYPES, ReplaceTool.MODID);

    public static final RegistryObject<MenuType<ReplaceMenu>> TYPE =
        MENU_TYPES.register("replace_menu", () ->
            IForgeMenuType.create((windowId, inv, data) -> {
                BlockPos pos1 = data.readBlockPos();
                BlockPos pos2 = data.readBlockPos();
                return new ReplaceMenu(windowId, inv, pos1, pos2);
            })
        );

    public final BlockPos pos1;
    public final BlockPos pos2;

    public ReplaceMenu(int windowId, Inventory inv, BlockPos pos1, BlockPos pos2) {
        super(TYPE.get(), windowId);
        this.pos1 = pos1;
        this.pos2 = pos2;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.isCreative();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
