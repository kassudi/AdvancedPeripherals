package de.srendi.advancedperipherals.common.blocks.remote;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.impl.Peripherals;
import dan200.computercraft.shared.computer.core.ServerContext;
import dan200.computercraft.shared.platform.InvalidateCallback;
import de.srendi.advancedperipherals.common.setup.APBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * The machine side: publishes the peripheral of the block it sits against on the wired network, the same way a wired
 * modem does, so it shows up on the computer as e.g. gtceu:large_plasma_turbine_3.
 */
public class RemoteProbeEntity extends RemoteLinkEntity {

    private static final InvalidateCallback NO_CALLBACK = () -> {
    };

    private int id = -1;
    @Nullable
    private String type;
    @Nullable
    private IPeripheral peripheral;

    public RemoteProbeEntity(BlockPos pos, BlockState state) {
        super(APBlockEntityTypes.REMOTE_PROBE.get(), pos, state);
    }

    @Override
    public boolean isHub() {
        return false;
    }

    private Direction facing() {
        return getBlockState().getValue(RemoteProbeBlock.FACING);
    }

    @Override
    protected void onRegistered() {
        // the node is new, so whatever we published before is gone
        peripheral = null;
        refresh();
    }

    @Override
    protected void refresh() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        Direction facing = facing();
        BlockPos target = worldPosition.relative(facing);
        IPeripheral found = serverLevel.isLoaded(target)
            ? Peripherals.getPeripheral(serverLevel, target, facing.getOpposite(), NO_CALLBACK)
            : peripheral;
        if (sameAs(found, peripheral)) {
            return;
        }
        peripheral = found;
        if (found == null) {
            getNode().updatePeripherals(Map.of());
            return;
        }
        String foundType = found.getType();
        if (id < 0 || !foundType.equals(type)) {
            type = foundType;
            id = ServerContext.get(serverLevel.getServer()).getNextId("peripheral." + foundType);
            setChanged();
        }
        getNode().updatePeripherals(Map.of(type + "_" + id, found));
    }

    private static boolean sameAs(@Nullable IPeripheral a, @Nullable IPeripheral b) {
        return a == b || (a != null && b != null && a.equals(b));
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        id = tag.contains("PeripheralId") ? tag.getInt("PeripheralId") : -1;
        type = tag.contains("PeripheralType") ? tag.getString("PeripheralType") : null;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        if (id >= 0) {
            tag.putInt("PeripheralId", id);
        }
        if (type != null) {
            tag.putString("PeripheralType", type);
        }
    }
}
