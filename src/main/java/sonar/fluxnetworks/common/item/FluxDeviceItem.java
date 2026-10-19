package sonar.fluxnetworks.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import sonar.fluxnetworks.api.FluxDataComponents;
import sonar.fluxnetworks.api.FluxTranslate;
import sonar.fluxnetworks.api.energy.EnergyType;
import sonar.fluxnetworks.client.ClientCache;
import sonar.fluxnetworks.common.block.FluxStorageBlock;
import sonar.fluxnetworks.common.connection.FluxNetwork;
import sonar.fluxnetworks.common.data.FluxDeviceConfigComponent;
import sonar.fluxnetworks.register.RegistryTags;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class FluxDeviceItem extends BlockItem {

    public FluxDeviceItem(Block block, Properties props) {
        super(block, props);
    }

    @Override
    public Component getName(ItemStack stack) {
        FluxDeviceConfigComponent component = stack.get(FluxDataComponents.FLUX_CONFIG);
        if (component != null) {
            Optional<String> customName = component.customName();
            if (customName.isPresent()) {
                String value = customName.get();
                if (!value.isEmpty()) {
                    return Component.literal(value);
                }
            }
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> tooltip, TooltipFlag flag) {
        FluxDeviceConfigComponent component = stack.get(FluxDataComponents.FLUX_CONFIG);
        Long storedEnergy = stack.get(FluxDataComponents.STORED_ENERGY);
        if (component != null) {
            final FluxNetwork network = ClientCache.getNetwork(component.networkId());
            if (network.isValid()) {
                tooltip.accept(Component.literal(ChatFormatting.BLUE + FluxTranslate.NETWORK_FULL_NAME.get() + ": " +
                        ChatFormatting.RESET + network.getNetworkName()));
            }

            if (component.limit().isPresent()) {
                tooltip.accept(Component.literal(ChatFormatting.BLUE + FluxTranslate.TRANSFER_LIMIT.get() + ": " +
                        ChatFormatting.RESET + EnergyType.FE.getStorage(component.getLimit())));
            }

            if (component.priority().isPresent()) {
                tooltip.accept(Component.literal(ChatFormatting.BLUE + FluxTranslate.PRIORITY.get() + ": " +
                        ChatFormatting.RESET + component.getPriority()));
            }
        }

        if (storedEnergy != null) {
            // Non-storage devices display internal buffer instead of stored energy
            if (!stack.is(RegistryTags.FLUX_STORAGE)) {
                tooltip.accept(Component.literal(ChatFormatting.BLUE + FluxTranslate.INTERNAL_BUFFER.get() + ": " +
                        ChatFormatting.RESET + EnergyType.FE.getStorage(storedEnergy)));
            } else {
                long energy = storedEnergy;
                Block block = getBlock();
                double percentage;
                if (block instanceof FluxStorageBlock)
                    percentage = Math.min((double) energy / ((FluxStorageBlock) block).getEnergyCapacity(), 1.0);
                else
                    percentage = 0;
                tooltip.accept(Component.literal(ChatFormatting.BLUE + FluxTranslate.ENERGY_STORED.get() + ": " +
                        ChatFormatting.RESET + EnergyType.FE.getStorage(energy) + String.format(" (%.1f%%)",
                        percentage * 100)));
            }
        }
        if (getBlock() instanceof sonar.fluxnetworks.common.block.FluxDeviceBlock deviceBlock) {
            deviceBlock.appendBlockTooltip(stack, context, tooltip, flag);
        }
        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}
