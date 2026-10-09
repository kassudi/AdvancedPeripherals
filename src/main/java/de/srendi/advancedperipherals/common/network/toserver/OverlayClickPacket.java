package de.srendi.advancedperipherals.common.network.toserver;

import de.srendi.advancedperipherals.common.items.SmartGlassesItem;
import de.srendi.advancedperipherals.common.network.IAPPacket;
import de.srendi.advancedperipherals.common.setup.CCEvents;
import de.srendi.advancedperipherals.common.smartglasses.SmartGlassesComputer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

/**
 * Sent when the player clicks with the free mouse cursor while the keyboard screen is open.
 * The position is in GUI-scaled pixels, the same coordinates the overlay module uses for 2D objects.
 */
public class OverlayClickPacket implements IAPPacket {

    private final double x;
    private final double y;
    private final int button;

    public OverlayClickPacket(double x, double y, int button) {
        this.x = x;
        this.y = y;
        this.button = button;
    }

    public OverlayClickPacket(FriendlyByteBuf buffer) {
        this.x = buffer.readDouble();
        this.y = buffer.readDouble();
        this.button = buffer.readVarInt();
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) {
            return;
        }

        ItemStack smartGlasses = SmartGlassesItem.getEquipped(player);
        if (smartGlasses.isEmpty()) {
            return;
        }
        SmartGlassesComputer computer = SmartGlassesItem.getServerComputer(player.server, smartGlasses);
        if (computer == null) {
            return;
        }
        computer.queueEvent(CCEvents.OVERLAY_CLICK, new Object[]{x, y, button});
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeVarInt(button);
    }
}
