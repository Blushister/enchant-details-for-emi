package pm.meh.emienchants;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static pm.meh.emienchants.Util.getBookStackForLevel;

public class EnchantmentEmiRecipe implements EmiRecipe {

    private static final ResourceLocation ICON_INFO = ResourceLocation.fromNamespaceAndPath(Common.MOD_ID, "textures/gui/icon_info.png");
    private static final ResourceLocation ICON_ENCH_TABLE = ResourceLocation.fromNamespaceAndPath(Common.MOD_ID, "textures/gui/icon_ench_table.png");
    private static final ResourceLocation ICON_VILLAGER = ResourceLocation.fromNamespaceAndPath(Common.MOD_ID, "textures/gui/icon_villager.png");
    private static final ResourceLocation ICON_DISCOVERABLE = ResourceLocation.fromNamespaceAndPath(Common.MOD_ID, "textures/gui/icon_discoverable.png");
    private static final ResourceLocation ICON_TREASURE = ResourceLocation.fromNamespaceAndPath(Common.MOD_ID, "textures/gui/icon_treasure.png");
    private static final ResourceLocation ICON_CURSE = ResourceLocation.fromNamespaceAndPath(Common.MOD_ID, "textures/gui/icon_curse.png");

    private static final int LAYOUT_X_OFFSET = 22;
    private static final int LAYOUT_X_OFFSET_SMALL = 2;
    private static final int LAYOUT_Y_OFFSET = 2;
    private static final int LAYOUT_ROW_HEIGHT = 10;
    private static final int LAYOUT_TEXT_COLOR = 0x333333;
    private static final boolean LAYOUT_TEXT_SHADOW = false;
    private final int LAYOUT_DESCRIPTION_OFFSET;

    private final ResourceLocation id;
    private final Holder<Enchantment> holder;
    private final ResourceLocation enchantmentResourceLocation;
    private final Enchantment enchantment;
    private final List<EmiStack> inputs;
    private final EmiIngredient canApplyTo;
    private final EmiIngredient incompatibleSlot;
    private final List<IconBoolStatEntry> iconStats;
    private final List<FormattedCharSequence> description;

    public EnchantmentEmiRecipe(Holder<Enchantment> holder) {
        this.holder = holder;
        this.enchantmentResourceLocation = holder.unwrapKey().orElseThrow().location();
        this.enchantment = holder.value();

        id = ResourceLocation.fromNamespaceAndPath(Common.MOD_ID,
                String.format("/%s/%s", enchantmentResourceLocation.getNamespace(), enchantmentResourceLocation.getPath()));

        inputs = IntStream.range(1, enchantment.getMaxLevel() + 1)
                .mapToObj(level -> getBookStackForLevel(holder, level)).toList();

        // Items applicables : desormais expose directement par l'enchantement (HolderSet<Item>)
        canApplyTo = EmiIngredient.of(enchantment.getSupportedItems().stream()
                .map(itemHolder -> (EmiIngredient) EmiStack.of(new ItemStack(itemHolder.value()))).toList());

        // Enchantements incompatibles : compares via Enchantment.areCompatible(holder, holder)
        Registry<Enchantment> registry =
                Minecraft.getInstance().level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        incompatibleSlot = EmiIngredient.of(registry.holders()
                .filter(other -> !other.equals(holder) && !Enchantment.areCompatible(holder, other))
                .map(other -> getBookStackForLevel(other, other.value().getMaxLevel())).toList());

        iconStats = List.of(
                new IconBoolStatEntry(ICON_ENCH_TABLE, "ench_table", holder.is(EnchantmentTags.IN_ENCHANTING_TABLE), true),
                new IconBoolStatEntry(ICON_VILLAGER, "tradeable", holder.is(EnchantmentTags.TRADEABLE), true),
                new IconBoolStatEntry(ICON_DISCOVERABLE, "discoverable", holder.is(EnchantmentTags.ON_RANDOM_LOOT), true),
                new IconBoolStatEntry(ICON_TREASURE, "treasure", holder.is(EnchantmentTags.TREASURE), false),
                new IconBoolStatEntry(ICON_CURSE, "curse", holder.is(EnchantmentTags.CURSE), false)
        );

        LAYOUT_DESCRIPTION_OFFSET = LAYOUT_Y_OFFSET + LAYOUT_ROW_HEIGHT * (incompatibleSlot.isEmpty() ? 4 : 5);

        // Description : la cle de traduction est derivee du nom de l'enchantement, suffixee de ".desc"
        String descriptionId = descriptionKey();
        if (descriptionId == null) {
            description = List.of();
        } else {
            Component descriptionTranslatable = Component.translatable(descriptionId).withStyle(ChatFormatting.ITALIC);
            if (descriptionTranslatable.getString().equals(descriptionId)) {
                description = List.of();
            } else {
                description = Minecraft.getInstance().font.split(descriptionTranslatable, getDisplayWidth() - LAYOUT_X_OFFSET_SMALL * 2);
            }
        }
    }

    @Nullable
    private String descriptionKey() {
        if (enchantment.description().getContents() instanceof TranslatableContents tc) {
            return tc.getKey() + ".desc";
        }
        return null;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return EnchantmentEmiPlugin.ENCHANTS_CATEGORY;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(canApplyTo, EmiIngredient.of(inputs.stream().map(s -> (EmiIngredient) s).toList()));
    }

    @Override
    public List<EmiStack> getOutputs() {
        return inputs;
    }

    @Override
    public int getDisplayWidth() {
        return 160;
    }

