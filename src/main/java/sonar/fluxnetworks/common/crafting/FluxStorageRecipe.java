package sonar.fluxnetworks.common.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import sonar.fluxnetworks.api.FluxDataComponents;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * Shaped recipe that carries stored energy and configuration from Flux Storage ingredients to the result.
 */
@ParametersAreNonnullByDefault
public class FluxStorageRecipe extends ShapedRecipe {

    public static final MapCodec<FluxStorageRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(
                            Recipe.CommonInfo.MAP_CODEC.forGetter(o -> o.commonInfo),
                            CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(o -> o.bookInfo),
                            ShapedRecipePattern.MAP_CODEC.forGetter(o -> o.pattern),
                            ItemStackTemplate.CODEC.fieldOf("result").forGetter(o -> o.mResult)
                    )
                    .apply(i, FluxStorageRecipe::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, FluxStorageRecipe> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC, o -> o.commonInfo,
            CraftingRecipe.CraftingBookInfo.STREAM_CODEC, o -> o.bookInfo,
            ShapedRecipePattern.STREAM_CODEC, o -> o.pattern,
            ItemStackTemplate.STREAM_CODEC, o -> o.mResult,
            FluxStorageRecipe::new
    );
    public static final RecipeSerializer<FluxStorageRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final ItemStackTemplate mResult;

    public FluxStorageRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
                             ShapedRecipePattern pattern, ItemStackTemplate result) {
        super(commonInfo, bookInfo, pattern, result);
        mResult = result;
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public ItemStack assemble(CraftingInput input) {
        ItemStack result = super.assemble(input);
        // Find all flux storage items in the input
        List<ItemStack> storageItems = input.items().stream().filter(stack -> !stack.isEmpty() && stack.get(FluxDataComponents.STORED_ENERGY) != null).toList();
        // If none found, bail
        if (storageItems.isEmpty()) {
            return result;
        }
        // Sum the energy of all storage items
        long totalEnergy = storageItems.stream().map(stack -> stack.get(FluxDataComponents.STORED_ENERGY)).reduce(0L, Long::sum);
        // Put the total energy value into the resulting item
        result.set(FluxDataComponents.STORED_ENERGY, totalEnergy);
        // Copy device configuration from the first storage item (if present)
        result.copyFrom(storageItems.getFirst(), FluxDataComponents.FLUX_CONFIG);

        return result;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public RecipeSerializer<ShapedRecipe> getSerializer() {
        return (RecipeSerializer) SERIALIZER;
    }
}
