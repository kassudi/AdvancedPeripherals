package de.srendi.advancedperipherals.common.addons.curios;

import de.srendi.advancedperipherals.common.items.ARGogglesItem;
import de.srendi.advancedperipherals.common.util.SideHelper;
import de.srendi.advancedperipherals.network.APNetworking;
import de.srendi.advancedperipherals.network.toclient.ClearHudCanvasPacket;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

/**
 * Allows to wear the AR goggles in the glasses slot of Curios. Only load this class if Curios is present.
 */
public class CuriosHelper {

    public static ICapabilityProvider createARGogglesProvider(ItemStack stackFor) {
        LazyOptional<ICurio> curio = LazyOptional.of(() -> new ICurio() {

            @Override
            public ItemStack getStack() {
                return stackFor;
            }

            @Override
            public void curioTick(SlotContext slotContext) {
                LivingEntity wearer = slotContext.entity();
                if (!SideHelper.isClientPlayer(wearer))
                    return;
                ARGogglesItem.clientTick((Player) wearer, stackFor);
            }

            @Override
            public void onUnequip(SlotContext slotContext, ItemStack newStack) {
                if (slotContext.entity() instanceof ServerPlayer serverPlayer)
                    APNetworking.sendTo(new ClearHudCanvasPacket(), serverPlayer);
            }
        });

        return new ICapabilityProvider() {
            @NotNull
            @Override
            public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                return CuriosCapability.ITEM.orEmpty(cap, curio);
            }
        };
    }
}
