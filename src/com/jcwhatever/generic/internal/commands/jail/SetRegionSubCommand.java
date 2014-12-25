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


package com.jcwhatever.generic.internal.commands.jail;

import com.jcwhatever.generic.GenericsLib;
import com.jcwhatever.generic.commands.AbstractCommand;
import com.jcwhatever.generic.commands.CommandInfo;
import com.jcwhatever.generic.commands.arguments.CommandArguments;
import com.jcwhatever.generic.commands.exceptions.CommandException;
import com.jcwhatever.generic.jail.Jail;
import com.jcwhatever.generic.regions.selection.IRegionSelection;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


@CommandInfo(
        parent="jail",
        command="setregion", 
        description="Set jail region using your current region selection.")

public final class SetRegionSubCommand extends AbstractCommand {

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws CommandException {

        CommandException.assertNotConsole(this, sender);

        IRegionSelection sel = getRegionSelection((Player) sender);
        if (sel == null)
            return; // finish
        
        Jail jail = GenericsLib.getDefaultJail();

        jail.getJailBounds().setCoords(sel.getP1(), sel.getP2());

        tellSuccess(sender, "Default Jail region set to your current region selection.");
    }

}