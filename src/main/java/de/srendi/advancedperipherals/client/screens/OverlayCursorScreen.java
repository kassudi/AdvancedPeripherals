package de.srendi.advancedperipherals.client.screens;

import com.mojang.blaze3d.platform.InputConstants;
import de.srendi.advancedperipherals.common.network.APNetworking;
import de.srendi.advancedperipherals.common.network.toserver.OverlayClickPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * An empty screen that only exists to free the mouse cursor while left alt is held.
 * Clicks are reported to the glasses computer as overlay_click events. The screen closes
 * as soon as alt is released, which hands the mouse back to the game.
 */
public class OverlayCursorScreen extends Screen {

    public OverlayCursorScreen() {
        super(Component.empty());
    }

    public static boolean isAltDown() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_ALT);
    }

    @Override
    public void tick() {
        if (!isAltDown()) {
            this.onClose();
        }
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        APNetworking.sendToServer(new OverlayClickPacket(x, y, button + 1));
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
