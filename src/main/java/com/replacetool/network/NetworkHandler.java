package com.replacetool.network;

import com.replacetool.ReplaceTool;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {

    private static final String PROTOCOL = "1";
    public static SimpleChannel CHANNEL;

    public static void register() {
        CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ReplaceTool.MODID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
        );

        CHANNEL.registerMessage(0, ReplaceBlocksPacket.class,
            ReplaceBlocksPacket::encode,
            ReplaceBlocksPacket::decode,
            ReplaceBlocksPacket::handle
        );
    }
}
