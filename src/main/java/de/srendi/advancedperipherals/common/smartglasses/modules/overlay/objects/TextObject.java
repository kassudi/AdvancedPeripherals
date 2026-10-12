package de.srendi.advancedperipherals.common.smartglasses.modules.overlay.objects;

import de.srendi.advancedperipherals.common.setup.APOverlayObjects;
import de.srendi.advancedperipherals.common.smartglasses.modules.overlay.OverlayModule;
import de.srendi.advancedperipherals.common.smartglasses.modules.overlay.OverlayObjectType;
import de.srendi.advancedperipherals.common.smartglasses.modules.overlay.propertytypes.BooleanProperty;
import de.srendi.advancedperipherals.common.smartglasses.modules.overlay.propertytypes.FloatingNumberProperty;
import de.srendi.advancedperipherals.common.smartglasses.modules.overlay.propertytypes.StringProperty;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TextObject extends ThreeDimensionalObject {
    @StringProperty(utf8 = true)
    public String content = "";

    @FloatingNumberProperty(min = 0, max = 128, lerp = true)
    public float fontSize = 1;

    @BooleanProperty(getterPrefix = "has")
    public boolean shadow = false;

    @BooleanProperty
    public boolean center = false;

    /**
     * Makes the text always face the camera. Only affects text in the world (not gui text),
     * the position is still absolute (or relative if relativePosition is set).
     */
    @BooleanProperty
    public boolean billboard = false;

    public TextObject(OverlayModule module) {
        super(module);
    }

    public TextObject(UUID player) {
        super(player);
    }

    @Override
    @NotNull
    public OverlayObjectType<TextObject> getType() {
        return APOverlayObjects.TEXT.get();
    }
}
