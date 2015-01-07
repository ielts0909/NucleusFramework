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

package com.jcwhatever.nucleus.nms;

import org.bukkit.entity.Player;

/**
 * Interface for NucleusFramework's Minecraft List Header and Footer handler which
 * can be retrieved from NucleusFramework's NmsManager under the
 * name "LIST_HEADER_FOOTER".
 */
public interface INmsListHeaderFooterHandler extends INmsHandler {

    /**
     * Send a tab list header and/or footer to a player.
     *
     * <p>The handler is responsible for converting the raw
     * header and footer text into the appropriate format.</p>
     *
     * @param player         The player to send the header/footer to.
     * @param rawHeaderText  The header text.
     * @param rawFooterText  The footer text.
     */
    void send(Player player, String rawHeaderText, String rawFooterText);

    /**
     * Send a tab list header and/or footer to a player.
     *
     * <p>Bypasses the handlers text conversion.</p>
     *
     * @param player           The player to send the header/footer to.
     * @param jsonHeaderText  The json header text.
     * @param jsonFooterText  The json footer text.
     */
    void sendJson(Player player, String jsonHeaderText, String jsonFooterText);
}
