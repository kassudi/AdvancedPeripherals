package de.srendi.advancedperipherals.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import de.srendi.advancedperipherals.AdvancedPeripherals;
import de.srendi.advancedperipherals.common.argoggles.ARRenderAction;
import de.srendi.advancedperipherals.common.util.inventory.ItemUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix4f;

/**
 * Executes {@link ARRenderAction}s on the client. Client only.
 */
public final class ARHudRenderer {

    private static final int CIRCLE_SEGMENTS = 360;

    private ARHudRenderer() {
    }

    public static int fixAlpha(int color) {
        return (color & 0xFF000000) == 0 ? color | 0xFF000000 : color;
    }

    /**
     * @param w the width of the gui, in gui scaled pixels
     * @param h the height of the gui, in gui scaled pixels
     */
    public static void draw(ARRenderAction action, Minecraft mc, GuiGraphics graphics, int w, int h) {
        if (action.getType() == null || !action.getType().ensureArgs(action.getIntArgs())) return;
        int[] i = action.getIntArgs();
        String text = action.getStringArg();
        Font font = mc.font;
        switch (action.getType()) {
            case DRAW_CENTERED_STRING ->
                    graphics.drawCenteredString(font, text, action.relativeX(i[0], w), action.relativeY(i[1], h), i[2]);
            case DRAW_STRING ->
                    graphics.drawString(font, text, action.relativeX(i[0], w), action.relativeY(i[1], h), i[2]);
            case DRAW_RIGHTBOUND_STRING ->
                    graphics.drawString(font, text, action.relativeX(i[0], w) - font.width(text), action.relativeY(i[1], h), i[2]);
            case FILL ->
                    graphics.fill(action.relativeX(i[0], w), action.relativeY(i[1], h), action.relativeX(i[2], w), action.relativeY(i[3], h), fixAlpha(i[4]));
            case HORIZONTAL_LINE ->
                    graphics.hLine(action.relativeX(i[0], w), action.relativeX(i[1], w), action.relativeY(i[2], h), fixAlpha(i[3]));
            case VERTICAL_LINE ->
                    graphics.vLine(action.relativeX(i[0], w), action.relativeY(i[1], h), action.relativeY(i[2], h), fixAlpha(i[3]));
            case FILL_GRADIENT ->
                    graphics.fillGradient(action.relativeX(i[0], w), action.relativeY(i[1], h), action.relativeX(i[2], w), action.relativeY(i[3], h), fixAlpha(i[4]), fixAlpha(i[5]));
            case DRAW_CIRCLE ->
                    drawCircle(graphics, action.relativeX(i[0], w), action.relativeY(i[1], h), action.relativeAverage(i[2], w, h), i[3]);
            case FILL_CIRCLE ->
                    fillCircle(graphics, action.relativeX(i[0], w), action.relativeY(i[1], h), action.relativeAverage(i[2], w, h), i[3]);
            case DRAW_ITEM_ICON -> drawItemIcon(graphics, text, action.relativeX(i[0], w), action.relativeY(i[1], h));
            default ->
                    AdvancedPeripherals.LOGGER.warn("Failed to execute AR render action of unimplemented type {}", action.getType());
        }
    }

    private static void drawCircle(GuiGraphics graphics, int centerX, int centerY, float radius, int color) {
        color = fixAlpha(color);
        graphics.flush();
        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        prepare();
        buffer.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        // <= so the strip is closed again at the start point
        for (int i = 0; i <= CIRCLE_SEGMENTS; i++) {
            double angle = 2 * Math.PI * i / CIRCLE_SEGMENTS;
            vertex(buffer, matrix, centerX + (float) (radius * Math.sin(angle)), centerY + (float) (radius * Math.cos(angle)), color);
        }
        BufferUploader.drawWithShader(buffer.end());
        cleanup();
    }

    private static void fillCircle(GuiGraphics graphics, int centerX, int centerY, float radius, int color) {
        color = fixAlpha(color);
        graphics.flush();
        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        prepare();
        buffer.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        vertex(buffer, matrix, centerX, centerY, color);
        for (int i = 0; i <= CIRCLE_SEGMENTS; i++) {
            double angle = 2 * Math.PI * i / CIRCLE_SEGMENTS;
            vertex(buffer, matrix, centerX + (float) (radius * Math.sin(angle)), centerY + (float) (radius * Math.cos(angle)), color);
        }
        BufferUploader.drawWithShader(buffer.end());
        cleanup();
    }

    private static void drawItemIcon(GuiGraphics graphics, String itemId, int x, int y) {
        Item item = ItemUtil.getRegistryEntry(itemId, ForgeRegistries.ITEMS);
        if (item == null) return;
        graphics.renderItem(new ItemStack(item), x, y);
    }

    private static void vertex(BufferBuilder buffer, Matrix4f matrix, float x, float y, int color) {
        float a = (color >> 24 & 255) / 255.0F;
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;
        buffer.vertex(matrix, x, y, 0).color(r, g, b, a).endVertex();
    }

    private static void prepare() {
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
    }

    private static void cleanup() {
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}
