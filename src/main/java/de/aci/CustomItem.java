package de.aci;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public final class CustomItem {
    public final String id;
    /** Anzeigename mit &-Farbcodes. */
    public String name;
    /** Basis-Item, auf dem das Custom Item aufbaut. */
    public Material material = Material.AMETHYST_SHARD;
    /** Name der Textur im Resource Pack (assets/aci/textures/item/&lt;texture&gt;.png). */
    public String texture;
    public final List<String> lore = new ArrayList<>();
    public final List<ItemEffect> effects = new ArrayList<>();
    public final List<CustomRecipe> recipes = new ArrayList<>();

    public CustomItem(String id) {
        this.id = id;
        this.name = "&f" + id;
        this.texture = id;
    }
}
