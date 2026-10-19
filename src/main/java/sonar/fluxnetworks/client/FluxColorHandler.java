package sonar.fluxnetworks.client;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import sonar.fluxnetworks.api.FluxConstants;
import sonar.fluxnetworks.api.FluxDataComponents;
import sonar.fluxnetworks.client.gui.basic.GuiFluxCore;
import sonar.fluxnetworks.common.data.FluxDeviceConfigComponent;
import sonar.fluxnetworks.common.device.TileFluxDevice;

import java.util.Optional;

/**
 * Render network color on blocks and items.
 * <p>
 * Since 1.21.4 item tints are data-driven: item model definitions reference the
 * {@code fluxnetworks:network_color} tint source registered below.
 */
public final class FluxColorHandler {

    /**
     * Block tint layer (tintindex 1) that reads the network color from the device block entity.
     */
    public static final BlockTintSource DEVICE_TINT = new BlockTintSource() {
        @Override
        public int color(BlockState state) {
            return -1;
        }

        @Override
        public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
            if (level.getBlockEntity(pos) instanceof TileFluxDevice device) {
                return ARGB.opaque(device.mClientColor);
            }
            return -1;
        }
    };

    /**
     * Item tint source showing the item's network color.
     */
    public record NetworkColor() implements ItemTintSource {
        public static final NetworkColor INSTANCE = new NetworkColor();
        public static final MapCodec<NetworkColor> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
            return ARGB.opaque(getColorForItem(stack));
        }

        @Override
        public MapCodec<NetworkColor> type() {
            return MAP_CODEC;
        }
    }

    private FluxColorHandler() {
    }

    public static int getColorForItem(ItemStack stack) {
        // 1. Check if the color is defined in the stack
        Optional<Integer> forcedColor = stack.get(FluxDataComponents.FLUX_COLOR);
        if (forcedColor != null && forcedColor.isPresent()) {
            return forcedColor.get();
        }
        // 2. If the item has a saved configuration, use network color
        FluxDeviceConfigComponent data = stack.get(FluxDataComponents.FLUX_CONFIG);
        if (data != null) {
            return ClientCache.getNetwork(data.networkId()).getNetworkColor();
        }
        // 3. Try grabbing color from the current screen
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof GuiFluxCore gui) {
            return gui.getNetwork().getNetworkColor();
        }
        // 4. If all else fails, use the default color
        return FluxConstants.INVALID_NETWORK_COLOR;
    }
}
