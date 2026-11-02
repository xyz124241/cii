package de.aci.gui;

import de.aci.AciPlugin;
import de.aci.ItemManager.RecipeRef;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/** /ci - Uebersicht aller Rezepte (fuer alle Spieler). Klick oeffnet die Seite des Rezepts. */
public final class RecipeIndexMenu extends Menu {
    private final int page;
    private final int pages;
    private final List<RecipeRef> refs;

    public RecipeIndexMenu(AciPlugin plugin, int page) {
        super(plugin);
        this.refs = plugin.items().refs();
        this.pages = Math.max(1, (refs.size() + 44) / 45);
        this.page = Math.min(Math.max(page, 0), pages - 1);
        inv = Bukkit.createInventory(this, 54, Ui.text("&8Rezeptbuch &7(" + (this.page + 1) + "/" + pages + ")"));

        for (int i = 0; i < 45; i++) {
            int idx = this.page * 45 + i;
            if (idx >= refs.size()) break;
            RecipeRef ref = refs.get(idx);
            ItemStack st = plugin.items().create(ref.item(), ref.item().recipes.get(ref.index()).amount);
            Ui.addLore(st, "", "&6Rezept " + (idx + 1) + " von " + refs.size(), "&eKlick: &7Rezept ansehen");
            inv.setItem(i, st);
        }
        for (int i = 45; i < 54; i++) inv.setItem(i, Ui.pane());
        if (this.page > 0) inv.setItem(45, Ui.button(Material.ARROW, "&eVorherige Seite"));
        if (this.page < pages - 1) inv.setItem(53, Ui.button(Material.ARROW, "&eNaechste Seite"));
        inv.setItem(49, Ui.button(Material.BOOK, "&6Rezeptbuch",
                refs.isEmpty() ? "&7Noch keine Rezepte vorhanden." : "&7" + refs.size() + " Rezepte"));
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        int slot = e.getSlot();
        if (slot < 45) {
            int idx = page * 45 + slot;
            if (idx < refs.size()) new RecipeBookMenu(plugin, idx).open(p);
        } else if (slot == 45 && page > 0) {
            new RecipeIndexMenu(plugin, page - 1).open(p);
        } else if (slot == 53 && page < pages - 1) {
            new RecipeIndexMenu(plugin, page + 1).open(p);
        }
    }
}
