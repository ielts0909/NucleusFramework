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

import java.util.Collection;
import javax.annotation.Nullable;

/**
 * Defines a type as owning a collection of commands.
 */
public interface ICommandOwner {

    /**
     * Register a command.
     *
     * @param commandClass  The command class to register and instantiate.
     */
    boolean registerCommand(Class<? extends ICommand> commandClass);

    /**
     * Unregister a sub command.
     *
     * @param commandClass  The commands implementation class.
     */
    boolean unregisterCommand(Class<? extends ICommand> commandClass);

    /**
     * Get a command by name.
     *
     * <p>Sub commands can be retrieved by separating command names with a period.
     * ie "command.subcommand1.subcommand2"</p>
     */
    @Nullable
    IRegisteredCommand getCommand(String commandName);

    /**
     * Get all commands.
     */
    Collection<IRegisteredCommand> getCommands();

    /**
     * Get all commands.
     *
     * @param output  The output collection to put results into.
     *
     * @return  The output collection.
     */
    <T extends Collection<IRegisteredCommand>> T getCommands(T output);

    /**
     * Get the sub command names.
     */
    Collection<String> getCommandNames();

    /**
     * Get the sub command names.
     *
     * @param output  The output collection to put results into.
     *
     * @return  The output collection.
     */
    <T extends Collection<String>> T getCommandNames(T output);
}
