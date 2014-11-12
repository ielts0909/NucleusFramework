/*
 * This file is part of GenericsLib for Bukkit, licensed under the MIT License (MIT).
 *
 * Copyright (c) JCThePants (www.jcwhatever.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


package com.jcwhatever.bukkit.generic.items.floating;

import com.jcwhatever.bukkit.generic.GenericsLib;
import com.jcwhatever.bukkit.generic.events.bukkit.floatingitems.FloatingItemPickUpEvent;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.generic.utils.Scheduler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class BukkitListener implements Listener {

    private Map<UUID, FloatingItem> _floatingItems = new HashMap<>(100);


    void register(FloatingItem item) {
        PreCon.notNull(item);
        PreCon.notNull(item.getEntity());

        _floatingItems.put(item.getEntity().getUniqueId(), item);
    }

    void unregister(FloatingItem item) {
        PreCon.notNull(item);

        Entity entity = item.getEntity();

        if (entity != null) {
            _floatingItems.remove(entity.getUniqueId());
        }
    }


    @EventHandler
    private void onPlayerPickup(PlayerPickupItemEvent event) {

        final FloatingItem item = _floatingItems.get(event.getItem().getUniqueId());
        if (item == null)
            return;

        ItemStack itemToGive = event.getItem().getItemStack();

        // restore display name
        String displayName = item.getItem().getItemMeta().getDisplayName();
        ItemMeta meta = itemToGive.getItemMeta();
        meta.setDisplayName(displayName);
        itemToGive.setItemMeta(meta);

        // prevent or allow pickup
        if (!item.canPickup()) {
            event.setCancelled(true);
        }

        FloatingItemPickUpEvent fiEvent = new FloatingItemPickUpEvent(item, event.getPlayer());
        fiEvent.setCancelled(event.isCancelled());

        Bukkit.getPluginManager().callEvent(fiEvent);

        event.setCancelled(fiEvent.isCancelled());

        // call items pickup event callback handlers
        item.onPickup(event.getPlayer(), event.isCancelled());

        // check for cancelled event.
        if (event.isCancelled()) {

            // set meta back to uuid to prevent merging
            meta.setDisplayName(UUID.randomUUID().toString());
            itemToGive.setItemMeta(meta);
            return;
        }

        final Location location = item.getLocation();

        if (location != null) {
            // schedule respawn
            Scheduler.runTaskLater(GenericsLib.getLib(), item.getRespawnTimeSeconds() * 20, new Runnable() {
                @Override
                public void run() {
                    if (item.isDisposed())
                        return;

                    item.spawn(location);
                }
            });
        }
    }

    @EventHandler
    private void onItemDespawn(ItemDespawnEvent event) {

        final FloatingItem item = _floatingItems.get(event.getEntity().getUniqueId());
        if (item == null)
            return;

        if (item.isSpawned())
            event.setCancelled(true);
    }

    @EventHandler
    private void onItemDestroyed(EntityDamageEvent event) {
        if (event.getEntity().getType() != EntityType.DROPPED_ITEM)
            return;

        final FloatingItem item = _floatingItems.get(event.getEntity().getUniqueId());
        if (item == null)
            return;

        if (item.isSpawned()) {
            event.setDamage(0.0D);
            event.setCancelled(true);
        }
    }
}
