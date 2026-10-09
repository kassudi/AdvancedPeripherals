package de.srendi.advancedperipherals.common.items;

import de.srendi.advancedperipherals.AdvancedPeripherals;
import de.srendi.advancedperipherals.client.HudOverlayHandler;
import de.srendi.advancedperipherals.client.KeyBindings;
import de.srendi.advancedperipherals.common.addons.APAddons;
import de.srendi.advancedperipherals.common.addons.curios.CuriosHelper;
import de.srendi.advancedperipherals.common.blocks.blockentities.ARControllerEntity;
import de.srendi.advancedperipherals.common.configuration.APConfig;
import de.srendi.advancedperipherals.common.setup.Blocks;
import de.srendi.advancedperipherals.common.util.EnumColor;
import de.srendi.advancedperipherals.common.util.KeybindUtil;
import de.srendi.advancedperipherals.common.util.SideHelper;
import de.srendi.advancedperipherals.common.util.TranslationUtil;
import de.srendi.advancedperipherals.network.APNetworking;
import de.srendi.advancedperipherals.network.toserver.RequestHudCanvasPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ARGogglesItem extends ArmorItem {

    private static final String CONTROLLER_POS = "controller_pos";
    private static final String CONTROLLER_LEVEL = "controller_level";
    /**
     * How often, in ticks, the goggles ask the server for the canvas if the controller is not loaded on the client
     */
    private static final int REQUEST_INTERVAL = 5;

    private Component description;

    public ARGogglesItem() {
        super(ArmorMaterials.LEATHER, Type.HELMET, new Properties().stacksTo(1));
    }

    /**
     * Whether a dimension key stored on the goggles points to the given dimension. Goggles linked in 1.18 stored the
     * {@code ResourceKey#toString()} of the dimension, so both formats are accepted.
     */
    public static boolean dimensionMatches(String storedKey, ResourceKey<Level> dimension) {
        return storedKey.equals(dimension.location().toString()) || storedKey.equals(dimension.toString());
    }

    /**
     * Updates the canvas which is displayed to the local player. Must only be called on the client.
     */
    public static void clientTick(Player player, ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(CONTROLLER_POS) || !tag.contains(CONTROLLER_LEVEL))
            return;
        int[] arr = tag.getIntArray(CONTROLLER_POS);
        if (arr.length < 3)
            return;
        BlockPos pos = new BlockPos(arr[0], arr[1], arr[2]);
        String dimensionKey = tag.getString(CONTROLLER_LEVEL);
        Level level = player.level();

        BlockEntity blockEntity = dimensionMatches(dimensionKey, level.dimension()) ? level.getBlockEntity(pos) : null;
        if (blockEntity instanceof ARControllerEntity controller) {
            HudOverlayHandler.updateCanvas(controller.getCanvas());
            return;
        }

        // The controller is in another dimension or further away than the client has chunks loaded, ask the server
        if (player.tickCount % REQUEST_INTERVAL == 0)
            APNetworking.sendToServer(new RequestHudCanvasPacket(pos, dimensionKey));
    }

    private Component getTooltipDescription() {
        if (description == null)
            description = TranslationUtil.itemTooltip(getDescriptionId());
        return description;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (!KeybindUtil.isKeyPressed(KeyBindings.DESCRIPTION_KEYBINDING)) {
            tooltip.add(EnumColor.buildTextComponent(Component.translatable("item.advancedperipherals.tooltip.show_desc", KeyBindings.DESCRIPTION_KEYBINDING.getTranslatedKeyMessage())));
        } else {
            tooltip.add(EnumColor.buildTextComponent(getTooltipDescription()));
        }
        if (!APConfig.PERIPHERALS_CONFIG.enableARGoggles.get())
            tooltip.add(EnumColor.buildTextComponent(Component.translatable("item.advancedperipherals.tooltip.disabled")));
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(CONTROLLER_POS, Tag.TAG_INT_ARRAY)) {
            int[] pos = tag.getIntArray(CONTROLLER_POS);
            if (pos.length >= 3)
                tooltip.add(EnumColor.buildTextComponent(Component.translatable("item.advancedperipherals.tooltip.ar_goggles.binding", pos[0], pos[1], pos[2])));
        }
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        if (!APAddons.curiosLoaded)
            return null;

        return CuriosHelper.createARGogglesProvider(stack);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return AdvancedPeripherals.MOD_ID + ":textures/models/ar_goggles.png";
    }

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        // only need to tick client side, if client is wearing them himself
        if (!SideHelper.isClientPlayer(player))
            return;
        clientTick(player, stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (!level.getBlockState(pos).is(Blocks.AR_CONTROLLER.get()))
            return super.useOn(context);
        if (!(level.getBlockEntity(pos) instanceof ARControllerEntity controller))
            return super.useOn(context);

        if (!level.isClientSide) {
            CompoundTag tag = context.getItemInHand().getOrCreateTag();
            BlockPos controllerPos = controller.getBlockPos();
            tag.putIntArray(CONTROLLER_POS, new int[]{controllerPos.getX(), controllerPos.getY(), controllerPos.getZ()});
            tag.putString(CONTROLLER_LEVEL, level.dimension().location().toString());
        }
        Player player = context.getPlayer();
        if (player != null)
            player.displayClientMessage(Component.translatable("text.advancedperipherals.linked_goggles"), true);
        return InteractionResult.SUCCESS;
    }
}
