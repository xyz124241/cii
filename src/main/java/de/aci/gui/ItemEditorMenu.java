package de.aci.gui;

import de.aci.AciPlugin;
import de.aci.CustomItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/** Editor fuer ein einzelnes Custom Item. */
public final class ItemEditorMenu extends Menu {
    private final CustomItem ci;

    public ItemEditorMenu(AciPlugin plugin, CustomItem ci) {
        super(plugin);
        this.ci = ci;
        inv = Bukkit.createInventory(this, 27, Ui.text("&8Item: &0" + Ui.plain(ci.name)));
        for (int i = 0; i < 27; i++) inv.setItem(i, Ui.pane());

        inv.setItem(4, plugin.items().create(ci, 1));
        inv.setItem(10, Ui.button(Material.NAME_TAG, "&eName aendern",
                "&7Aktuell: &r" + ci.name, "&8MiniMessage, z.B.:", "&8\\<gradient:#ff4500:#ffd000>Name\\</gradient>", "&8Alte &-Codes gehen auch."));
        inv.setItem(11, Ui.button(Material.PAINTING, "&eTextur waehlen",
                "&7Aktuell: &f" + ci.texture));
        inv.setItem(12, Ui.button(Material.WRITABLE_BOOK, "&eBeschreibung (Lore)",
                "&7Zeilen: &f" + ci.lore.size(),
                "&eKlick: &7Zeile hinzufuegen",
                "&eShift-Klick: &7letzte Zeile entfernen"));
        inv.setItem(13, Ui.button(Material.POTION, "&eEffekte",
                "&7Anzahl: &f" + ci.effects.size(),
                "&7Dauerhafte Effekte im Slot", "&7(z.B. Nebenhand)."));
        inv.setItem(14, Ui.button(Material.CRAFTING_TABLE, "&eRezepte",
                "&7Anzahl: &f" + ci.recipes.size()));
        inv.setItem(15, Ui.addLore(new ItemStack(ci.material),
                "&7", "&eBasis-Item: &f" + ci.material.name(),
                "&7Klicke mit einem anderen Item", "&7auf dem Cursor, um es zu aendern."));
        inv.setItem(16, Ui.button(Material.CHEST, "&aItem erhalten",
                "&eKlick: &71x", "&eShift-Klick: &7Stack"));
        inv.setItem(18, Ui.button(Material.ARROW, "&cZurueck"));
        inv.setItem(26, Ui.button(Material.TNT, "&4Item loeschen",
                "&cShift-Klick zum Bestaetigen!"));
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        switch (e.getSlot()) {
            case 10 -> plugin.prompts().ask(p, "Neuer Anzeigename fuer " + ci.id + " (MiniMessage, z.B. \\<gradient:#ff4500:#ffd000>Feuer-Kristall\\</gradient> oder \\<bold>\\<red>Name):",
                    text -> {
                        ci.name = text;
                        plugin.items().changed();
                        new ItemEditorMenu(plugin, ci).open(p);
                    },
                    () -> new ItemEditorMenu(plugin, ci).open(p));
            case 11 -> new TextureMenu(plugin, ci, 0).open(p);
            case 12 -> {
                if (e.isShiftClick()) {
                    if (!ci.lore.isEmpty()) ci.lore.remove(ci.lore.size() - 1);
                    plugin.items().changed();
                    new ItemEditorMenu(plugin, ci).open(p);
                } else {
                    plugin.prompts().ask(p, "Neue Lore-Zeile (MiniMessage oder &-Codes):",
                            text -> {
                                ci.lore.add(text);
                                plugin.items().changed();
                                new ItemEditorMenu(plugin, ci).open(p);
                            },
                            () -> new ItemEditorMenu(plugin, ci).open(p));
                }
            }
            case 13 -> new EffectsMenu(plugin, ci, 0).open(p);
            case 14 -> new RecipeListMenu(plugin, ci).open(p);
            case 15 -> {
                ItemStack cursor = e.getCursor();
                if (cursor == null || cursor.getType().isAir()) return;
                if (plugin.items().idOf(cursor) != null || !cursor.getType().isItem()) {
                    p.sendMessage(Ui.text("&c[ACI] Als Basis geht nur ein normales Vanilla-Item."));
                    return;
                }
                ci.material = cursor.getType();
                plugin.items().changed();
                new ItemEditorMenu(plugin, ci).open(p);
            }
            case 16 -> plugin.items().give(p, ci, e.isShiftClick() ? ci.material.getMaxStackSize() : 1);
            case 18 -> new AdminMainMenu(plugin, 0).open(p);
            case 26 -> {
                if (!e.isShiftClick()) {
                    p.sendMessage(Ui.text("&c[ACI] Zum Loeschen Shift-Klick benutzen."));
                    return;
                }
                plugin.items().remove(ci);
                p.sendMessage(Ui.text("&a[ACI] Item " + ci.id + " geloescht."));
                new AdminMainMenu(plugin, 0).open(p);
            }
            default -> { }
        }
    }
}
