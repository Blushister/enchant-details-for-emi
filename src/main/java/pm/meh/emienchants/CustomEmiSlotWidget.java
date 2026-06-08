package pm.meh.emienchants;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.SlotWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

import java.util.List;

public class CustomEmiSlotWidget extends SlotWidget {
    private static final ClientTooltipComponent SUBTITLE_VIEW_ALL =
            new ClientTextTooltip(Component.translatable("emienchants.slot.subtitle.view_all")
                    .withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC).getVisualOrderText());

    private final boolean hideItemName;
    private final ClientTooltipComponent slotTitle;

    public CustomEmiSlotWidget(EmiIngredient stack, int x, int y, boolean hideItemName, Component slotTitle) {
        super(stack, x, y);
        this.hideItemName = hideItemName;
        this.slotTitle = new ClientTextTooltip(slotTitle.getVisualOrderText());
    }

    @Override
    protected void addSlotTooltip(List<ClientTooltipComponent> list) {
        if (hideItemName) {
            if (getStack().getEmiStacks().size() == 1) {
                list.remove(0);
            } else if (getStack().getEmiStacks().size() > 1) {
                list.remove(2);
            }
        }
        if (getStack().getEmiStacks().size() > 1) {
            list.set(0, SUBTITLE_VIEW_ALL);
        }
        list.add(0, slotTitle);
    }
}
