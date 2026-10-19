package sonar.fluxnetworks.register;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.RegisterEvent;
import sonar.fluxnetworks.FluxNetworks;
import sonar.fluxnetworks.common.block.FluxControllerBlock;
import sonar.fluxnetworks.common.block.FluxPlugBlock;
import sonar.fluxnetworks.common.block.FluxPointBlock;
import sonar.fluxnetworks.common.block.FluxStorageBlock;

public class RegistryBlocks {
    public static final Identifier FLUX_BLOCK_KEY = FluxNetworks.location("flux_block");
    public static final Identifier FLUX_PLUG_KEY = FluxNetworks.location("flux_plug");
    public static final Identifier FLUX_POINT_KEY = FluxNetworks.location("flux_point");
    public static final Identifier FLUX_CONTROLLER_KEY = FluxNetworks.location("flux_controller");
    public static final Identifier BASIC_FLUX_STORAGE_KEY = FluxNetworks.location("basic_flux_storage");
    public static final Identifier HERCULEAN_FLUX_STORAGE_KEY = FluxNetworks.location("herculean_flux_storage");
    public static final Identifier GARGANTUAN_FLUX_STORAGE_KEY = FluxNetworks.location("gargantuan_flux_storage");

    public static final DeferredBlock<Block> FLUX_BLOCK = holder(FLUX_BLOCK_KEY);
    public static final DeferredBlock<FluxPlugBlock> FLUX_PLUG = holder(FLUX_PLUG_KEY);
    public static final DeferredBlock<FluxPointBlock> FLUX_POINT = holder(FLUX_POINT_KEY);
    public static final DeferredBlock<FluxControllerBlock> FLUX_CONTROLLER = holder(FLUX_CONTROLLER_KEY);
    public static final DeferredBlock<FluxStorageBlock.Basic> BASIC_FLUX_STORAGE = holder(BASIC_FLUX_STORAGE_KEY);
    public static final DeferredBlock<FluxStorageBlock.Herculean> HERCULEAN_FLUX_STORAGE = holder(HERCULEAN_FLUX_STORAGE_KEY);
    public static final DeferredBlock<FluxStorageBlock.Gargantuan> GARGANTUAN_FLUX_STORAGE = holder(GARGANTUAN_FLUX_STORAGE_KEY);

    static <T extends Block> DeferredBlock<T> holder(Identifier location) {
        return DeferredBlock.createBlock(location);
    }

    private static BlockBehaviour.Properties normalProps(Identifier id) {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL)
                .strength(1.0F, 1000F)
                .setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, id));
    }

    private static BlockBehaviour.Properties deviceProps(Identifier id) {
        return normalProps(id).noOcclusion();
    }

    static void register(RegisterEvent.RegisterHelper<Block> helper) {
        helper.register(FLUX_BLOCK_KEY, new Block(normalProps(FLUX_BLOCK_KEY)));
        helper.register(FLUX_PLUG_KEY, new FluxPlugBlock(deviceProps(FLUX_PLUG_KEY)));
        helper.register(FLUX_POINT_KEY, new FluxPointBlock(deviceProps(FLUX_POINT_KEY)));
        helper.register(FLUX_CONTROLLER_KEY, new FluxControllerBlock(deviceProps(FLUX_CONTROLLER_KEY)));
        helper.register(BASIC_FLUX_STORAGE_KEY, new FluxStorageBlock.Basic(deviceProps(BASIC_FLUX_STORAGE_KEY)));
        helper.register(HERCULEAN_FLUX_STORAGE_KEY, new FluxStorageBlock.Herculean(deviceProps(HERCULEAN_FLUX_STORAGE_KEY)));
        helper.register(GARGANTUAN_FLUX_STORAGE_KEY, new FluxStorageBlock.Gargantuan(deviceProps(GARGANTUAN_FLUX_STORAGE_KEY)));
    }

    private RegistryBlocks() {}
}
