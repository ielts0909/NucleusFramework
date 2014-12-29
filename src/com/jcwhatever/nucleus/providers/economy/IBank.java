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

package com.jcwhatever.nucleus.providers.economy;

import com.jcwhatever.nucleus.mixins.INamed;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

/**
 * Interface for an economy bank.
 */
public interface IBank extends INamed {

    /**
     * Get the ID of the bank owner.
     *
     * @return  Null if the bank has no owner.
     */
    @Nullable
    UUID getOwnerId();

    /**
     * Get the bank balance.
     */
    double getBalance();

    /**
     * Determine if the specified player has
     * an account with the bank.
     *
     * @param playerId  The ID of the player.
     */
    boolean hasAccount(UUID playerId);

    /**
     * Get a player account from the bank.
     *
     * @param playerId  The ID of the account owner.
     *
     * @return  Null if the account was not found.
     */
    @Nullable
    IAccount getAccount(UUID playerId);

    /**
     * Get all bank accounts.
     */
    List<IAccount> getAccounts();

    /**
     * Create a bank account.
     *
     * @param playerId  The ID of the account owner.
     *
     * @return  Null if the account was not created.
     */
    @Nullable
    IAccount createAccount(UUID playerId);

    /**
     * Delete a bank account.
     *
     * @param playerId  The ID of the account owner.
     *
     * @return  True if the account was found and deleted.
     */
    boolean deleteAccount(UUID playerId);

    /**
     * Get the underlying bank object if the
     * object is wrapped. Otherwise, the handle is
     * the {@code IBank} instance.
     */
    Object getHandle();
}
