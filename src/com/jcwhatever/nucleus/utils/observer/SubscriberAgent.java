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

package com.jcwhatever.nucleus.utils.observer;

import com.jcwhatever.nucleus.utils.PreCon;

import java.util.HashSet;
import java.util.Set;

/**
 * An abstract implementation of an {@link ISubscriberAgent}.
 *
 * <p>Provides the basic implementation of tracking and removing
 * subscribers and handling disposal.</p>
 */
public abstract class SubscriberAgent implements ISubscriberAgent {

    private Set<ISubscriber> _subscribers;
    private final Object _sync = new Object();
    private volatile boolean _isDisposed;

    @Override
    public boolean addSubscriber(ISubscriber subscriber) {
        PreCon.notNull(subscriber);

        synchronized (getSync()) {
            if (registerReference(subscriber)) {
                subscriber.registerReference(this);
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean removeSubscriber(ISubscriber subscriber) {
        PreCon.notNull(subscriber);

        synchronized (getSync()) {

            if (!hasSubscribers())
                return false;

            if (unregisterReference(subscriber)) {
                subscriber.unregisterReference(this);
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean registerReference(ISubscriber subscriber) {
        PreCon.notNull(subscriber);

        if (isDisposed())
            throw new RuntimeException("Cannot use a disposed SubscriberAgent.");

        synchronized (getSync()) {
            return subscribers().add(subscriber);
        }
    }

    @Override
    public boolean unregisterReference(ISubscriber subscriber) {
        PreCon.notNull(subscriber);

        synchronized (getSync()) {
            return hasSubscribers() && subscribers().remove(subscriber);
        }
    }

    @Override
    public Set<ISubscriber> getSubscribers() {

        synchronized (getSync()) {

            if (!hasSubscribers())
                return new HashSet<>(0);

            return new HashSet<>(subscribers());
        }
    }

    @Override
    public boolean isDisposed() {
        return _isDisposed;
    }

    @Override
    public void dispose() {

        if (_isDisposed)
            return;

        synchronized (getSync()) {

            if (_isDisposed)
                return;

            if (hasSubscribers()) {

                for (ISubscriber subscriber : subscribers()) {
                    subscriber.unregisterReference(this);
                }

                subscribers().clear();
            }

            _isDisposed = true;
        }
    }

    /**
     * Invoked to get the set of subscribers.
     */
    protected Set<ISubscriber> subscribers() {
        if (_subscribers == null)
            _subscribers = new HashSet<>(7);

        return _subscribers;
    }

    /**
     * Determine if there is at least 1 subscriber.
     */
    protected boolean hasSubscribers() {
        return _subscribers != null && !_subscribers.isEmpty();
    }

    /**
     * Get the object used for synchronization.
     */
    protected Object getSync() {
        return _sync;
    }
}
