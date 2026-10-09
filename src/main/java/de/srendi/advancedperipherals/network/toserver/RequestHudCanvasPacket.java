package de.srendi.advancedperipherals.network.toserver;

import de.srendi.advancedperipherals.common.blocks.blockentities.ARControllerEntity;
import de.srendi.advancedperipherals.common.items.ARGogglesItem;
import de.srendi.advancedperipherals.network.APNetworking;
import de.srendi.advancedperipherals.network.base.IPacket;
import de.srendi.advancedperipherals.network.toclient.ClearHudCanvasPacket;
import de.srendi.advancedperipherals.network.toclient.UpdateHudCanvasPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

/**
 * Sent by clients wearing linked AR goggles whose controller is not loaded on the client, for example because it is too far away.
 */
public class RequestHudCanvasPacket implements IPacket {

    private final BlockPos blockPos;
    private final String dimensionKey;

    public RequestHudCanvasPacket(BlockPos blockPos, String dimensionKey) {
        this.blockPos = blockPos;
        this.dimensionKey = dimensionKey;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayer sender = context.getSender();
        if (sender == null)
            return;
        for (ServerLevel level : sender.getServer().getAllLevels()) {
            if (!ARGogglesItem.dimensionMatches(dimensionKey, level.dimension()))
                continue;
            // Do not let clients force the server to load chunks
            if (!level.hasChunkAt(blockPos))
                return;
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity instanceof ARControllerEntity controller)
                APNetworking.sendTo(new UpdateHudCanvasPacket(controller.getCanvas()), sender);
            else
                // The controller was removed, do not leave an outdated overlay on the screen
                APNetworking.sendTo(new ClearHudCanvasPacket(), sender);
            return;
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(blockPos);
        buffer.writeUtf(dimensionKey, Short.MAX_VALUE);
    }

    public static RequestHudCanvasPacket decode(FriendlyByteBuf buffer) {
        return new RequestHudCanvasPacket(buffer.readBlockPos(), buffer.readUtf(Short.MAX_VALUE));
    }
}
