package de.aci.gui;

import de.aci.AciPlugin;
import de.aci.CustomItem;
import de.aci.CustomRecipe;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Rezept-Editor: Das 3x3-Gitter besteht aus "Geister-Slots". Mit einem Item auf dem Cursor
 * hineinklicken setzt die Zutat (der Cursor bleibt unveraendert, es wird nichts verbraucht),
 * mit leerem Cursor klicken leert den Slot. Shift-Klick im eigenen Inventar legt das Item
 * in den ersten freien Slot. Custom Items und normale Items sind beide moeglich.
 */
public final class RecipeEditorMenu extends Menu {
    static final int[] GRID = {10, 11, 12, 19, 20, 21, 28, 29, 30};
    private static final int RESULT = 25, ARROW = 23, AMT_UP = 16, AMT_DOWN = 34, SAVE = 40, BACK = 36;

    private final CustomItem ci;
    private final int index;
    private int amount;

    public RecipeEditorMenu(AciPlugin plugin, CustomItem ci, int index) {
        super(plugin);
        this.ci = ci;
        this.index = index;
        inv = Bukkit.createInventory(this, 45, Ui.text("&8Rezept " + (index + 1) + ": &0" + Ui.plain(ci.name)));
        for (int i = 0; i < 45; i++) inv.setItem(i, Ui.pane());

        CustomRecipe r = index < ci.recipes.size() ? ci.recipes.get(index) : new CustomRecipe();
        amount = r.amount;
        for (int i = 0; i < 9; i++) inv.setItem(GRID[i], r.grid[i] == null ? null : plugin.items().stackForToken(r.grid[i], 1));

        inv.setItem(ARROW, Ui.button(Material.ARROW, "&7ergibt"));
        inv.setItem(AMT_UP, Ui.button(Material.LIME_STAINED_GLASS_PANE, "&a+1 Menge", "&7Shift: +8"));
        inv.setItem(AMT_DOWN, Ui.button(Material.RED_STAINED_GLASS_PANE, "&c-1 Menge", "&7Shift: -8"));
        inv.setItem(BACK, Ui.button(Material.BARRIER, "&cAbbrechen", "&7Aenderungen verwerfen"));
        inv.setItem(SAVE, Ui.button(Material.EMERALD_BLOCK, "&aRezept speichern"));
        inv.setItem(4, Ui.button(Material.PAPER, "&6Anleitung",
                "&7Item auf den Cursor nehmen und in ein",
                "&7Gitter-Feld klicken = Zutat setzen.",
                "&7Leerer Cursor + Klick = Feld leeren.",
                "&7Shift-Klick im Inventar geht auch.",
                "&7(Es wird nichts verbraucht.)"));
        updateResult();
    }

    private void updateResult() {
        inv.setItem(RESULT, plugin.items().create(ci, amount));
    }

    private static int gridIndex(int slot) {
        for (int i = 0; i < 9; i++) if (GRID[i] == slot) return i;
        return -1;
    }

    private void setGhost(int slot, ItemStack source) {
        String token = plugin.items().tokenFor(source);
        inv.setItem(slot, token == null ? null : plugin.items().stackForToken(token, 1));
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        int slot = e.getSlot();
        int g = gridIndex(slot);

        if (g >= 0) {
            ItemStack cursor = e.getCursor();
            if (cursor == null || cursor.getType().isAir()) inv.setItem(slot, null);
            else setGhost(slot, cursor);
            return;
        }

        switch (slot) {
            case AMT_UP -> {
                amount = Math.min(64, amount + (e.isShiftClick() ? 8 : 1));
                updateResult();
            }
            case AMT_DOWN -> {
                amount = Math.max(1, amount - (e.isShiftClick() ? 8 : 1));
                updateResult();
            }
            case BACK -> new RecipeListMenu(plugin, ci).open(p);
            case SAVE -> {
                CustomRecipe r = new CustomRecipe();
                r.amount = amount;
                for (int i = 0; i < 9; i++) r.grid[i] = plugin.items().tokenFor(inv.getItem(GRID[i]));
                if (r.isEmpty()) {
                    p.sendMessage(Ui.text("&c[ACI] Das Gitter ist leer."));
                    return;
                }
                if (index < ci.recipes.size()) ci.recipes.set(index, r);
                else ci.recipes.add(r);
                plugin.items().changed();
                p.sendMessage(Ui.text("&a[ACI] Rezept gespeichert."));
                new RecipeListMenu(plugin, ci).open(p);
            }
            default -> { }
        }
    }

    @Override
    public void onBottomClick(InventoryClickEvent e) {
        if (e.isShiftClick()) {
            e.setCancelled(true);
            ItemStack item = e.getCurrentItem();
            if (item == null || item.getType().isAir()) return;
            for (int slot : GRID) {
                if (inv.getItem(slot) == null || inv.getItem(slot).getType().isAir()) {
                    setGhost(slot, item);
                    return;
                }
            }
            return;
        }
        super.onBottomClick(e);
    }
}
