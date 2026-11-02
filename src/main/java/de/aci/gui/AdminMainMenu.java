package de.aci.gui;

import de.aci.AciPlugin;
import de.aci.CustomItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** /aci - Uebersicht aller Custom Items; Ausgangspunkt zum Erstellen und Bearbeiten. */
public final class AdminMainMenu extends Menu {
    private final int page;
    private final int pages;
    private final List<CustomItem> list;

    public AdminMainMenu(AciPlugin plugin, int page) {
        super(plugin);
        this.list = new ArrayList<>(plugin.items().all());
        this.pages = Math.max(1, (list.size() + 44) / 45);
        this.page = Math.min(Math.max(page, 0), pages - 1);
        inv = Bukkit.createInventory(this, 54, Ui.text("&8ACI Admin &7(" + (this.page + 1) + "/" + pages + ")"));

        for (int i = 0; i < 45; i++) {
            int idx = this.page * 45 + i;
            if (idx >= list.size()) break;
            CustomItem ci = list.get(idx);
            ItemStack st = plugin.items().create(ci, 1);
            Ui.addLore(st, "", "&8ID: " + ci.id,
                    "&7Rezepte: &f" + ci.recipes.size(),
                    "&eLinksklick: &7bearbeiten",
                    "&eRechtsklick: &7Item erhalten");
            inv.setItem(i, st);
        }
        for (int i = 45; i < 54; i++) inv.setItem(i, Ui.pane());
        if (this.page > 0) inv.setItem(45, Ui.button(Material.ARROW, "&eVorherige Seite"));
        if (this.page < pages - 1) inv.setItem(53, Ui.button(Material.ARROW, "&eNaechste Seite"));
        inv.setItem(49, Ui.button(Material.NETHER_STAR, "&aNeues Custom Item",
                "&7Erstellt ein neues Item und", "&7oeffnet den Editor."));
        inv.setItem(47, Ui.button(Material.PAINTING, "&bPack neu laden & senden",
                "&7Liest PNGs aus &fplugins/ACI/textures", "&7neu ein und schickt das Pack",
                "&7an alle Spieler."));
        inv.setItem(51, Ui.button(Material.BOOK, "&6Rezeptbuch (/ci)", "&7So sehen es die Spieler."));
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        int slot = e.getSlot();

        if (slot < 45) {
            int idx = page * 45 + slot;
            if (idx >= list.size()) return;
            CustomItem ci = list.get(idx);
            if (e.getClick() == ClickType.RIGHT || e.getClick() == ClickType.SHIFT_RIGHT) {
                plugin.items().give(p, ci, e.isShiftClick() ? ci.material.getMaxStackSize() : 1);
            } else {
                new ItemEditorMenu(plugin, ci).open(p);
            }
            return;
        }
        switch (slot) {
            case 45 -> {
                if (page > 0) new AdminMainMenu(plugin, page - 1).open(p);
            }
            case 53 -> {
                if (page < pages - 1) new AdminMainMenu(plugin, page + 1).open(p);
            }
            case 47 -> {
                plugin.reloadAll();
                p.sendMessage(Ui.text("&a[ACI] Pack neu gebaut und gesendet (" + plugin.pack().textureNames().size() + " Texturen)."));
                new AdminMainMenu(plugin, page).open(p);
            }
            case 51 -> new RecipeIndexMenu(plugin, 0).open(p);
            case 49 -> plugin.prompts().ask(p,
                    "Gib die ID des neuen Items ein (nur a-z, 0-9, _).",
                    text -> {
                        String id = text.toLowerCase(Locale.ROOT);
                        if (!id.matches("[a-z0-9_]{1,32}")) {
                            p.sendMessage(Ui.text("&c[ACI] Ungueltige ID. Erlaubt: a-z, 0-9 und _ (max. 32 Zeichen)."));
                            new AdminMainMenu(plugin, page).open(p);
                        } else if (plugin.items().get(id) != null) {
                            p.sendMessage(Ui.text("&c[ACI] Diese ID gibt es schon."));
                            new AdminMainMenu(plugin, page).open(p);
                        } else {
                            new ItemEditorMenu(plugin, plugin.items().createItem(id)).open(p);
                        }
                    },
                    () -> new AdminMainMenu(plugin, page).open(p));
            default -> { }
        }
    }
}
