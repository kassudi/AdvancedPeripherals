package de.srendi.advancedperipherals.client;

import de.srendi.advancedperipherals.AdvancedPeripherals;
import de.srendi.advancedperipherals.common.argoggles.ARRenderAction;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the canvas of the AR goggles the client currently displays and renders it as a gui overlay.
 */
@Mod.EventBusSubscriber(modid = AdvancedPeripherals.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class HudOverlayHandler {

    private static final String OVERLAY_ID = "ar_hud";
    private static final List<ARRenderAction> CANVAS = new ArrayList<>();

    public static void updateCanvas(List<ARRenderAction> actions) {
        CANVAS.clear();
        CANVAS.addAll(actions);
    }

    public static void clearCanvas() {
        CANVAS.clear();
    }

    @SubscribeEvent
    public static void registerOverlay(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll(OVERLAY_ID, (gui, graphics, partialTick, width, height) -> {
            Minecraft mc = Minecraft.getInstance();
            if (CANVAS.isEmpty() || mc.options.hideGui) return;
            for (ARRenderAction action : CANVAS) {
                ARHudRenderer.draw(action, mc, graphics, width, height);
            }
        });
    }

    @Mod.EventBusSubscriber(modid = AdvancedPeripherals.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEvents {

        // Clear the canvas when the player joins or leaves a world to prevent old renders from other worlds/servers
        @SubscribeEvent
        public static void onLogin(ClientPlayerNetworkEvent.LoggingIn event) {
            clearCanvas();
        }

        @SubscribeEvent
        public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
            clearCanvas();
        }
    }
}
