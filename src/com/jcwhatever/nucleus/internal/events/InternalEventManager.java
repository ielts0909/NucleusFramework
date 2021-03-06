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

package com.jcwhatever.nucleus.internal.events;

import com.jcwhatever.nucleus.Nucleus;
import com.jcwhatever.nucleus.events.manager.BukkitEventForwarder;
import com.jcwhatever.nucleus.events.manager.EventManager;
import com.jcwhatever.nucleus.managed.scheduler.Scheduler;

import org.bukkit.plugin.Plugin;

/**
 * NucleusFrameworks global event manager.
 */
public final class InternalEventManager extends EventManager {

    public InternalEventManager(Plugin plugin) {
        super(plugin, null);

        new GlobalForwarder(this);

        Scheduler.runTaskLater(Nucleus.getPlugin(), new Runnable() {
            @Override
            public void run() {

                BukkitEventForwarder.init();
            }
        });
    }

    @Override
    public void dispose() {

        // The global manager cannot be disposed.
        throw new RuntimeException("Cannot dispose the global event manager.");
    }
}
