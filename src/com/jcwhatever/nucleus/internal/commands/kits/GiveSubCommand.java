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

package com.jcwhatever.nucleus.internal.commands.kits;

import com.jcwhatever.nucleus.internal.NucLang;
import com.jcwhatever.nucleus.managed.commands.CommandInfo;
import com.jcwhatever.nucleus.managed.commands.arguments.ICommandArguments;
import com.jcwhatever.nucleus.managed.commands.exceptions.CommandException;
import com.jcwhatever.nucleus.managed.commands.mixins.IExecutableCommand;
import com.jcwhatever.nucleus.managed.commands.mixins.ITabCompletable;
import com.jcwhatever.nucleus.managed.language.Localizable;
import com.jcwhatever.nucleus.providers.kits.IKit;
import com.jcwhatever.nucleus.providers.kits.Kits;
import com.jcwhatever.nucleus.utils.player.PlayerUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;

@CommandInfo(
        parent="kits",
        command="give",
        staticParams={ "kitName", "playerName=" },
        description="Give a kit to self or specified player.",

        paramDescriptions = {
                "kitName= The name of the kit to give.",
                "playerName= Optional. The name of the player to give the kit to. " +
                        "If omitted, the kit is given to self."})

class GiveSubCommand extends AbstractKitCommand implements IExecutableCommand, ITabCompletable{

    @Localizable static final String _KIT_NOT_FOUND = "A kit named '{0: kit name}' was not found.";
    @Localizable static final String _PLAYER_NOT_FOUND = "A player named '{0: player name}' was not found.";
    @Localizable static final String _SUCCESS = "Kit '{0: kit name}' given to player '{1: player name}' removed.";

    @Override
    public void execute(CommandSender sender, ICommandArguments args) throws CommandException {

        String kitName = args.getString("kitName");

        Player player;

        if (args.isDefaultValue("playerName")) {

            CommandException.checkNotConsole(getPlugin(), this, sender);

            player = (Player)sender;
        }
        else {

            String playerName = args.getString("playerName");

            player = PlayerUtils.getPlayer(playerName);
            if (player == null)
                throw new CommandException(NucLang.get(_PLAYER_NOT_FOUND, playerName));
        }

        IKit kit = Kits.get(kitName);
        if (kit == null)
            throw new CommandException(NucLang.get(_KIT_NOT_FOUND, kitName));

        kit.give(player);

        tellSuccess(sender, NucLang.get(_SUCCESS, kit.getName(), player.getName()));
    }

    @Override
    public void onTabComplete(CommandSender sender, String[] arguments, Collection<String> completions) {

        if (arguments.length == 2) {
            tabCompletePlayerName(arguments, completions);
        }
        else {
            super.onTabComplete(sender, arguments, completions);
        }
    }
}
