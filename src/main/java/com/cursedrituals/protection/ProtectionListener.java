package com.cursedrituals.protection;

import com.cursedrituals.plugin.CursedRitualsPlugin;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.entity.Player;

public class ProtectionListener implements Listener {
    private final CursedRitualsPlugin plugin;
    public ProtectionListener(CursedRitualsPlugin plugin) { this.plugin = plugin; }
    private boolean isProtected(Item item) {
        if (!item.hasMetadata("ritual_item")) return false;
        for (MetadataValue v : item.getMetadata("ritual_reward"))
            if (v.getOwningPlugin() == plugin && v.asBoolean()) return true;
        return false;
    }
    @EventHandler public void onPickup(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player player && plugin.getConfigManager().isPreventPickup() && isProtected(e.getItem())) {
            e.setCancelled(true);
        }
    }
    @EventHandler public void onDespawn(ItemDespawnEvent e) {
        if (plugin.getConfigManager().isPreventDespawn() && isProtected(e.getEntity())) e.setCancelled(true);
    }
    @EventHandler public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Item)) return;
        Item item = (Item) e.getEntity();
        if (!isProtected(item)) return;

        EntityDamageEvent.DamageCause cause = e.getCause();
        if (plugin.getConfigManager().isPreventFire() &&
            (cause == EntityDamageEvent.DamageCause.FIRE ||
             cause == EntityDamageEvent.DamageCause.FIRE_TICK ||
             cause == EntityDamageEvent.DamageCause.LAVA)) {
            e.setCancelled(true);
        } else if (plugin.getConfigManager().isPreventExplosion() &&
            (cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION ||
             cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)) {
            e.setCancelled(true);
        }
    }

    // No protection for the obsidian cage, allowing players to break blocks
    // @EventHandler public void onBlockBreak(BlockBreakEvent e) {
    //     // Check if the broken block is part of an active ritual cage
    //     if (plugin.getObsidianCageManager().isCageActive()) {
    //         if (plugin.getObsidianCageManager().isCageBlock(e.getBlock().getLocation())) {
    //             e.setCancelled(true);
    //             e.getPlayer().sendMessage(plugin.getConfigManager().color("&cThe obsidian cage cannot be broken!"));
    //         }
    //     }
    // }
}
