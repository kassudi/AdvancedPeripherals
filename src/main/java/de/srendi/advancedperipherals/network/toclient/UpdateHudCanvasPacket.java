package de.srendi.advancedperipherals.network.toclient;

import de.srendi.advancedperipherals.client.HudOverlayHandler;
import de.srendi.advancedperipherals.common.argoggles.ARRenderAction;
import de.srendi.advancedperipherals.network.base.IPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;

public class UpdateHudCanvasPacket implements IPacket {

    private static final String LIST = "list";

    private final List<ARRenderAction> canvas;

    public UpdateHudCanvasPacket(List<ARRenderAction> canvas) {
        this.canvas = canvas;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        if (!FMLEnvironment.dist.isClient())
            return;
        HudOverlayHandler.updateCanvas(canvas);
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        ListTag list = new ListTag();
        canvas.forEach(action -> list.add(action.serializeNBT()));
        CompoundTag nbt = new CompoundTag();
        nbt.put(LIST, list);
        buffer.writeNbt(nbt);
    }

    public static UpdateHudCanvasPacket decode(FriendlyByteBuf buffer) {
        List<ARRenderAction> canvas = new ArrayList<>();
        CompoundTag nbt = buffer.readNbt();
        if (nbt != null) {
            ListTag list = nbt.getList(LIST, Tag.TAG_COMPOUND);
            list.forEach(tag -> canvas.add(ARRenderAction.deserialize((CompoundTag) tag)));
        }
        return new UpdateHudCanvasPacket(canvas);
    }
}
