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

package com.jcwhatever.nucleus.utils.observer.script;

import com.jcwhatever.nucleus.mixins.IDisposable;

/**
 * An interface that can be easily created by script engines
 * from a script function.
 *
 * <p>Encapsulate using {@link ScriptEventSubscriber} to convert to an
 * {@link com.jcwhatever.nucleus.utils.observer.event.EventSubscriber}.</p>
 */
public interface IScriptEventSubscriber<E> {

    /**
     * Invoked when an event that is subscribed to is called.
     *
     * @param event   The called event.
     * @param handle  The script subscribers {@link IDisposable} wrapper which
     *                can be used to un-subscribe from within the script.
     */
    void onEvent(E event, IDisposable handle);
}