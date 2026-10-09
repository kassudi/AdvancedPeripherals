package de.srendi.advancedperipherals.common.argoggles;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * A single drawing instruction of the AR overlay. This class only holds the data and the coordinate math, so it is safe
 * to load on a dedicated server. The actual drawing happens in the client-only
 * {@link de.srendi.advancedperipherals.client.ARHudRenderer}.
 */
public final class ARRenderAction implements INBTSerializable<CompoundTag> {

    private static final String TYPE = "type";
    private static final String STRING_ARG = "string_arg";
    private static final String INT_ARGS = "int_args";
    private static final String VIRTUAL_SCREEN_SIZE = "virtualScreenSize";

    private String id;
    private RenderActionType type;
    private String stringArg = "";
    private int[] intArgs = new int[0];
    private Optional<int[]> virtualScreenSize = Optional.empty();

    public ARRenderAction() {

    }

    public ARRenderAction(String id, RenderActionType type, int... intArgs) {
        this();
        this.id = id;
        this.type = type;
        this.intArgs = intArgs;
    }

    public ARRenderAction(RenderActionType type, int... intArgs) {
        this(null, type, intArgs);
    }

    public ARRenderAction(RenderActionType type, String stringArg, int... intArgs) {
        this(null, type, stringArg, intArgs);
    }

    public ARRenderAction(String id, RenderActionType type, String stringArg, int... intArgs) {
        this(id, type, intArgs);
        this.stringArg = stringArg;
    }

    public static ARRenderAction deserialize(CompoundTag nbt) {
        ARRenderAction action = new ARRenderAction();
        action.deserializeNBT(nbt);
        return action;
    }

    public String getId() {
        return id;
    }

    public RenderActionType getType() {
        return type;
    }

    public String getStringArg() {
        return stringArg;
    }

    public int[] getIntArgs() {
        return intArgs;
    }

    /**
     * Converts a x coordinate from the coordinate space of the computer to the actual screen. Negative values count from the right side.
     */
    public int relativeX(int x, int windowWidth) {
        if (virtualScreenSize.isPresent()) {
            x = x >= 0 ? x : virtualScreenSize.get()[0] + x;
            return (int) Math.round((double) x / virtualScreenSize.get()[0] * windowWidth);
        }
        return x >= 0 ? x : windowWidth + x;
    }

    /**
     * Converts a y coordinate from the coordinate space of the computer to the actual screen. Negative values count from the bottom.
     */
    public int relativeY(int y, int windowHeight) {
        if (virtualScreenSize.isPresent()) {
            y = y >= 0 ? y : virtualScreenSize.get()[1] + y;
            return (int) Math.round((double) y / virtualScreenSize.get()[1] * windowHeight);
        }
        return y >= 0 ? y : windowHeight + y;
    }

    public float relativeAverage(int i, int w, int h) {
        if (virtualScreenSize.isPresent()) {
            float xFactor = (float) w / virtualScreenSize.get()[0];
            float yFactor = (float) h / virtualScreenSize.get()[1];
            return i * (xFactor + yFactor) / 2;
        }
        return i;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ARRenderAction renderAction) {
            return type == renderAction.type && stringArg.equals(renderAction.stringArg) && Arrays.equals(intArgs, renderAction.intArgs);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, type, stringArg);
        result = 31 * result + Arrays.hashCode(intArgs);
        return result;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        int[] virtualScreenSizeFromNbt = nbt.getIntArray(VIRTUAL_SCREEN_SIZE);

        type = RenderActionType.valueOf(nbt.getString(TYPE));
        stringArg = nbt.getString(STRING_ARG);
        intArgs = nbt.getIntArray(INT_ARGS);
        virtualScreenSize = virtualScreenSizeFromNbt.length == 0 ? Optional.empty() : Optional.of(virtualScreenSizeFromNbt);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString(TYPE, type.toString());
        nbt.putString(STRING_ARG, stringArg);
        nbt.putIntArray(INT_ARGS, intArgs);
        nbt.putIntArray(VIRTUAL_SCREEN_SIZE, virtualScreenSize.orElse(new int[]{}));
        return nbt;
    }

    public ARRenderAction copyWithVirtualScreenSize(Optional<int[]> virtualScreenSize2) {
        ARRenderAction action = new ARRenderAction(id, type, stringArg, intArgs);
        virtualScreenSize2.ifPresent(size -> action.virtualScreenSize = Optional.of(new int[]{size[0], size[1]}));
        return action;
    }
}
