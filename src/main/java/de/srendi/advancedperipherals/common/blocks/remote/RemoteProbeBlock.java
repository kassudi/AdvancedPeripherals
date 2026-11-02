package de.srendi.advancedperipherals.common.blocks.remote;

import de.srendi.advancedperipherals.common.setup.APBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Modem sized block that sits against a machine. FACING is the side the machine is on.
 */
public class RemoteProbeBlock extends RemoteLinkBlock {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private static final VoxelShape NORTH = Block.box(3, 3, 0, 13, 13, 4);
    private static final VoxelShape SOUTH = Block.box(3, 3, 12, 13, 13, 16);
    private static final VoxelShape WEST = Block.box(0, 3, 3, 4, 13, 13);
    private static final VoxelShape EAST = Block.box(12, 3, 3, 16, 13, 13);
    private static final VoxelShape DOWN = Block.box(3, 0, 3, 13, 4, 13);
    private static final VoxelShape UP = Block.box(3, 12, 3, 13, 16, 13);

    public RemoteProbeBlock() {
        super();
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite());
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case EAST -> EAST;
            case DOWN -> DOWN;
            case UP -> UP;
        };
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return APBlockEntityTypes.REMOTE_PROBE.get().create(pos, state);
    }
}
