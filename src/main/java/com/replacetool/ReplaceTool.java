package com.replacetool;

import com.replacetool.gui.ReplaceMenu;
import com.replacetool.gui.ReplaceMenuProvider;
import com.replacetool.item.ReplaceToolItem;
import com.replacetool.network.NetworkHandler;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(ReplaceTool.MODID)
public class ReplaceTool {
    public static final String MODID = "replacetool";

    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<Item> REPLACE_TOOL_ITEM =
        ITEMS.register("replace_tool", () -> new ReplaceToolItem(
            new Item.Properties()
                .stacksTo(1)
                .tab(CreativeModeTab.TAB_TOOLS)
        ));

    public ReplaceTool() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);
        ReplaceMenu.MENU_TYPES.register(bus);
        bus.addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(FMLCommonSetupEvent event) {
        NetworkHandler.register();
        ReplaceMenuProvider.register();
    }
}
