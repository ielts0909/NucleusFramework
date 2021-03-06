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

package com.jcwhatever.nucleus.managed.commands;

import com.jcwhatever.nucleus.managed.commands.utils.ICommandUsageGenerator;

import org.bukkit.plugin.Plugin;

/**
 * Interface for the global command manager.
 */
public interface ICommandManager {

    /**
     * Create a new dispatcher instance.
     *
     * @param plugin  The plugin the dispatcher is for.
     */
    ICommandDispatcher createDispatcher(Plugin plugin);

    /**
     * Get a command usage string generator.
     */
    ICommandUsageGenerator getUsageGenerator();

    /**
     * Get a command usage string generator.
     *
     * @param defaultTemplate  The default template to use when one is not specified.
     */
    ICommandUsageGenerator getUsageGenerator(String defaultTemplate);
}
