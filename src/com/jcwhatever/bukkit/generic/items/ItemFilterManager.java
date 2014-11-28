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


package com.jcwhatever.bukkit.generic.items;

import com.jcwhatever.bukkit.generic.internal.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.generic.storage.IDataNode;
import com.jcwhatever.bukkit.generic.utils.PreCon;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * A collection of ItemStacks that can be used for validation/filtering of ItemStacks.
 */
public class ItemFilterManager {

    @Localizable static final String _WHITELIST = "Whitelist";
    @Localizable static final String _BLACKLIST = "Blacklist";

    private Plugin _plugin;
    private IDataNode _dataNode;

    private FilterPolicy _filter = FilterPolicy.WHITELIST;
    private ItemStackComparer _comparer;
    private final Map<ItemWrapper, ItemWrapper> _filterItems = new HashMap<ItemWrapper, ItemWrapper>(6 * 9);

    /**
     * Validation filtering mode
     */
    public enum FilterPolicy {
        WHITELIST,
        BLACKLIST;

        public String getDisplayName() {
            switch (this) {
                case WHITELIST:
                    return Lang.get(_WHITELIST);
                case BLACKLIST:
                    return Lang.get(_BLACKLIST);
                default:
                    return super.toString();
            }
        }

        @Override
        public String toString() {
            return getDisplayName();
        }
    }

    /**
     * Constructor.
     *
     * @param plugin    Owning plugin
     * @param dataNode  Data node to save and load settings.
     */
    public ItemFilterManager(Plugin plugin, @Nullable IDataNode dataNode) {
        PreCon.notNull(plugin);

        _plugin = plugin;
        _dataNode = dataNode;
        _comparer = ItemStackComparer.getDefault();

        loadSettings();
    }

    /**
     * Constructor.
     *
     * @param plugin             The owning plugin
     * @param dataNode           Data node to save and load settings.
     * @param compareOperations  ItemStackComparer bit compare operations to use to match items
     */
    public ItemFilterManager(Plugin plugin, @Nullable IDataNode dataNode, byte compareOperations) {
        PreCon.notNull(plugin);

        _plugin = plugin;
        _dataNode = dataNode;
        _comparer = new ItemStackComparer(compareOperations);

        loadSettings();
    }

    /**
     * Constructor.
     *
     * @param plugin    The owning plugin
     * @param dataNode  Data node to save and load settings.
     * @param comparer  The {@link ItemStackComparer} to use to match items.
     */
    public ItemFilterManager(Plugin plugin, @Nullable IDataNode dataNode, ItemStackComparer comparer) {
        PreCon.notNull(plugin);
        PreCon.notNull(comparer);

        _plugin = plugin;
        _dataNode = dataNode;
        _comparer = comparer;

        loadSettings();
    }

    /**
     * Get the owning plugin.
     */
    public Plugin getPlugin() {
        return _plugin;
    }

    /**
     * Get the filter mode used for validating items.
     */
    public FilterPolicy getFilterPolicy() {
        return _filter;
    }

    /**
     * Set the filter policy used for validation items.
     *
     * @param filter  The filter policy.
     */
    public void setMode(FilterPolicy filter) {
        PreCon.notNull(filter);

        _filter = filter;

        if (_dataNode != null) {
            _dataNode.set("mode", filter);
            _dataNode.saveAsync(null);
        }
    }


    /**
     * Get the compare operations bit flags.
     */
    public byte getCompareOperations() {
        return _comparer.getCompareOperations();
    }

    /**
     * Get the {@link ItemStackComparer} used to match items.
     */
    public ItemStackComparer getItemStackComparer() {
        return _comparer;
    }

    /**
     * Determine if the specified {code ItemStack} is valid.
     * Checks to see if the collection contains a matching {@code ItemStack} as
     * determined by the collections {@code ItemStackComparer} and the filter mode.
     *
     * @param item  The item to check.
     */
    public boolean isValidItem(ItemStack item) {
        PreCon.notNull(item);

        ItemWrapper wrapper = _filterItems.get(new ItemWrapper(item, _comparer));
        if (wrapper == null)
            return _filter == FilterPolicy.BLACKLIST;

        return _filter == FilterPolicy.WHITELIST;
    }

    /**
     * Get a new set of wrapped items from the collection.
     */
    public Set<ItemWrapper> getItems() {
        return new HashSet<ItemWrapper>(_filterItems.keySet());
    }

    /**
     * Add an item to the collection.
     *
     * @param itemStack  The item to add.
     */
    public boolean addItem(ItemStack itemStack) {
        PreCon.notNull(itemStack);

        ItemWrapper wrapper = new ItemWrapper(itemStack, _comparer);
        if (_filterItems.put(wrapper, wrapper) != null) {
            saveFilterItems();
            return true;
        }
        return false;
    }

    /**
     * Add multiple items to the collection.
     *
     * @param itemStacks  The items to add.
     */
    public boolean addItems(ItemStack[] itemStacks) {
        PreCon.notNull(itemStacks);

        for (ItemStack stack : itemStacks) {
            ItemWrapper wrapper = new ItemWrapper(stack, _comparer);
            _filterItems.put(wrapper, wrapper);
        }

        saveFilterItems();

        return true;
    }

    /**
     * Remove an item from the collection.
     *
     * @param itemStack  The item to remove.
     */
    public boolean removeItem(ItemStack itemStack) {
        PreCon.notNull(itemStack);

        if (_filterItems.remove(new ItemWrapper(itemStack, _comparer)) != null) {
            saveFilterItems();
            return true;
        }
        return false;
    }

    /**
     * Remove multiple items from the collection.
     *
     * @param itemStacks  The items to remove.
     */
    public boolean removeItems(ItemStack[] itemStacks) {
        PreCon.notNull(itemStacks);

        for (ItemStack stack : itemStacks) {
            _filterItems.remove(new ItemWrapper(stack, _comparer));
        }

        saveFilterItems();

        return true;
    }

    /**
     * Remove all items from the collection.
     */
    public boolean clearItems() {
        _filterItems.clear();

        if (_dataNode != null) {
            _dataNode.set("items", null);
            _dataNode.saveAsync(null);
        }
        return true;
    }


    private void saveFilterItems() {
        if (_dataNode == null)
            return;

        Collection<ItemWrapper> wrappers = _filterItems.values();

        List<ItemStack> stacks = new ArrayList<ItemStack>(wrappers.size());

        for (ItemWrapper wrapper : wrappers) {
            stacks.add(wrapper.getItem());
        }

        _dataNode.set("items", stacks.toArray(new ItemStack[stacks.size()]));
        _dataNode.saveAsync(null);
    }

    private void loadSettings() {
        if (_dataNode == null)
            return;

        _filter = _dataNode.getEnum("policy", _filter, FilterPolicy.class);

        ItemStack[] craftItems = _dataNode.getItemStacks("items");

        _filterItems.clear();
        if (craftItems != null && craftItems.length > 0) {
            for (ItemStack stack : craftItems) {
                ItemWrapper wrapper = new ItemWrapper(stack, _comparer);
                _filterItems.put(wrapper, wrapper);
            }
        }
    }


}
