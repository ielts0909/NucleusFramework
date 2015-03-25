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


package com.jcwhatever.nucleus.internal.providers.permissions;

import com.jcwhatever.nucleus.Nucleus;
import com.jcwhatever.nucleus.collections.players.PlayerMap;
import com.jcwhatever.nucleus.internal.providers.InternalProviderInfo;
import com.jcwhatever.nucleus.providers.permissions.IPermission;
import com.jcwhatever.nucleus.storage.DataPath;
import com.jcwhatever.nucleus.storage.DataStorage;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.Permissions;
import com.jcwhatever.nucleus.utils.PreCon;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

/**
 * Basic Bukkit permissions handler
 */
public final class BukkitProvider extends AbstractPermissionsProvider {

    public static final String NAME = "NucleusBukkitPerms";

    private static Map<UUID, PermissionAttachment> _transient = new PlayerMap<PermissionAttachment>(Nucleus.getPlugin());
    private static Listener _bukkitListener;

    private IDataNode _dataNode;

    /**
     * Constructor.
     */
    public BukkitProvider() {

        setInfo(new InternalProviderInfo(this.getClass(),
                NAME, "Default Bukkit permissions provider."));

        // get permissions data node
        _dataNode = DataStorage.get(Nucleus.getPlugin(), new DataPath("bukkit-permissions"));
        _dataNode.load();

        // initialize bukkit listener if its not already initialized
        if (_bukkitListener == null) {
            _bukkitListener = new PermissionListener();
            Bukkit.getPluginManager().registerEvents(_bukkitListener, Nucleus.getPlugin());
        }
    }

    @Override
    public boolean has(CommandSender sender, String permissionName) {
        PreCon.notNull(sender);
        PreCon.notNullOrEmpty(permissionName);

        return sender.hasPermission(permissionName);
    }

    @Override
    public boolean addTransient(Plugin plugin, CommandSender sender, String permissionName) {
        PreCon.notNull(plugin);
        PreCon.notNull(sender);
        PreCon.notNullOrEmpty(permissionName);

        Player p = getPlayer(sender);
        if (p == null)
            return false;

        IPermission perm = Permissions.get(permissionName);
        if (perm == null)
            return false;

        PermissionAttachment attachment = _transient.get(p.getUniqueId());
        if (attachment == null) {
            attachment = sender.addAttachment(plugin);
            _transient.put(p.getUniqueId(), attachment);
        }

        attachment.setPermission((Permission)perm.getHandle(), true);

        return true;
    }

    @Override
    public boolean removeTransient(Plugin plugin, CommandSender sender, String permissionName) {
        PreCon.notNull(plugin);
        PreCon.notNull(sender);
        PreCon.notNullOrEmpty(permissionName);

        Player p = getPlayer(sender);
        if (p == null)
            return false;

        IPermission perm = Permissions.get(permissionName);
        if (perm == null)
            return false;

        PermissionAttachment attachment = _transient.get(p.getUniqueId());
        if (attachment == null) {
            return false;
        }

        attachment.setPermission((Permission)perm.getHandle(), false);

        return true;
    }

    @Override
    public boolean add(Plugin plugin, CommandSender sender, String permissionName) {
        PreCon.notNull(plugin);
        PreCon.notNull(sender);
        PreCon.notNullOrEmpty(permissionName);

        Player p = getPlayer(sender);
        if (p == null)
            return false;

        String pid = p.getUniqueId().toString() + '.' + plugin.getName();

        List<String> permissions = _dataNode.getStringList(pid, null);
        if (permissions == null)
            permissions = new ArrayList<String>(30);

        permissions.add(permissionName);

        _dataNode.set(pid, permissions);
        _dataNode.save();

        return addTransient(plugin, sender, permissionName);
    }

    @Override
    public boolean remove(Plugin plugin, CommandSender sender, String permissionName) {
        PreCon.notNull(plugin);
        PreCon.notNull(sender);
        PreCon.notNullOrEmpty(permissionName);

        Player p = getPlayer(sender);
        if (p == null)
            return false;

        String pid = p.getUniqueId().toString() + '.' + plugin.getName();

        List<String> permissions = _dataNode.getStringList(pid, null);
        if (permissions == null)
            permissions = new ArrayList<String>(30);

        permissions.remove(permissionName);

        _dataNode.set(pid, permissions);

        return removeTransient(plugin, sender, permissionName);
    }

    @Nullable
    @Override
    public Object getHandle() {
        return null;
    }

    @Nullable
    private Player getPlayer(CommandSender sender) {
        if (!(sender instanceof Player))
            return null;

        return (Player)sender;
    }

    /**
     * Bukkit event listener
     */
    private static class PermissionListener implements Listener {

        /**
         * Add permissions on player join.
         */
        @EventHandler(priority= EventPriority.LOW)
        private void onPlayerJoin(PlayerJoinEvent event) {

            Player p = event.getPlayer();

            // give permissions

            BukkitProvider perms = (BukkitProvider)
                    Nucleus.getProviderManager().getPermissionsProvider();

            // get players permission data node
            IDataNode playerNode = perms._dataNode.getNode(p.getUniqueId().toString());

            // get the names of the plugins that have set permissions on the player.
            for (IDataNode node : playerNode) {

                // make sure the plugin is loaded.
                Plugin plugin = Bukkit.getPluginManager().getPlugin(node.getName());
                if (plugin == null)
                    continue;

                // get the list of permissions the player has.
                List<String> permissions = playerNode.getStringList(plugin.getName(), null);

                if (permissions == null || permissions.isEmpty())
                    continue;

                // add permissions to player
                for (String permission : permissions) {
                    // add permissions as transient to prevent re-saving them
                    Permissions.addTransient(plugin, p, permission);
                }
            }
        }

    }

}
