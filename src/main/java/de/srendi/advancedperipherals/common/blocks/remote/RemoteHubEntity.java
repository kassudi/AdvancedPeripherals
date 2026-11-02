package de.srendi.advancedperipherals.common.blocks.remote;

import dan200.computercraft.api.ForgeComputerCraftAPI;
import de.srendi.advancedperipherals.common.setup.APBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The computer side: joins every probe with the same frequency into one wired network and connects that network to
 * the cables and modems around it, so a normal wired modem next to the hub gives the computer all the remote machines.
 */
public class RemoteHubEntity extends RemoteLinkEntity {

    public RemoteHubEntity(BlockPos pos, BlockState state) {
        super(APBlockEntityTypes.REMOTE_HUB.get(), pos, state);
    }

    @Override
    public boolean isHub() {
        return true;
    }

    @Override
    protected void onRegistered() {
        refresh();
    }

    @Override
    protected void refresh() {
        if (level == null) {
            return;
        }
        for (Direction direction : Direction.values()) {
            BlockPos offset = worldPosition.relative(direction);
            if (!level.isLoaded(offset)) {
                continue;
            }
            ForgeComputerCraftAPI.getWiredElementAt(level, offset, direction.getOpposite())
                .ifPresent(element -> getNode().connectTo(element.getNode()));
        }
    }
}
