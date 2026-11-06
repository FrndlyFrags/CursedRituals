package com.itemrituals.config;

import com.itemrituals.plugin.ItemRitualsPlugin;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ConfigManager {

    // Obsidian Cage
    private boolean obsidianCageEnabled;
    private int obsidianCageSize;
    private int obsidianCageDuration;
    private int obsidianCageWarningTime;
    private String obsidianCageMaterial;
    private boolean obsidianCageAnnounce;
    private List<String> obsidianCageAnnounceMsg;
    private int obsidianCageBlocksPerTick;
    private int obsidianCageAnnouncementDelay;

    private final ItemRitualsPlugin plugin;
    private FileConfiguration config;

    // Settings
    private boolean debug;

    // Ritual
    private int ritualDuration;
    private int ritualRadius;
    private int phaseInterval;
    private int warningCount;

    // Selection
    private SelectionMode selectionMode;
    private boolean removeOnCompletion;
    private boolean returnIfCancelled;
    private List<String> priorityMaterials;

    // Location
    private boolean randomLocation;
    private int minDistanceFromSpawn;
    private int maxDistanceFromSpawn;
    private int minDistanceFromPlayers;
    private List<String> allowedWorlds;
    private int maxAttempts;
    private boolean avoidWater;
    private boolean avoidLava;
    private boolean avoidVoid;
    private boolean avoidRegions;
    private int minY;
    private int maxY;

    // BossBar
    private boolean bossBarEnabled;
    private BarColor bossBarColor;
    private BarStyle bossBarStyle;
    private String bossBarTitle;
    private int bossBarUpdateInterval;

    // Particles
    private boolean particlesEnabled;
    private int particleInterval;
    private List<String> particleTypes;
    private boolean particleIntensityScaling;

    // Sounds
    private boolean soundsEnabled;
    private SoundData awakeningSound;
    private SoundData warningSound;
    private SoundData ambientSound;
    private SoundData completionSound;
    private SoundData cancellationSound;
    private SoundData lightningSound;

    // Lightning
    private boolean lightningEnabled;
    private boolean lightningVisualOnly;
    private int lightningBaseInterval;
    private boolean lightningIntensityScaling;
    private int lightningMinInterval;

    // Protection
    private boolean preventPickup;
    private boolean preventHopperPickup;
    private boolean preventExplosion;
    private boolean preventDespawn;
    private boolean preventFire;
    private boolean glowEffect;

    // Announcements
    private boolean announceStart;
    private boolean announceWarning;
    private boolean announceComplete;
    private boolean announceCancel;
    private int announceLocalRadius;

    // Messages
    private String prefix;
    private String noPermission;
    private String noActiveRitual;
    private String ritualAlreadyActive;
    private String ritualStarted;
    private String ritualStopped;
    private String ritualCompleted;
    private String ritualCancelled;
    private String itemAdded;
    private String itemRemoved;
    private String poolEmpty;
    private String configReloaded;
    private List<String> announceStartMsg;
    private List<String> announceWarningMsg;
    private List<String> announceCompleteMsg;
    private List<String> announceCancelMsg;

    // GUI
    private String guiTitle;
    private int guiSize;
    private int startButtonSlot;
    private String startButtonName;
    private List<String> startButtonLore;
    private boolean fillerEnabled;
    private String fillerMaterial;
    private String fillerName;
    private int poolSlotsStart;
    private int poolSlotsEnd;

    public ConfigManager(ItemRitualsPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        this.config = plugin.getConfig();
        loadValues();
    }

    private void loadValues() {
        // Obsidian Cage
        obsidianCageEnabled = config.getBoolean("obsidian-cage.enabled", true);
        obsidianCageSize = config.getInt("obsidian-cage.size", 10);
        obsidianCageDuration = config.getInt("obsidian-cage.duration", 120);
        obsidianCageWarningTime = config.getInt("obsidian-cage.warning-time", 120);
        obsidianCageMaterial = config.getString("obsidian-cage.material", "OBSIDIAN");
        obsidianCageAnnounce = config.getBoolean("obsidian-cage.announce", true);
        obsidianCageAnnounceMsg = config.getStringList("obsidian-cage.announce-msg");
        obsidianCageBlocksPerTick = config.getInt("obsidian-cage.blocks-per-tick", 5);
        obsidianCageAnnouncementDelay = config.getInt("obsidian-cage.announcement-delay", 5);
        debug = config.getBoolean("settings.debug", false);

        ritualDuration = config.getInt("ritual.duration", 600);
        ritualRadius = config.getInt("ritual.radius", 25);
        phaseInterval = config.getInt("ritual.phase-interval", 120);
        warningCount = config.getInt("ritual.warning-count", 3);

        selectionMode = SelectionMode.valueOf(config.getString("ritual-selection.mode", "RANDOM").toUpperCase());
        removeOnCompletion = config.getBoolean("ritual-selection.remove-item-on-completion", true);
        returnIfCancelled = config.getBoolean("ritual-selection.return-item-if-cancelled", true);
        priorityMaterials = config.getStringList("ritual-selection.priority-materials");

        randomLocation = config.getBoolean("location.random", true);
        minDistanceFromSpawn = config.getInt("location.min-distance-from-spawn", 1000);
        maxDistanceFromSpawn = config.getInt("location.max-distance-from-spawn", 5000);
        minDistanceFromPlayers = config.getInt("location.min-distance-from-players", 300);
        allowedWorlds = config.getStringList("location.allowed-worlds");
        maxAttempts = config.getInt("location.max-attempts", 50);
        avoidWater = config.getBoolean("location.restrictions.avoid-water", true);
        avoidLava = config.getBoolean("location.restrictions.avoid-lava", true);
        avoidVoid = config.getBoolean("location.restrictions.avoid-void", true);
        avoidRegions = config.getBoolean("location.restrictions.avoid-regions", true);
        minY = config.getInt("location.restrictions.min-y", 50);
        maxY = config.getInt("location.restrictions.max-y", 120);

        bossBarEnabled = config.getBoolean("bossbar.enabled", true);
        bossBarColor = BarColor.valueOf(config.getString("bossbar.color", "RED").toUpperCase());
        bossBarStyle = BarStyle.valueOf(config.getString("bossbar.style", "SOLID").toUpperCase());
        bossBarTitle = config.getString("bossbar.title", "☠ RITUAL: {item} — X: {x} Y: {y} Z: {z} | Remaining: {time}");
        bossBarUpdateInterval = config.getInt("bossbar.update-interval", 20);

        particlesEnabled = config.getBoolean("particles.enabled", true);
        particleInterval = config.getInt("particles.interval", 10);
        particleTypes = config.getStringList("particles.types");
        particleIntensityScaling = config.getBoolean("particles.intensity-scaling", true);

        soundsEnabled = config.getBoolean("sounds.enabled", true);
        awakeningSound = loadSoundData("sounds.awakening");
        warningSound = loadSoundData("sounds.warning");
        ambientSound = loadSoundData("sounds.ambient");
        completionSound = loadSoundData("sounds.completion");
        cancellationSound = loadSoundData("sounds.cancellation");
        lightningSound = loadSoundData("sounds.lightning");

        lightningEnabled = config.getBoolean("lightning.enabled", true);
        lightningVisualOnly = config.getBoolean("lightning.visual-only", true);
        lightningBaseInterval = config.getInt("lightning.base-interval", 30);
        lightningIntensityScaling = config.getBoolean("lightning.intensity-scaling", true);
        lightningMinInterval = config.getInt("lightning.min-interval", 3);

        preventPickup = config.getBoolean("protection.prevent-pickup", true);
        preventHopperPickup = config.getBoolean("protection.prevent-hopper-pickup", true);
        preventExplosion = config.getBoolean("protection.prevent-explosion", true);
        preventDespawn = config.getBoolean("protection.prevent-despawn", true);
        preventFire = config.getBoolean("protection.prevent-fire", true);
        glowEffect = config.getBoolean("protection.glow-effect", true);

        announceStart = config.getBoolean("announcements.start", true);
        announceWarning = config.getBoolean("announcements.warning", true);
        announceComplete = config.getBoolean("announcements.complete", true);
        announceCancel = config.getBoolean("announcements.cancel", true);
        announceLocalRadius = config.getInt("announcements.local-radius", 0);

        prefix = config.getString("messages.prefix", "&8[&c☠&8] &7");
        noPermission = config.getString("messages.no-permission", "&cYou do not have permission to use this command.");
        noActiveRitual = config.getString("messages.no-active-ritual", "&cThere is no active ritual.");
        ritualAlreadyActive = config.getString("messages.ritual-already-active", "&cA ritual is already active.");
        ritualStarted = config.getString("messages.ritual-started", "&aRitual started successfully.");
        ritualStopped = config.getString("messages.ritual-stopped", "&cRitual has been stopped.");
        ritualCompleted = config.getString("messages.ritual-completed", "&aRitual completed! The relic has been awakened.");
        ritualCancelled = config.getString("messages.ritual-cancelled", "&cRitual has been cancelled.");
        itemAdded = config.getString("messages.item-added", "&aItem added to the Ritual Item Pool.");
        itemRemoved = config.getString("messages.item-removed", "&cItem removed from the Ritual Item Pool.");
        poolEmpty = config.getString("messages.pool-empty", "&cThe Ritual Item Pool is empty.");
        configReloaded = config.getString("messages.config-reloaded", "&aConfiguration reloaded.");
        announceStartMsg = config.getStringList("messages.announce-start");
        announceWarningMsg = config.getStringList("messages.announce-warning");
        announceCompleteMsg = config.getStringList("messages.announce-complete");
        announceCancelMsg = config.getStringList("messages.announce-cancel");

        guiTitle = config.getString("gui.title", "&8☠ Ritual Items ☠");
        guiSize = config.getInt("gui.size", 54);
        startButtonSlot = config.getInt("gui.start-button-slot", 49);
        startButtonName = config.getString("gui.start-button-name", "&c&lSTART RITUAL");
        startButtonLore = config.getStringList("gui.start-button-lore");
        fillerEnabled = config.getBoolean("gui.filler-enabled", true);
        fillerMaterial = config.getString("gui.filler-material", "BLACK_STAINED_GLASS_PANE");
        fillerName = config.getString("gui.filler-name", " ");
        poolSlotsStart = config.getInt("gui.pool-slots.start", 0);
        poolSlotsEnd = config.getInt("gui.pool-slots.end", 44);
    }

    private SoundData loadSoundData(String path) {
        String soundName = config.getString(path + ".sound", "BLOCK_NOTE_BLOCK_PLING");
        float volume = (float) config.getDouble(path + ".volume", 1.0);
        float pitch = (float) config.getDouble(path + ".pitch", 1.0);
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            return new SoundData(sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound: " + soundName);
            return new SoundData(Sound.BLOCK_NOTE_BLOCK_PLING, volume, pitch);
        }
    }

    public String color(String msg) {
        return msg.replace('&', '§');
    }

    public String getPrefix() {
        return color(prefix);
    }

    // Getters
    public boolean isDebug() { return debug; }
    public int getRitualDuration() { return ritualDuration; }
    public int getRitualRadius() { return ritualRadius; }
    public int getPhaseInterval() { return phaseInterval; }
    public int getWarningCount() { return warningCount; }
    public SelectionMode getSelectionMode() { return selectionMode; }
    public boolean isRemoveOnCompletion() { return removeOnCompletion; }
    public boolean isReturnIfCancelled() { return returnIfCancelled; }
    public List<String> getPriorityMaterials() { return priorityMaterials; }
    public boolean isRandomLocation() { return randomLocation; }
    public int getMinDistanceFromSpawn() { return minDistanceFromSpawn; }
    public int getMaxDistanceFromSpawn() { return maxDistanceFromSpawn; }
    public int getMinDistanceFromPlayers() { return minDistanceFromPlayers; }
    public List<String> getAllowedWorlds() { return allowedWorlds; }
    public int getMaxAttempts() { return maxAttempts; }
    public boolean isAvoidWater() { return avoidWater; }
    public boolean isAvoidLava() { return avoidLava; }
    public boolean isAvoidVoid() { return avoidVoid; }
    public boolean isAvoidRegions() { return avoidRegions; }
    public int getMinY() { return minY; }
    public int getMaxY() { return maxY; }
    public boolean isBossBarEnabled() { return bossBarEnabled; }
    public BarColor getBossBarColor() { return bossBarColor; }
    public BarStyle getBossBarStyle() { return bossBarStyle; }
    public String getBossBarTitle() { return bossBarTitle; }
    public int getBossBarUpdateInterval() { return bossBarUpdateInterval; }
    public boolean isParticlesEnabled() { return particlesEnabled; }
    public int getParticleInterval() { return particleInterval; }
    public List<String> getParticleTypes() { return particleTypes; }
    public boolean isParticleIntensityScaling() { return particleIntensityScaling; }
    public boolean isSoundsEnabled() { return soundsEnabled; }
    public SoundData getAwakeningSound() { return awakeningSound; }
    public SoundData getWarningSound() { return warningSound; }
    public SoundData getAmbientSound() { return ambientSound; }
    public SoundData getCompletionSound() { return completionSound; }
    public SoundData getCancellationSound() { return cancellationSound; }
    public SoundData getLightningSound() { return lightningSound; }
    public boolean isLightningEnabled() { return lightningEnabled; }
    public boolean isLightningVisualOnly() { return lightningVisualOnly; }
    public int getLightningBaseInterval() { return lightningBaseInterval; }
    public boolean isLightningIntensityScaling() { return lightningIntensityScaling; }
    public int getLightningMinInterval() { return lightningMinInterval; }
    public boolean isPreventPickup() { return preventPickup; }
    public boolean isPreventHopperPickup() { return preventHopperPickup; }
    public boolean isPreventExplosion() { return preventExplosion; }
    public boolean isPreventDespawn() { return preventDespawn; }
    public boolean isPreventFire() { return preventFire; }
    public boolean isGlowEffect() { return glowEffect; }
    public boolean isAnnounceStart() { return announceStart; }
    public boolean isAnnounceWarning() { return announceWarning; }
    public boolean isAnnounceComplete() { return announceComplete; }
    public boolean isAnnounceCancel() { return announceCancel; }
    public int getAnnounceLocalRadius() { return announceLocalRadius; }
    public String getNoPermission() { return color(noPermission); }
    public String getNoActiveRitual() { return color(noActiveRitual); }
    public String getRitualAlreadyActive() { return color(ritualAlreadyActive); }
    public String getRitualStarted() { return color(ritualStarted); }
    public String getRitualStopped() { return color(ritualStopped); }
    public String getRitualCompleted() { return color(ritualCompleted); }
    public String getRitualCancelled() { return color(ritualCancelled); }
    public String getItemAdded() { return color(itemAdded); }
    public String getItemRemoved() { return color(itemRemoved); }
    public String getPoolEmpty() { return color(poolEmpty); }
    public String getConfigReloaded() { return color(configReloaded); }
    public List<String> getAnnounceStartMsg() { return announceStartMsg.stream().map(this::color).toList(); }
    public List<String> getAnnounceWarningMsg() { return announceWarningMsg.stream().map(this::color).toList(); }
    public List<String> getAnnounceCompleteMsg() { return announceCompleteMsg.stream().map(this::color).toList(); }
    public List<String> getAnnounceCancelMsg() { return announceCancelMsg.stream().map(this::color).toList(); }
    public String getGuiTitle() { return color(guiTitle); }
    public int getGuiSize() { return guiSize; }
    public int getStartButtonSlot() { return startButtonSlot; }
    public String getStartButtonName() { return color(startButtonName); }
    public List<String> getStartButtonLore() { return startButtonLore.stream().map(this::color).toList(); }
    public boolean isFillerEnabled() { return fillerEnabled; }
    public String getFillerMaterial() { return fillerMaterial; }
    public String getFillerName() { return color(fillerName); }
    public int getPoolSlotsStart() { return poolSlotsStart; }
    public int getPoolSlotsEnd() { return poolSlotsEnd; }

        // Getters for Obsidian Cage
    public boolean isObsidianCageEnabled() { return obsidianCageEnabled; }
    public int getObsidianCageSize() { return obsidianCageSize; }
    public int getObsidianCageDuration() { return obsidianCageDuration; }
    public int getObsidianCageWarningTime() { return obsidianCageWarningTime; }
    public String getObsidianCageMaterial() { return obsidianCageMaterial; }
    public boolean isObsidianCageAnnounce() { return obsidianCageAnnounce; }
    public List<String> getObsidianCageAnnounceMsg() { return obsidianCageAnnounceMsg.stream().map(this::color).toList(); }
    public int getObsidianCageBlocksPerTick() { return obsidianCageBlocksPerTick; }
    public int getObsidianCageAnnouncementDelay() { return obsidianCageAnnouncementDelay; }

    public enum SelectionMode {
        RANDOM, FIRST, PRIORITY
    }

    public record SoundData(Sound sound, float volume, float pitch) {}
}
