package sonar.fluxnetworks.common.item;

import net.minecraft.world.level.block.Block;

/**
 * Flux Storage block item. The energy bar overlay is rendered by a special item model on the client.
 */
public class FluxStorageItem extends FluxDeviceItem {

    public FluxStorageItem(Block block, Properties props) {
        super(block, props);
    }
}
