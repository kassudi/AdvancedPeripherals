package de.srendi.advancedperipherals.common.blocks.remote;

import de.srendi.advancedperipherals.common.setup.APBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Full block that joins all probes with the same frequency into one wired network.
 */
public class RemoteHubBlock extends RemoteLinkBlock {

    @Override
    @Nullable
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return APBlockEntityTypes.REMOTE_HUB.get().create(pos, state);
    }
}
