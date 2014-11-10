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


package com.jcwhatever.bukkit.generic;

import com.jcwhatever.bukkit.generic.internal.commands.CommandHandler;
import com.jcwhatever.bukkit.generic.internal.listeners.JCGEventListener;
import com.jcwhatever.bukkit.generic.jail.JailManager;
import com.jcwhatever.bukkit.generic.regions.RegionManager;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.sun.istack.internal.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GenericsLib Bukkit plugin.
 */
public class GenericsLib extends GenericsPlugin {

    private static GenericsLib _instance;
    private static Map<String, GenericsPlugin> _plugins = new HashMap<>(25);

    private JailManager _jailManager;
    private RegionManager _regionManager;

    /**
     * Get the {@code GenericsLib} plugin instance.
     */
    public static GenericsLib getLib() {
        return _instance;
    }

    /**
     * Get a Bukkit plugin that implements {@code GenericsPlugin} by name.
     *
     * @param name  The name of the plugin.
     */
    @Nullable
    public static GenericsPlugin getPlugin(String name) {
        PreCon.notNullOrEmpty(name);

        return _plugins.get(name.toLowerCase());
    }

    /**
     * Get all Bukkit plugins that implement {@code GenericsPlugin}.
     */
    public static List<GenericsPlugin> getPlugins() {
        return new ArrayList<>(_plugins.values());
    }

    /**
     * Get the global {@code RegionManager}.
     */
    public static RegionManager getRegionManager() {
        return _instance._regionManager;
    }

    /**
     * Get the default Jail Manager.
     */
    public static JailManager getJailManager() {
        return _instance._jailManager;
    }

    /**
     * Constructor.
     */
    public GenericsLib() {
        super();

        _instance = this;
    }

    /**
     * Get the chat prefix.
     */
    @Override
    public String getChatPrefix() {
        return "[GenericsLib] ";
    }

    /**
     * Get the console prefix.
     */
    @Override
    public String getConsolePrefix() {
        return getChatPrefix();
    }

    @Override
    protected void onEnablePlugin() {
        _regionManager = new RegionManager();
        _jailManager = new JailManager(this, "default", getDataNode().getNode("jail"));

        registerEventListeners(new JCGEventListener());
        registerCommands(new CommandHandler());
    }

    @Override
    protected void onDisablePlugin() {
        _regionManager = null; // null to prevent exception if re-enabled
    }

    void registerPlugin(GenericsPlugin plugin) {

        _plugins.put(plugin.getName().toLowerCase(), plugin);
    }

    void unregisterPlugin(GenericsPlugin plugin) {

        _plugins.remove(plugin.getName().toLowerCase());
    }
}
