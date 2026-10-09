package de.srendi.advancedperipherals.common.blocks.blockentities;

import de.srendi.advancedperipherals.common.addons.computercraft.peripheral.ARControllerPeripheral;
import de.srendi.advancedperipherals.common.argoggles.ARRenderAction;
import de.srendi.advancedperipherals.common.blocks.base.PeripheralBlockEntity;
import de.srendi.advancedperipherals.common.setup.BlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ARControllerEntity extends PeripheralBlockEntity<ARControllerPeripheral> {

    private static final String CANVAS = "canvas";
    private static final String VIRTUAL_SCREEN_SIZE = "virtual_screen_size";

    private final List<ARRenderAction> canvas = new ArrayList<>();
    private Optional<int[]> virtualScreenSize = Optional.empty();

    public ARControllerEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypes.AR_CONTROLLER.get(), pos, state);
    }

    /**
     * Adds a rendering action to the canvas. Won't add an action if it is already
     * present with exactly the same parameters, to avoid clutter.
     *
     * @param action The action to add to the canvas.
     */
    public void addToCanvas(ARRenderAction action) {
        if (canvas.contains(action))
            return;
        if (action.getId() != null)
            canvas.removeIf(old -> action.getId().equals(old.getId()));
        canvas.add(action);
        markDataSync();
    }

    public void clearCanvas() {
        canvas.clear();
        markDataSync();
    }

    public void clearElement(String id) {
        canvas.removeIf(old -> id.equals(old.getId()));
        markDataSync();
    }

    @NotNull
    @Override
    protected ARControllerPeripheral createPeripheral() {
        return new ARControllerPeripheral(this);
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        int[] size = nbt.getIntArray(VIRTUAL_SCREEN_SIZE);
        virtualScreenSize = size.length >= 2 ? Optional.of(size) : Optional.empty();

        ListTag list = nbt.getList(CANVAS, Tag.TAG_COMPOUND);
        canvas.clear();
        for (int i = 0; i < list.size(); i++) {
            canvas.add(ARRenderAction.deserialize(list.getCompound(i)));
        }
    }

    // The canvas is saved with the block entity and synced to clients, so goggles can read it without a request packet
    @Override
    protected void saveShared(@NotNull CompoundTag compound) {
        super.saveShared(compound);
        virtualScreenSize.ifPresent(size -> compound.putIntArray(VIRTUAL_SCREEN_SIZE, size));
        ListTag list = new ListTag();
        for (ARRenderAction action : canvas) {
            list.add(action.serializeNBT());
        }
        compound.put(CANVAS, list);
    }

    public boolean isRelativeMode() {
        return virtualScreenSize.isPresent();
    }

    public int[] getVirtualScreenSize() {
        return virtualScreenSize.orElse(null);
    }

    public void setRelativeMode(int virtualScreenWidth, int virtualScreenHeight) {
        virtualScreenSize = Optional.of(new int[]{virtualScreenWidth, virtualScreenHeight});
        markDataSync();
    }

    public void disableRelativeMode() {
        virtualScreenSize = Optional.empty();
        markDataSync();
    }

    /**
     * Returns a copy of the canvas with the virtual screen size added.
     */
    public List<ARRenderAction> getCanvas() {
        List<ARRenderAction> list = new ArrayList<>(canvas.size());
        for (ARRenderAction action : canvas) {
            list.add(action.copyWithVirtualScreenSize(virtualScreenSize));
        }
        return list;
    }
}
