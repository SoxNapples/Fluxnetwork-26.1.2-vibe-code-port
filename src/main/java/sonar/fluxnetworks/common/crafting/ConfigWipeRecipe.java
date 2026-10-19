package sonar.fluxnetworks.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Block;
import sonar.fluxnetworks.api.FluxDataComponents;
import sonar.fluxnetworks.common.block.FluxStorageBlock;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * save Flux Storage energy when wiping NBT
 */
@ParametersAreNonnullByDefault
public class ConfigWipeRecipe extends ShapelessRecipe {

    public static final MapCodec<ConfigWipeRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(
                            Recipe.CommonInfo.MAP_CODEC.forGetter(o -> o.commonInfo),
                            CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(o -> o.bookInfo),
                            ItemStackTemplate.CODEC.fieldOf("result").forGetter(o -> o.mResult),
                            Codec.lazyInitialized(() -> Ingredient.CODEC.listOf(1, 9))
                                    .fieldOf("ingredients").forGetter(o -> o.mIngredients)
                    )
                    .apply(i, ConfigWipeRecipe::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigWipeRecipe> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC, o -> o.commonInfo,
            CraftingRecipe.CraftingBookInfo.STREAM_CODEC, o -> o.bookInfo,
            ItemStackTemplate.STREAM_CODEC, o -> o.mResult,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), o -> o.mIngredients,
            ConfigWipeRecipe::new
    );
    public static final RecipeSerializer<ConfigWipeRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final ItemStackTemplate mResult;
    private final List<Ingredient> mIngredients;

    public ConfigWipeRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
                            ItemStackTemplate result, List<Ingredient> ingredients) {
        super(commonInfo, bookInfo, result, ingredients);
        mResult = result;
        mIngredients = ingredients;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack originalStack = input.items().stream().filter(stack -> !stack.isEmpty() && stack.get(FluxDataComponents.FLUX_CONFIG) != null).findFirst().orElse(null);
        ItemStack output = super.assemble(input);

        if (originalStack != null) {
            if (Block.byItem(output.getItem()) instanceof FluxStorageBlock) {
                output.copyFrom(originalStack, FluxDataComponents.STORED_ENERGY);
            }
            return output;
        }
        return output;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public RecipeSerializer<ShapelessRecipe> getSerializer() {
        return (RecipeSerializer) SERIALIZER;
    }
}
