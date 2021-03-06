/*
 * This file is part of NucleusFramework for Bukkit, licensed under the MIT License (MIT).
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


package com.jcwhatever.nucleus.utils.player;

import com.jcwhatever.nucleus.collections.players.PlayerMap;
import com.jcwhatever.nucleus.managed.scheduler.Scheduler;
import com.jcwhatever.nucleus.managed.teleport.Teleporter;
import com.jcwhatever.nucleus.mixins.IPluginOwned;
import com.jcwhatever.nucleus.providers.storage.DataStorage;
import com.jcwhatever.nucleus.storage.DataPath;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.PreCon;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Saves and restores snapshots of a players state.
 *
 * <p>When a players state is restored, saved state data is removed.</p>
 */
public class PlayerState implements IPluginOwned {

    // store player states by plugin
    private static Map<Plugin, PlayerMap<PlayerState>> _statesByPlugin;

    static {
        _statesByPlugin = new WeakHashMap<>(50);
    }

    /**
     * Get the currently stored player state.
     *
     * <p>If no player state is found in memory, an attempt
     * is made to see if a player state is saved to disk.</p>
     *
     * @param plugin  The owning plugin.
     * @param player  The player.
     *
     * @return  Null if no player state is stored.
     */
    @Nullable
    public static PlayerState get(Plugin plugin, Player player) {
        PreCon.notNull(plugin);
        PreCon.notNull(player);

        PlayerMap<PlayerState> states = getStateMap(plugin);
        PlayerState state = states.get(player.getUniqueId());

        // load state from file, if any
        if (state == null) {
            state = new PlayerState(plugin, player);
            if (!loadFromFile(state))
                return null;

            states.put(player.getUniqueId(), state);
        }

        return state;
    }

    /**
     * Saves a player current state and returns the information in
     * a {@link PlayerState} object.
     *
     * <p>If a player state is already saved for the specified plugin,
     * the current player state data overwrites the data in the
     * existing {@link PlayerState} object. Otherwise, a new object
     * is created.</p>
     *
     * @param plugin  The owning plugin.
     * @param player  The player.
     */
    public static PlayerState store(Plugin plugin, Player player) {
        PreCon.notNull(plugin);
        PreCon.notNull(player);

        PlayerMap<PlayerState> states = getStateMap(plugin);

        PlayerState state = states.get(player.getUniqueId());
        if (state == null) {
            state = new PlayerState(plugin, player);
            states.put(player.getUniqueId(), state);
        }

        state.save();

        return state;
    }

    /**
     * Clear the specified players player state from the cache
     * and disk.
     *
     * @param plugin  The owning plugin.
     * @param player  The player.
     */
    public static void clear(Plugin plugin, Player player) {
        PreCon.notNull(plugin);
        PreCon.notNull(player);

        PlayerMap<PlayerState> states = getStateMap(plugin);

        PlayerState state = states.remove(player.getUniqueId());
        if (state == null)
            return;

        state.deleteFile();
    }

    /**
     * Specify if the players location should be restored.
     */
    public enum RestoreLocation {
        TRUE,
        FALSE
    }

    private final Player _player;
    private final UUID _playerId;
    private final Plugin _plugin;

    private PlayerStateSnapshot _snapshot;

    /**
     * Private Constructor.
     *
     * @param plugin  The owning plugin.
     * @param player  The player.
     */
    private PlayerState(Plugin plugin, Player player) {
        _player = player;
        _playerId = player.getUniqueId();
        _plugin = plugin;
    }

    @Override
    public Plugin getPlugin() {
        return null;
    }

    /**
     * Determine if the player state is saved.
     */
    public boolean isSaved() {
        return _snapshot != null;
    }

    /**
     * Get the stored state snapshot..
     *
     * @return Null if the state is not saved.
     */
    @Nullable
    public PlayerStateSnapshot getSnapshot() {
        return _snapshot;
    }

