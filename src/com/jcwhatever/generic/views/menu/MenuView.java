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

package com.jcwhatever.generic.views.menu;

import com.jcwhatever.generic.utils.items.ItemStackComparer;
import com.jcwhatever.generic.utils.PreCon;
import com.jcwhatever.generic.views.IViewFactory;
import com.jcwhatever.generic.views.ViewSession;
import com.jcwhatever.generic.views.chest.ChestEventAction;
import com.jcwhatever.generic.views.chest.ChestEventInfo;
import com.jcwhatever.generic.views.chest.ChestView;
import com.jcwhatever.generic.views.chest.InventoryItemAction.InventoryPosition;
import com.jcwhatever.generic.views.data.ViewArguments;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Abstract implementation of a {@code ChestView} used as a menu.
 */
public abstract class MenuView extends ChestView {

    private final Map<Integer, MenuItem> _menuItems = new HashMap<>(MAX_SLOTS);

    /**
     * Constructor.
     *
     * @param title      The inventory title of the view.
     * @param session    The player view session.
     * @param factory    The factory that instantiated the menu view.
     * @param arguments  The meta arguments for the view.
     * @param comparer   An item stack comparer.
     */
    protected MenuView(@Nullable String title, ViewSession session,
                       IViewFactory factory, ViewArguments arguments,
                       @Nullable ItemStackComparer comparer) {
        super(title, session, factory, arguments, comparer);
    }

    /**
     * Get the currently registered {@code MenuItem}'s.
     */
    public List<MenuItem> getMenuItems() {
        return new ArrayList<>(_menuItems.values());
    }

    /**
     * Get the registered {@code MenuItem} at the specified
     * slot index.
     *
     * @param slot  The slot index.
     *
     * @return  Null if not found.
     */
    @Nullable
    public MenuItem getMenuItem(int slot) {
        return _menuItems.get(slot);
    }

    /**
     * Remove a menu item from the view.
     *
     * @param menuItem  The menu item to remove.
     */
    public void removeMenuItem(MenuItem menuItem) {
        MenuItem item = _menuItems.get(menuItem.getSlot());
        if (menuItem.equals(item))
            return;

        _menuItems.remove(menuItem.getSlot());

        Inventory inventory = getInventory();
        if (inventory == null)
            return;

        menuItem.setVisible(this, false);
    }

    /**
     * Set a menu item into the view inventory and register it.
     *
     * @param menuItem  The menu item to set.
     */
    public void setMenuItem(MenuItem menuItem) {
        PreCon.notNull(menuItem);

        _menuItems.put(menuItem.getSlot(), menuItem);

        Inventory inventory = getInventory();
        if (inventory == null)
            return;

        inventory.setItem(menuItem.getSlot(), menuItem.getItemStack());
    }

    /**
     * Create the inventory needed by the {@code ChestView} super type.
     */
    @Override
    protected Inventory createInventory() {

        _menuItems.clear();

        List<MenuItem> menuItems = createMenuItems();
        for (MenuItem item : menuItems) {
            _menuItems.put(item.getSlot(), item);
        }

        if (_menuItems.size() > MAX_SLOTS)
            throw new RuntimeException("The number of menu items cannot be more than " + MAX_SLOTS + '.');

        int maxSlots = getSlotsRequired();

        Inventory inventory = getTitle() != null
                ? Bukkit.createInventory(getPlayer(), maxSlots, getTitle())
                : Bukkit.createInventory(getPlayer(), maxSlots);

        for (MenuItem item : _menuItems.values()) {
            //item.set(this);
            inventory.setItem(item.getSlot(), item.getItemStack());
        }

        return inventory;
    }

    /**
     * Deny placing items into the menu.
     */
    @Override
    protected ChestEventAction onItemsPlaced(ChestEventInfo eventInfo) {
        return ChestEventAction.DENY;
    }

    /**
     * Deny dropping items from the menu.
     */
    @Override
    protected ChestEventAction onItemsDropped(ChestEventInfo eventInfo) {
        return ChestEventAction.DENY;
    }

    /**
     * Deny picking up items from the menu. Detect clicks on menu items.
     */
    @Override
    protected ChestEventAction onItemsPickup(ChestEventInfo eventInfo) {

        if (eventInfo.getInventoryPosition() == InventoryPosition.TOP) {

            MenuItem menuItem = _menuItems.get(eventInfo.getSlot());
            if (menuItem != null && menuItem.isVisible(this)) {

                List<Runnable> clickCallbacks = menuItem.getOnClick();
                for (Runnable onClick : clickCallbacks) {
                    onClick.run();
                }

                if (menuItem.isCancelled()) {
                    menuItem.setCancelled(false);
                }
                else {
                    onItemSelect(menuItem);
                }
            }
        }

        return ChestEventAction.DENY;
    }

    /**
     * Get the number of slots needed for the {@code Inventory}
     * instance.
     */
    protected int getSlotsRequired() {
        int maxSlot = _menuItems.size();

        for (MenuItem menuItem: _menuItems.values()) {

            if (menuItem.getSlot() > maxSlot) {
                maxSlot = menuItem.getSlot();
            }
        }

        int rows = (int) Math.ceil((double)maxSlot / ROW_SIZE);
        return Math.max(rows * ROW_SIZE, ROW_SIZE);
    }

    /**
     * Called to get a list of {@code MenuItem}'s to initially register and
     * fill the {@code Inventory} after it is created.
     */
    protected abstract List<MenuItem> createMenuItems();

    /**
     * Called when a menu item in the inventory view is clicked
     * by the player.
     *
     * @param menuItem  The clicked menu item.
     */
    protected abstract void onItemSelect(MenuItem menuItem);
}