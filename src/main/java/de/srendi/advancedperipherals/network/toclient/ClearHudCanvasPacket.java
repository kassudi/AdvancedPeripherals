package de.srendi.advancedperipherals.network.toclient;

import de.srendi.advancedperipherals.client.HudOverlayHandler;
import de.srendi.advancedperipherals.network.base.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

public class ClearHudCanvasPacket implements IPacket {

    @Override
    public void handle(NetworkEvent.Context context) {
        if (!FMLEnvironment.dist.isClient())
            return;
        HudOverlayHandler.clearCanvas();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
    }

    public static ClearHudCanvasPacket decode(FriendlyByteBuf buffer) {
        return new ClearHudCanvasPacket();
    }
}
