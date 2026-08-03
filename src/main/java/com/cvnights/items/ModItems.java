package com.cvnights.items;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class ModItems {

    private ModItems() {}

    public static Item NIGHT_WATCH;
    public static Item FOCUS_CANDY;
    public static ItemGroup ITEM_GROUP;

    public static void register() {
        NIGHT_WATCH = Registry.register(Registries.ITEM, Identifier.of("cvnights", "night_watch"),
                new NightWatchItem(new Item.Settings().maxCount(1)));

        FOCUS_CANDY = Registry.register(Registries.ITEM, Identifier.of("cvnights", "focus_candy"),
                new FocusCandyItem(new Item.Settings().maxCount(16)));

        RegistryKey<ItemGroup> groupKey = RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier.of("cvnights", "main"));
        ITEM_GROUP = Registry.register(Registries.ITEM_GROUP, groupKey, FabricItemGroup.builder()
                .displayName(Text.translatable("itemGroup.cvnights.main"))
                .icon(() -> new ItemStack(NIGHT_WATCH))
                .entries((context, entries) -> {
                    entries.add(NIGHT_WATCH);
                    entries.add(FOCUS_CANDY);
                })
                .build());
    }
}
