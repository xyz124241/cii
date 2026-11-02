package de.aci;

import org.bukkit.Material;

/** In welchem Slot ein Custom Item seinen Effekt gibt. */
public enum EquipSlot {
    MAIN_HAND("Haupthand", Material.IRON_SWORD),
    OFF_HAND("Nebenhand", Material.SHIELD),
    HELMET("Helm", Material.IRON_HELMET),
    CHESTPLATE("Brustplatte", Material.IRON_CHESTPLATE),
    LEGGINGS("Hose", Material.IRON_LEGGINGS),
    BOOTS("Schuhe", Material.IRON_BOOTS),
    ANYWHERE("Inventar", Material.CHEST);

    public final String label;
    public final Material icon;

    EquipSlot(String label, Material icon) {
        this.label = label;
        this.icon = icon;
    }

    public EquipSlot next() {
        EquipSlot[] v = values();
        return v[(ordinal() + 1) % v.length];
    }
}
