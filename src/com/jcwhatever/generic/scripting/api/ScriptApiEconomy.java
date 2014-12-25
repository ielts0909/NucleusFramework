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


package com.jcwhatever.generic.scripting.api;

import com.jcwhatever.generic.utils.EconomyUtils;
import com.jcwhatever.generic.utils.player.PlayerUtils;
import com.jcwhatever.generic.scripting.IEvaluatedScript;
import com.jcwhatever.generic.scripting.ScriptApiInfo;
import com.jcwhatever.generic.utils.PreCon;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Provides script with Economy API
 */
@ScriptApiInfo(
        variableName = "economy",
        description = "Provide scripts with Economy API.")
public class ScriptApiEconomy extends GenericsScriptApi {

    private static ApiObject _api;

    /**
     * Constructor. Automatically adds variable to script.
     *
     * @param plugin The owning plugin
     */
    public ScriptApiEconomy(Plugin plugin) {
        super(plugin);
    }

    @Override
    public IScriptApiObject getApiObject(IEvaluatedScript script) {
        if (_api == null)
            _api = new ApiObject();

        return _api;
    }

    public void reset() {
        if (_api != null)
            _api.dispose();
    }

    public static class ApiObject implements IScriptApiObject {

        ApiObject(){}

        @Override
        public boolean isDisposed() {
            return false;
        }

        @Override
        public void dispose() {
            // do nothing
        }

        /**
         * Get the balance of a players global account.
         *
         * @param player  The player.
         */
        public double getBalance(Object player) {
            PreCon.notNull(player);

            Player p = PlayerUtils.getPlayer(player);
            PreCon.notNull(p);

            return EconomyUtils.getBalance(p.getUniqueId());
        }

        /**
         * Deposit money into a players global account.
         *
         * @param player  The player.
         * @param amount  The amount to give.
         *
         * @return  True if successful
         */
        public boolean deposit(Object player, double amount) {
            PreCon.notNull(player);

            Player p = PlayerUtils.getPlayer(player);
            PreCon.notNull(p);

            return EconomyUtils.deposit(p.getUniqueId(), amount);
        }

        /**
         * Withdraw money from a players global account.
         *
         * @param player  The player.
         * @param amount  The amount to take.
         *
         * @return  True if successful
         */
        public boolean withdraw(Object player, double amount) {
            PreCon.notNull(player);

            Player p = PlayerUtils.getPlayer(player);
            PreCon.notNull(p);

            return EconomyUtils.withdraw(p.getUniqueId(), amount);
        }

        /**
         * Format an amount into a displayable String.
         *
         * @param amount  The amount to format.
         */
        public String formatAmount(double amount) {

            return EconomyUtils.formatAmount(amount);
        }
    }
}