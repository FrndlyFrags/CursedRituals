package com.itemrituals.ritual;

import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import java.util.UUID;

public class Ritual {

    private final ItemStack rewardItem;
    private final Location centerLocation;
    private final long startTime;
    private final int duration;
    private final int radius;
    private RitualPhase phase;
    private Item droppedItem;
    private UUID droppedItemUUID;
    private boolean completed;
    private boolean cancelled;
    private int warningsSent;

    public Ritual(ItemStack rewardItem, Location centerLocation, int duration, int radius) {
        this.rewardItem = rewardItem;
        this.centerLocation = centerLocation;
        this.startTime = System.currentTimeMillis();
        this.duration = duration * 1000;
        this.radius = radius;
        this.phase = RitualPhase.AWAKENING;
        this.completed = false;
        this.cancelled = false;
        this.warningsSent = 0;
    }

    public long getRemainingTime() {
        long elapsed = System.currentTimeMillis() - startTime;
        long remaining = duration - elapsed;
        return Math.max(0, remaining);
    }

    public double getProgress() {
        long elapsed = System.currentTimeMillis() - startTime;
        return Math.min(1.0, (double) elapsed / duration);
    }

    public void updatePhase() {
        double progress = getProgress();
        if (progress >= 1.0) {
            phase = RitualPhase.COMPLETION;
        } else if (progress >= 0.75) {
            phase = RitualPhase.FINAL;
        } else if (progress >= 0.50) {
            phase = RitualPhase.INTENSIFYING;
        } else if (progress >= 0.25) {
            phase = RitualPhase.GATHERING;
        } else {
            phase = RitualPhase.AWAKENING;
        }
    }

    public ItemStack getRewardItem() { return rewardItem; }
    public Location getCenterLocation() { return centerLocation; }
    public long getStartTime() { return startTime; }
    public int getDuration() { return duration / 1000; }
    public int getRadius() { return radius; }
    public RitualPhase getPhase() { return phase; }
    public Item getDroppedItem() {
        if (droppedItemUUID == null) return null;
        if (droppedItem == null || !droppedItem.isValid()) {
            org.bukkit.entity.Entity entity = org.bukkit.Bukkit.getEntity(droppedItemUUID);
            if (entity instanceof Item) {
                droppedItem = (Item) entity;
            } else {
                droppedItem = null;
            }
        }
        return droppedItem;
    }
    public void setDroppedItem(Item droppedItem) {
        this.droppedItem = droppedItem;
        this.droppedItemUUID = droppedItem != null ? droppedItem.getUniqueId() : null;
    }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public boolean isCancelled() { return cancelled; }
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
    public int getWarningsSent() { return warningsSent; }
    public void incrementWarnings() { warningsSent++; }
}
