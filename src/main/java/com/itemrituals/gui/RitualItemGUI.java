package com.itemrituals.gui;

import com.itemrituals.plugin.ItemRitualsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;

public class RitualItemGUI implements Listener {
    private final ItemRitualsPlugin plugin;
    public RitualItemGUI(ItemRitualsPlugin plugin) { this.plugin = plugin; }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, plugin.getConfigManager().getGuiSize(), plugin.getConfigManager().getGuiTitle());

        // Fill with filler
        if (plugin.getConfigManager().isFillerEnabled()) {
            ItemStack filler = createItem(Material.valueOf(plugin.getConfigManager().getFillerMaterial()),
                    plugin.getConfigManager().getFillerName(), new ArrayList<>());
            for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, filler);
        }

        // Add Item Instruction (slot 45)
        List<String> addInstructionLore = new ArrayList<>();
        addInstructionLore.add("§aClick on an item in your inventory");
        addInstructionLore.add("§ato add it to the pool");
        ItemStack addInstruction = createItem(Material.LIME_DYE, "§aClick on item to add", addInstructionLore);
        inv.setItem(45, addInstruction);

        // Remove Item Instruction (slot 46)
        List<String> removeInstructionLore = new ArrayList<>();
        removeInstructionLore.add("§cRight-click on an item in the GUI");
        removeInstructionLore.add("§cto remove it from the pool");
        ItemStack removeInstruction = createItem(Material.RED_DYE, "§cClick on GUI item to remove", removeInstructionLore);
        inv.setItem(46, removeInstruction);

        // Pool items (slots 0-44)
        List<ItemStack> items = plugin.getItemPoolManager().getPoolItems();
        int start = plugin.getConfigManager().getPoolSlotsStart();
        int end = 44;
        for (int i = 0; i < items.size() && start + i <= end; i++) {
            ItemStack display = items.get(i).clone();
            ItemMeta meta = display.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                lore.add("");
                if (plugin.getItemPoolManager().isReserved(i)) {
                    lore.add("§c§l🔒 RESERVED");
                }
                meta.setLore(lore);
                display.setItemMeta(meta);
            }
            inv.setItem(start + i, display);
        }

        // Start Button (slot 49)
        ItemStack startBtn = createItem(Material.NETHER_STAR, plugin.getConfigManager().getStartButtonName(),
                plugin.getConfigManager().getStartButtonLore());
        inv.setItem(plugin.getConfigManager().getStartButtonSlot(), startBtn);

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(plugin.getConfigManager().getGuiTitle())) return;
        e.setCancelled(true);
        Player player = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();
        int inventorySize = plugin.getConfigManager().getGuiSize();
        ItemStack clickedItem = e.getCurrentItem();

        // Handle Start Button
        if (slot == plugin.getConfigManager().getStartButtonSlot()) {
            if (plugin.getRitualManager().isActive()) {
                player.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getRitualAlreadyActive());
                return;
            }
            if (plugin.getRitualManager().startRitual()) {
                player.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getRitualStarted());
                player.closeInventory();
            } else {
                player.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getPoolEmpty());
            }
            return;
        }

        // Handle clicking on player's inventory (add item to pool)
        if (slot >= inventorySize) {
            if (!player.hasPermission("ritual.item")) {
                player.sendMessage(plugin.getConfigManager().getPrefix() + "§cYou do not have permission to modify the item pool.");
                return;
            }
            if (clickedItem != null && !clickedItem.getType().isAir()) {
                if (!plugin.getItemPoolManager().isInPool(clickedItem)) {
                    plugin.getItemPoolManager().addItem(clickedItem);
                    player.sendMessage(plugin.getConfigManager().getPrefix() + "§aItem added to the Ritual Item Pool.");
                } else {
                    player.sendMessage(plugin.getConfigManager().getPrefix() + "§cThis item is already in the pool.");
                }
            }
            open(player);
            return;
        }

        // Handle Pool Items (slots 0-44)
        if (slot >= 0 && slot <= 44) {
            if (!player.hasPermission("ritual.item")) {
                player.sendMessage(plugin.getConfigManager().getPrefix() + "§cYou do not have permission to modify the item pool.");
                return;
            }

            // Right-click to remove item from pool
            if (e.getClick().isRightClick() && clickedItem != null && !clickedItem.getType().isAir()) {
                int index = slot;
                if (index < plugin.getItemPoolManager().getPoolSize()) {
                    plugin.getItemPoolManager().removeItem(index);
                    player.sendMessage(plugin.getConfigManager().getPrefix() + "§cItem removed from the Ritual Item Pool.");
                }
            }
            open(player);
        }
    }

    @EventHandler public void onDrag(InventoryDragEvent e) {
        if (e.getView().getTitle().equals(plugin.getConfigManager().getGuiTitle())) e.setCancelled(true);
    }

    private ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
