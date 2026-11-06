package com.itemrituals.plugin;

import com.itemrituals.announcement.AnnouncementManager;
import com.itemrituals.bossbar.RitualBossBarManager;
import com.itemrituals.commands.RitualCommand;
import com.itemrituals.config.ConfigManager;
import com.itemrituals.effects.EffectsManager;
import com.itemrituals.gui.RitualItemGUI;
import com.itemrituals.items.ItemPoolManager;
import com.itemrituals.location.LocationFinder;
import com.itemrituals.protection.ProtectionListener;
import com.itemrituals.cage.ObsidianCageManager;
import com.itemrituals.cage.CageAnnouncementManager;
import com.itemrituals.ritual.RitualManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemRitualsPlugin extends JavaPlugin {

    private static ItemRitualsPlugin instance;
    private ConfigManager configManager;
    private ItemPoolManager itemPoolManager;
    private RitualManager ritualManager;
    private LocationFinder locationFinder;
    private EffectsManager effectsManager;
    private RitualBossBarManager bossBarManager;
    private AnnouncementManager announcementManager;
    private RitualItemGUI ritualItemGUI;
    private ObsidianCageManager obsidianCageManager;
    private CageAnnouncementManager cageAnnouncementManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        this.itemPoolManager = new ItemPoolManager(this);
        this.locationFinder = new LocationFinder(this);
        this.effectsManager = new EffectsManager(this);
        this.bossBarManager = new RitualBossBarManager(this);
        this.announcementManager = new AnnouncementManager(this);
        this.ritualItemGUI = new RitualItemGUI(this);
        this.ritualManager = new RitualManager(this);
        this.obsidianCageManager = new ObsidianCageManager(this);
        this.cageAnnouncementManager = new CageAnnouncementManager(this);

        getCommand("ritual").setExecutor(new RitualCommand(this));
        getCommand("ritual").setTabCompleter(new RitualCommand(this));

        getServer().getPluginManager().registerEvents(new ProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(ritualItemGUI, this);

        getLogger().info("☠ Item Rituals has been enabled.");
    }

    @Override
    public void onDisable() {
        // Stop any active ritual and clean up the obsidian cage
        if (ritualManager != null && ritualManager.isActive()) {
            ritualManager.stopRitual(true);
        }
        // Ensure cage is removed on server shutdown
        if (obsidianCageManager != null) {
            obsidianCageManager.stopCage();
        }
        if (itemPoolManager != null) {
            itemPoolManager.savePool();
        }
        getLogger().info("☠ Item Rituals has been disabled.");
    }

    public void reload() {
        reloadConfig();
        configManager.reload();
        itemPoolManager.loadPool();
    }

    public static ItemRitualsPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ItemPoolManager getItemPoolManager() {
        return itemPoolManager;
    }

    public RitualManager getRitualManager() {
        return ritualManager;
    }

    public LocationFinder getLocationFinder() {
        return locationFinder;
    }

    public EffectsManager getEffectsManager() {
        return effectsManager;
    }

    public RitualBossBarManager getBossBarManager() {
        return bossBarManager;
    }

    public AnnouncementManager getAnnouncementManager() {
        return announcementManager;
    }

    public RitualItemGUI getRitualItemGUI() {
        return ritualItemGUI;
    }

    public ObsidianCageManager getObsidianCageManager() {
        return obsidianCageManager;
    }

    public CageAnnouncementManager getCageAnnouncementManager() {
        return cageAnnouncementManager;
    }
}