    /**
     * Records player chest and saves to disk.
     *
     * <p>Overwrites any current saved data.</p>
     *
     * @return  True if saving to disk is successful. Save to memory is always successful.
     */
    public boolean save() {

        _snapshot = new PlayerStateSnapshot(_player);

        if (_plugin == null)
            return false;

        IDataNode dataNode = DataStorage.get(_plugin, new DataPath("nucleus.player-states." + _playerId));

        dataNode.set("", _snapshot);
        dataNode.save();

        return true;
    }

    /**
     * Restore the players state.
     *
     * <p>The stored to disk player state is deleted.</p>
     *
     * @param restoreLocation  Specify if the players saved location should be restored.
     *
     * @return  The restore location or null if failed.
     *
     * @throws IOException
     * @throws InvalidConfigurationException
     */
    @Nullable
    public Location restore(RestoreLocation restoreLocation) throws IOException, InvalidConfigurationException {
        PreCon.notNull(restoreLocation);

        if (_snapshot == null)
            return null;

        if (restoreLocation == RestoreLocation.TRUE && _snapshot.getLocation() != null) {
            Teleporter.teleport(_player, _snapshot.getLocation());
        }

        // wait till after the player is teleported to restore
        Scheduler.runTaskLater(_plugin, 2, new Runnable() {
            @Override
            public void run() {

                // remove all potion effects
                for (PotionEffectType potion : PotionEffectType.values()) {
                    if (potion == null || !_player.hasPotionEffect(potion)) continue;
                    _player.removePotionEffect(potion);
                }
                _player.getActivePotionEffects().clear();

                _player.getInventory().setContents(_snapshot.getItems());
                _player.getInventory().setArmorContents(_snapshot.getArmor());
                _player.setFoodLevel(_snapshot.getFoodLevel());
                _player.setLevel(_snapshot.getLevel());
                _player.setExp((float)_snapshot.getExp());
                _player.setHealth(_snapshot.getHealth());
                _player.addPotionEffects(_snapshot.getPotionEffects());
                _player.setGameMode(_snapshot.getGameMode());
                _player.setAllowFlight(_snapshot.isFlightAllowed());
                _player.setFlying(_snapshot.isFlying());
                _player.setFireTicks(_snapshot.getFireTicks());
                _player.setFallDistance(0);// prevent player respawn deaths

                // discard snapshot
                _snapshot = null;

            }
        });

        // remove back up state storage
        DataStorage.remove(_plugin, new DataPath("nucleus.player-states." + _playerId));

        // remove from state map
        getStateMap(_plugin).remove(_player.getUniqueId());

        // get restore location so it can be returned
        return _snapshot.getLocation();
    }

    /**
     * Deletes player state file from disk.
     */
    public void deleteFile() {

        DataPath dataPath = new DataPath("nucleus.player-states." + _playerId);

        if (DataStorage.has(_plugin, dataPath)) {
            DataStorage.remove(_plugin, dataPath);
        }
    }

    /*
     * Get a player state map for the specified plugin.
     * Finds the existing one or creates a new one.
     */
    private static PlayerMap<PlayerState> getStateMap(Plugin plugin) {
        PlayerMap<PlayerState> state = _statesByPlugin.get(plugin);
        if (state == null) {
            state = new PlayerMap<PlayerState>(plugin);
            _statesByPlugin.put(plugin, state);
        }
        return state;
    }

    /*
     *  Loads player state data from a file a fills the provided
     *  PlayerState object with the data.
     */
    private static boolean loadFromFile(PlayerState state) {

        if (!DataStorage.has(state._plugin, new DataPath("nucleus.player-states." + state._playerId)))
            return false;

        IDataNode dataNode = DataStorage.get(state._plugin,
                new DataPath("nucleus.player-states." + state._playerId));

        if (!dataNode.load())
            return false;

        state._snapshot = dataNode.getSerializable("", PlayerStateSnapshot.class);

        return state._snapshot != null;
    }
}
