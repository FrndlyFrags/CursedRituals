package com.cursedrituals.items;

import com.cursedrituals.plugin.CursedRitualsPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ItemPoolManager {

    private final CursedRitualsPlugin plugin;
    private final File poolFile;
    private FileConfiguration poolConfig;
    private final List<ItemStack> poolItems;
    private final Set<Integer> reservedSlots;

    public ItemPoolManager(CursedRitualsPlugin plugin) {
        this.plugin = plugin;
        this.poolFile = new File(plugin.getDataFolder(), "pool.yml");
        this.poolItems = new ArrayList<>();
        this.reservedSlots = new HashSet<>();
        loadPool();
    }

    public void loadPool() {
        poolItems.clear();
        reservedSlots.clear();
        if (!poolFile.exists()) {
            try {
                poolFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        poolConfig = YamlConfiguration.loadConfiguration(poolFile);
        List<String> serialized = poolConfig.getStringList("items");
        for (String data : serialized) {
            ItemStack item = ItemSerializer.deserialize(data);
            if (item != null) {
                poolItems.add(item);
            }
        }
    }

    public void savePool() {
        List<String> serialized = new ArrayList<>();
        for (ItemStack item : poolItems) {
            String data = ItemSerializer.serialize(item);
            if (data != null) {
                serialized.add(data);
            }
        }
        poolConfig.set("items", serialized);
        try {
            poolConfig.save(poolFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addItem(ItemStack item) {
        if (item == null || item.getType().isAir()) return;
        ItemStack clone = item.clone();
        poolItems.add(clone);
        savePool();
    }

    public void removeItem(int index) {
        if (index >= 0 && index < poolItems.size()) {
            poolItems.remove(index);
            savePool();
        }
    }

    public ItemStack getItem(int index) {
        if (index >= 0 && index < poolItems.size()) {
            return poolItems.get(index).clone();
        }
        return null;
    }

    public List<ItemStack> getPoolItems() {
        List<ItemStack> copy = new ArrayList<>();
        for (ItemStack item : poolItems) {
            copy.add(item.clone());
        }
        return copy;
    }

    public int getPoolSize() {
        return poolItems.size();
    }

    public boolean isEmpty() {
        return poolItems.isEmpty();
    }

    public ItemStack selectItem() {
        List<Integer> available = new ArrayList<>();
        for (int i = 0; i < poolItems.size(); i++) {
            if (!reservedSlots.contains(i)) {
                available.add(i);
            }
        }
        if (available.isEmpty()) return null;

        int selectedIndex;
        switch (plugin.getConfigManager().getSelectionMode()) {
            case FIRST -> selectedIndex = available.get(0);
            case PRIORITY -> {
                selectedIndex = available.get(0);
                List<String> priorities = plugin.getConfigManager().getPriorityMaterials();
                for (int idx : available) {
                    String mat = poolItems.get(idx).getType().name();
                    if (priorities.contains(mat)) {
                        selectedIndex = idx;
                        break;
                    }
                }
            }
            default -> selectedIndex = available.get(new Random().nextInt(available.size()));
        }
        return poolItems.get(selectedIndex).clone();
    }

    public void reserveItem(ItemStack item) {
        for (int i = 0; i < poolItems.size(); i++) {
            if (poolItems.get(i).isSimilar(item)) {
                reservedSlots.add(i);
                break;
            }
        }
    }

    public void unreserveItem(ItemStack item) {
        for (int i = 0; i < poolItems.size(); i++) {
            if (poolItems.get(i).isSimilar(item)) {
                reservedSlots.remove(i);
                break;
            }
        }
    }

    public void removeReservedItem(ItemStack item) {
        for (int i = 0; i < poolItems.size(); i++) {
            if (poolItems.get(i).isSimilar(item)) {
                poolItems.remove(i);
                reservedSlots.remove(i);
                // Rebuild reserved indices
                Set<Integer> newReserved = new HashSet<>();
                for (int r : reservedSlots) {
                    if (r > i) {
                        newReserved.add(r - 1);
                    } else if (r < i) {
                        newReserved.add(r);
                    }
                }
                reservedSlots.clear();
                reservedSlots.addAll(newReserved);
                savePool();
                break;
            }
        }
    }

    public boolean isReserved(int index) {
        return reservedSlots.contains(index);
    }

    public boolean isInPool(ItemStack item) {
        for (ItemStack poolItem : poolItems) {
            if (poolItem.isSimilar(item)) {
                return true;
            }
        }
        return false;
    }

    public void clearPool() {
        poolItems.clear();
        reservedSlots.clear();
        savePool();
    }
}
