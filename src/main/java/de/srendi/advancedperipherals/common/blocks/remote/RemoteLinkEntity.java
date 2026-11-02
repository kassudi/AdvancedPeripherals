package de.srendi.advancedperipherals.common.blocks.remote;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.network.wired.WiredElement;
import dan200.computercraft.api.network.wired.WiredNode;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * A block that is a node of a wired network and links itself, without any cable, to the nodes of the opposite kind
 * (probe to hub and the other way round) which carry the same three dye colours.
 */
public abstract class RemoteLinkEntity extends BlockEntity implements WiredElement {

    private WiredNode node = ComputerCraftAPI.createWiredNodeForElement(this);
    private final Set<RemoteLinkEntity> linked = new HashSet<>();
    private final byte[] colors = new byte[3];
    private int slot = 0;
    private boolean registered = false;
    private int ticks = 0;

    protected RemoteLinkEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public abstract boolean isHub();

    /**
     * Called every second on the server.
     */
    protected abstract void refresh();

    protected abstract void onRegistered();

    public int frequency() {
        return (colors[0] & 15) | ((colors[1] & 15) << 4) | ((colors[2] & 15) << 8);
    }

    public String frequencyName() {
        return DyeColor.byId(colors[0]).getName() + ", " + DyeColor.byId(colors[1]).getName() + ", " + DyeColor.byId(colors[2]).getName();
    }

    public void useDye(Player player, DyeColor color) {
        colors[slot] = (byte) color.getId();
        slot = (slot + 1) % 3;
        changedFrequency(player);
    }

    public void resetFrequency(Player player) {
        colors[0] = colors[1] = colors[2] = 0;
        slot = 0;
        changedFrequency(player);
    }

    public void showFrequency(Player player) {
        player.displayClientMessage(Component.literal("Frequency: " + frequencyName() + " (next dye sets colour " + (slot + 1) + ")"), true);
    }

    private void changedFrequency(Player player) {
        setChanged();
        if (registered) {
            RemoteLinks.relink(this);
        }
        showFrequency(player);
    }

    // --- linking ---

    void link(RemoteLinkEntity other) {
        if (linked.add(other)) {
            other.linked.add(this);
            node.connectTo(other.node);
        }
    }

    void unlinkAll() {
        for (RemoteLinkEntity other : linked) {
            node.disconnectFrom(other.node);
            other.linked.remove(this);
        }
        linked.clear();
    }

    private void register() {
        if (registered || !(level instanceof ServerLevel)) {
            return;
        }
        registered = true;
        node = ComputerCraftAPI.createWiredNodeForElement(this);
        RemoteLinks.add(this);
        onRegistered();
    }

    private void unregister() {
        if (!registered) {
            return;
        }
        registered = false;
        RemoteLinks.remove(this);
        node.remove();
    }

    public void serverTick() {
        if (!registered) {
            register();
        }
        if (++ticks >= 20) {
            ticks = 0;
            refresh();
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        register();
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        register();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        unregister();
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        unregister();
    }

    // --- WiredElement ---

    @Override
    public WiredNode getNode() {
        return node;
    }

    @Override
    public String getSenderID() {
        return "remote_link";
    }

    @Override
    public Vec3 getPosition() {
        return Vec3.atCenterOf(getBlockPos());
    }

    // --- saving ---

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        byte[] saved = tag.getByteArray("Colors");
        for (int i = 0; i < 3; i++) {
            colors[i] = i < saved.length ? (byte) (saved[i] & 15) : 0;
        }
        slot = Math.floorMod(tag.getInt("Slot"), 3);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putByteArray("Colors", colors.clone());
        tag.putInt("Slot", slot);
    }
}