    @Override
    public int getDisplayHeight() {
        return LAYOUT_DESCRIPTION_OFFSET + LAYOUT_ROW_HEIGHT * description.size();
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        int row = 0;

        // slot de l'enchantement (niveau max)
        widgetHolder.addSlot(getBookStackForLevel(holder, enchantment.getMaxLevel()), LAYOUT_X_OFFSET_SMALL, LAYOUT_Y_OFFSET);
        // items applicables
        widgetHolder.add(new CustomEmiSlotWidget(canApplyTo, LAYOUT_X_OFFSET_SMALL, LAYOUT_Y_OFFSET + 20,
                false, Component.translatable("emienchants.property.applicable_to")));
        // enchantements incompatibles
        if (!incompatibleSlot.isEmpty()) {
            widgetHolder.add(new CustomEmiSlotWidget(incompatibleSlot, LAYOUT_X_OFFSET_SMALL, LAYOUT_Y_OFFSET + 40,
                    true, Component.translatable("emienchants.property.conflicts")));
        }

        // nom + intervalle de niveaux
        MutableComponent title = enchantment.description().copy();
        if (enchantment.getMaxLevel() > 1) {
            title = title.append(String.format(" §5%d-%d", enchantment.getMinLevel(), enchantment.getMaxLevel()));
        }
        widgetHolder.addText(title, LAYOUT_X_OFFSET, LAYOUT_Y_OFFSET + LAYOUT_ROW_HEIGHT * row++, LAYOUT_TEXT_COLOR, LAYOUT_TEXT_SHADOW);

        // mod id
        widgetHolder.addText(Component.literal(enchantmentResourceLocation.getNamespace()).withStyle(ChatFormatting.DARK_BLUE),
                LAYOUT_X_OFFSET, LAYOUT_Y_OFFSET + LAYOUT_ROW_HEIGHT * row++, LAYOUT_TEXT_COLOR, LAYOUT_TEXT_SHADOW);

        // poids (remplace l'ancienne "rarete" supprimee en 1.21) + tooltip des couts par niveau
        TextWidget weightWidget = widgetHolder.addText(Component.translatable("emienchants.property.weight",
                        Component.literal(String.valueOf(enchantment.getWeight())).withStyle(ChatFormatting.DARK_PURPLE)),
                LAYOUT_X_OFFSET, LAYOUT_Y_OFFSET + LAYOUT_ROW_HEIGHT * row, LAYOUT_TEXT_COLOR, LAYOUT_TEXT_SHADOW);
        widgetHolder.addTexture(ICON_INFO, LAYOUT_X_OFFSET + weightWidget.getBounds().width() + 1,
                LAYOUT_Y_OFFSET + LAYOUT_ROW_HEIGHT * row, 7, 8, 7, 8, 7, 8, 7, 8);
        widgetHolder.addTooltipText(IntStream.range(1, enchantment.getMaxLevel() + 1)
                        .mapToObj(lvl -> (Component) Component.translatable("emienchants.property.cost", lvl,
                                enchantment.getMinCost(lvl), enchantment.getMaxCost(lvl))).toList(),
                LAYOUT_X_OFFSET, LAYOUT_Y_OFFSET + LAYOUT_ROW_HEIGHT * row++, weightWidget.getBounds().width() + 9, 8);

        // stats a icones
        int iconSectionWidth;
        int iconXOffset;
        if (incompatibleSlot.isEmpty()) {
            iconSectionWidth = (getDisplayWidth() - LAYOUT_X_OFFSET_SMALL * 2) / iconStats.size();
            iconXOffset = LAYOUT_X_OFFSET_SMALL;
        } else {
            iconSectionWidth = (getDisplayWidth() - LAYOUT_X_OFFSET - LAYOUT_X_OFFSET_SMALL) / 3;
            iconXOffset = LAYOUT_X_OFFSET;
        }
        int iconYOffset = LAYOUT_Y_OFFSET + LAYOUT_ROW_HEIGHT * row;
        int iconCounter = 0;

        for (IconBoolStatEntry stat : iconStats) {
            widgetHolder.addTexture(stat.icon, iconXOffset, iconYOffset, 8, 8, 8, 8, 8, 8, 8, 8);
            TextWidget statWidget = widgetHolder.addText(stat.getValueLabel(), iconXOffset + 10, iconYOffset, LAYOUT_TEXT_COLOR, LAYOUT_TEXT_SHADOW);
            widgetHolder.addTooltipText(List.of(Component.translatable(String.format("emienchants.property.%s.%s", stat.label, stat.value))),
                    iconXOffset, iconYOffset, statWidget.getBounds().width() + 10, 8);
            iconCounter += 1;
            if (!incompatibleSlot.isEmpty() && iconCounter == 3) {
                iconCounter = 0;
                iconXOffset = LAYOUT_X_OFFSET;
                iconYOffset += LAYOUT_ROW_HEIGHT;
            } else {
                iconXOffset += iconSectionWidth;
            }
        }

        // description
        row = 0;
        for (FormattedCharSequence line : description) {
            widgetHolder.addText(line, LAYOUT_X_OFFSET_SMALL, LAYOUT_DESCRIPTION_OFFSET + LAYOUT_ROW_HEIGHT * row++,
                    LAYOUT_TEXT_COLOR, LAYOUT_TEXT_SHADOW);
        }
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    private record IconBoolStatEntry(ResourceLocation icon, String label, boolean value, boolean isPositive) {
        public Component getValueLabel() {
            return Component.translatable("emienchants.property.value." + value).withStyle(
                    Style.EMPTY.withColor(value ^ isPositive ? 0xAA0000 : 0x008800));
        }
    }
}
