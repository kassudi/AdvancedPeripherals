package de.srendi.advancedperipherals.common.blocks.remote;

import de.srendi.advancedperipherals.common.blocks.base.IHarvestableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.Nullable;

/**
 * Shared behaviour of the remote probe and the remote hub: the frequency is set by right clicking with dyes.
 */
public abstract class RemoteLinkBlock extends Block implements EntityBlock, IHarvestableBlock {

    protected RemoteLinkBlock() {
        super(Properties.of().sound(SoundType.METAL).mapColor(MapColor.METAL).strength(1.5f, 6f).noOcclusion());
    }

    @Override
    public TagKey<Block> getHarvestTag() {
        return Tags.Blocks.NEEDS_WOOD_TOOL;
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (!(entity instanceof RemoteLinkEntity link)) {
            return InteractionResult.PASS;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.isEmpty() && !(stack.getItem() instanceof DyeItem)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (stack.getItem() instanceof DyeItem dye) {
            link.useDye(player, dye.getDyeColor());
        } else if (player.isShiftKeyDown()) {
            link.resetFrequency(player);
        } else {
            link.showFrequency(player);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return (lvl, pos, st, entity) -> {
            if (entity instanceof RemoteLinkEntity link) {
                link.serverTick();
            }
        };
    }
}
