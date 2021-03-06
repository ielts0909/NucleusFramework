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


package com.jcwhatever.nucleus.managed.commands.response;


import com.google.common.collect.ImmutableMap;
import com.jcwhatever.nucleus.internal.NucLang;
import com.jcwhatever.nucleus.managed.language.Localizable;
import com.jcwhatever.nucleus.managed.language.Localized;
import com.jcwhatever.nucleus.utils.PreCon;

import java.util.Map;

/**
 * A response expected from a player that has received a response request.
 *
 * <p>The response is given in the form of a command. The command that the
 * player must execute is the command name retrieved by invoking
 * {@link #getCommandName}.</p>
 */
public class ResponseType {

    @Localizable static final String _YES = "yes";
    @Localizable static final String _NO = "no";
    @Localizable static final String _ACCEPT = "accept";
    @Localizable static final String _DECLINE = "decline";
    @Localizable static final String _OK = "ok";
    @Localizable static final String _CANCEL = "cancel";
    @Localizable static final String _ALLOW = "allow";
    @Localizable static final String _DENY = "deny";
    @Localizable static final String _CONFIRM = "confirm";

    public static final ResponseType YES = new ResponseType(_YES);
    public static final ResponseType NO = new ResponseType(_NO);
    public static final ResponseType ACCEPT = new ResponseType(_ACCEPT);
    public static final ResponseType DECLINE = new ResponseType(_DECLINE);
    public static final ResponseType OK = new ResponseType(_OK);
    public static final ResponseType CANCEL = new ResponseType(_CANCEL);
    public static final ResponseType ALLOW = new ResponseType(_ALLOW);
    public static final ResponseType DENY = new ResponseType(_DENY);
    public static final ResponseType CONFIRM = new ResponseType(_CONFIRM);

    private static Map<String, ResponseType> _typeMap;

    private String _commandName;

    /**
     * Constructor.
     *
     * @param commandName  The response command.
     */
    public ResponseType (String commandName) {
        PreCon.notNullOrEmpty(commandName);
        _commandName = commandName;
    }

    /**
     * Get the command that must be executed by a player to use
     * the response type.
     */
    @Localized
    public String getCommandName() {
        return NucLang.get(_commandName).toString();
    }

    /**
     * Get an existing {@link ResponseType} that matches the specified
     * command name or create a new one.
     *
     * @param commandName  The command.
     */
    public static ResponseType from(String commandName) {
        PreCon.notNullOrEmpty(commandName);

        loadMaps();

        ResponseType responseType = _typeMap.get(commandName.toLowerCase());
        if (responseType != null)
            return responseType;

        return new ResponseType(commandName);
    }

    private static void loadMaps() {
        if (_typeMap != null)
            return;

        _typeMap =
                new ImmutableMap.Builder<String, ResponseType>()
                        .put(YES.getCommandName(), YES)
                        .put(NO.getCommandName(), NO)
                        .put(ACCEPT.getCommandName(), ACCEPT)
                        .put(DECLINE.getCommandName(), DECLINE)
                        .put(OK.getCommandName(), OK)
                        .put(CANCEL.getCommandName(), CANCEL)
                        .put(ALLOW.getCommandName(), ALLOW)
                        .put(DENY.getCommandName(), DENY)
                        .put(CONFIRM.getCommandName(), CONFIRM)
                        .build();
    }

    @Override
    public int hashCode() {
        return _commandName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ResponseType &&
                ((ResponseType) obj)._commandName.equalsIgnoreCase(_commandName);
    }
}
