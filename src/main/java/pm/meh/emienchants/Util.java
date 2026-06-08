package pm.meh.emienchants;

import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Locale;

public class Util {
    /**
     * Construit un livre enchante (enchantement + niveau) via le composant de donnees
     * STORED_ENCHANTMENTS (remplace les NBT depuis 1.21).
     */
    public static EmiStack getBookStackForLevel(Holder<Enchantment> enchantment, int level) {
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        mutable.set(enchantment, level);
        book.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());
        return EmiStack.of(book);
    }

    public static MutableComponent getLocalizedTextByCode(String code, String localizationIdTemplate) {
        String localizationId = String.format(localizationIdTemplate, code.toLowerCase(Locale.ROOT));
        MutableComponent translatable = Component.translatable(localizationId);
        if (translatable.getString().equals(localizationId)) {
            return Component.literal(code);
        } else {
            return translatable;
        }
    }
}
