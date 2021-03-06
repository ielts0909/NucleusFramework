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


package com.jcwhatever.nucleus.internal.commands.kits.items;

import com.jcwhatever.nucleus.internal.NucLang;
import com.jcwhatever.nucleus.internal.commands.kits.AbstractKitCommand;
import com.jcwhatever.nucleus.managed.commands.CommandInfo;
import com.jcwhatever.nucleus.managed.commands.arguments.ICommandArguments;
import com.jcwhatever.nucleus.managed.commands.exceptions.CommandException;
import com.jcwhatever.nucleus.managed.commands.mixins.IExecutableCommand;
import com.jcwhatever.nucleus.managed.language.Localizable;
import com.jcwhatever.nucleus.managed.messaging.ChatPaginator;
import com.jcwhatever.nucleus.providers.kits.IKit;
import com.jcwhatever.nucleus.providers.kits.Kits;
import com.jcwhatever.nucleus.utils.items.ItemStackUtils;
import com.jcwhatever.nucleus.utils.text.TextUtils.FormatTemplate;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

@CommandInfo(
        parent="items",
        command="list",
        staticParams={ "kitName", "page=1" },
        description="List items in an inventory kit.",

        paramDescriptions = {
                "kitName= The name of the kit items will be added to. {NAME16}",
                "page= {PAGE}"})

class ListSubCommand extends AbstractKitCommand implements IExecutableCommand {

    @Localizable static final String _PAGINATOR_TITLE = "Kit Items";
    @Localizable static final String _KIT_NOT_FOUND = "An inventory kit named '{0}' was not found.";
    @Localizable static final String _LABEL_ARMOR = "ARMOR";
    @Localizable static final String _LABEL_ITEMS = "ITEMS";
    @Localizable static final String _LABEL_NONE = "<none>";

    @Override
    public void execute(CommandSender sender, ICommandArguments args) throws CommandException {

        String kitName = args.getString("kitName");
        int	page = args.getInteger("page");

        IKit kit = Kits.get(kitName);
        if (kit == null)
            throw new CommandException(NucLang.get(_KIT_NOT_FOUND, kitName));

        ChatPaginator pagin = createPagin(args, 5, NucLang.get(_PAGINATOR_TITLE));

        // Armor
        pagin.addFormatted(FormatTemplate.SUB_HEADER, NucLang.get(_LABEL_ARMOR));
        ItemStack[] armor = kit.getArmor();

        if (armor.length == 0) {
            pagin.addFormatted(FormatTemplate.LIST_ITEM, NucLang.get(_LABEL_NONE));
        }
        else {

            for (ItemStack item : armor) {
                if (item == null)
                    continue;

                pagin.add(ItemStackUtils.serialize(item));
            }
        }

        // Items
        pagin.addFormatted(FormatTemplate.SUB_HEADER, NucLang.get(_LABEL_ITEMS));
        ItemStack[] items = kit.getItems();

        if (items.length == 0) {
            pagin.addFormatted(FormatTemplate.LIST_ITEM, NucLang.get(_LABEL_NONE));
        }
        else {

            for (ItemStack item : items) {
                pagin.add(ItemStackUtils.serialize(item));
            }
        }

        pagin.show(sender, page, FormatTemplate.RAW);
    }
}
