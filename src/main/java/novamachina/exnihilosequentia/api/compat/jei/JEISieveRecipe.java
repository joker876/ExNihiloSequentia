package novamachina.exnihilosequentia.api.compat.jei;

import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;
import novamachina.exnihilosequentia.api.crafting.sieve.SieveRecipe;


public class JEISieveRecipe {
    @Nonnull private final List<List<ItemStack>> inputs;
    @Nonnull private final List<ItemStack> results;

    public JEISieveRecipe(@Nonnull final List<List<ItemStack>> input, @Nonnull final List<SieveRecipe> results) {
        this.inputs = input;
        this.results = results.parallelStream().map(SieveRecipe::getDrop).collect(Collectors.toList());
    }

    @Nonnull
    public List<List<ItemStack>> getInputs() {
        return inputs;
    }

    @Nonnull
    public ItemStack getMesh() {
        return inputs.get(0).get(0);
    }

    @Nonnull
    public List<ItemStack> getResults() {
        return results;
    }

    @Nonnull
    public List<ItemStack> getSieveables() {
        return inputs.get(1);
    }
}
