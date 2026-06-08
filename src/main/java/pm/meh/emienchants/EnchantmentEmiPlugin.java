package pm.meh.emienchants;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;

@EmiEntrypoint
public class EnchantmentEmiPlugin implements EmiPlugin {
    public static final EmiRecipeCategory ENCHANTS_CATEGORY = new EmiRecipeCategory(
            ResourceLocation.fromNamespaceAndPath(Common.MOD_ID, "enchants"), EmiStack.of(Items.ENCHANTED_BOOK));

    @Override
    public void register(EmiRegistry emiRegistry) {
        emiRegistry.addCategory(ENCHANTS_CATEGORY);

        // Les enchantements sont desormais un registre dynamique (data-driven) :
        // on les lit via l'acces aux registres du niveau client charge.
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        Registry<Enchantment> registry =
                minecraft.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);

        registry.holders().forEach(holder ->
                emiRegistry.addRecipe(new EnchantmentEmiRecipe(holder)));
    }
}
