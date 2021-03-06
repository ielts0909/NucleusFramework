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

package com.jcwhatever.nucleus.providers.kits;

import com.jcwhatever.nucleus.mixins.INamedInsensitive;
import com.jcwhatever.nucleus.mixins.IPluginOwned;
import com.jcwhatever.nucleus.utils.items.ItemStackMatcher;

import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

/**
 * A kit of items that can be given (or taken) from an entity.
 */
public interface IKit extends INamedInsensitive, IPluginOwned {

    /**
     * Get the kit context.
     */
    IKitContext getContext();

    /**
     * Get the kit helmet, if any.
     */
    @Nullable
    ItemStack getHelmet();

    /**
     * Get the kit chest plate, if any.
     */
    @Nullable
    ItemStack getChestplate();

    /**
     * Get the kit leggings, if any.
     */
    @Nullable
    ItemStack getLeggings();

    /**
     * Gets the kit boots, if any.
     */
    @Nullable
    ItemStack getBoots();

    /**
     * Gets a new array of non-armor items in the kit.
     */
    ItemStack[] getItems();

    /**
     * Gets the kit armor items (for human entity) as an a new array.
     *
     * <p>The array size always has 4 elements. Starting from index 0 the items are
     * helmet, chestplate, leggings, boots. If any of the items is not present in
     * the kit then the value of the element is null.</p>
     */
    ItemStack[] getArmor();

    /**
     * Give the kit to the specified entity.
     *
     * @param entity  The entity to give a copy of the kit to.
     */
    void give(Entity entity);

    /**
     * Take items from the kit away from the specified entity.
     *
     * <p>Does not take items if the entity does not have all required items.</p>
     *
     * @param entity  The entity to take from.
     * @param qty     The number of items to take. (kit * qty)
     *
     * @return  True if the items were taken.
     */
    boolean take(Entity entity, int qty);

    /**
     * Take items from the kit away from the specified entity.
     *
     * <p>Does not take items if the entity does not have all required items.</p>
     *
     * @param entity   The entity to take from.
     * @param matcher  The {@link ItemStackMatcher} used to compare items.
     * @param qty      The number of items to take. (kit * qty)
     *
     * @return  True if the entity had all the items and they were taken.
     */
    boolean take(Entity entity, ItemStackMatcher matcher, int qty);
}
